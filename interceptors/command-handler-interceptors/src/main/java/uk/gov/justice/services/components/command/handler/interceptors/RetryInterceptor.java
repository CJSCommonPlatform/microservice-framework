package uk.gov.justice.services.components.command.handler.interceptors;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.OptimisticLockingRetryException;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;

/**
 * Interceptor designed to catch the {@link OptimisticLockingRetryException} to retry processing of
 * an envelope within the same transaction to avoid JMS rollback.
 */
public class RetryInterceptor implements Interceptor {

    @Inject
    Logger logger;

    /**
     * Amount of retries the handler will attempt before failing.
     *
     * The default value of 0 means retry indefinitely.
     */
    @Inject
    @Value(key = "handler.retry.max", defaultValue = "0")
    String maxRetry;

    @Inject
    @Value(key = "handler.retry.wait.millis", defaultValue = "1000")
    String waitTime;

    @Inject
    @Value(key = "handler.retry.immediate.retries", defaultValue = "3")
    String immediateRetries;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        final int maxRetryCount = parseInt(maxRetry);
        final int retryWaitTime = parseInt(waitTime);
        final int maxImmediateRetries = parseInt(immediateRetries);

        final JsonObject metadata = interceptorContext.inputEnvelope().metadata().asJsonObject();
        int retries = 0;

        while (maxRetryCount == 0 || retries < maxRetryCount) {
            try {
                return interceptorChain.processNext(interceptorContext);
            } catch (OptimisticLockingRetryException e) {
                logger.debug(format("Optimistic locking failed on command %s at retry attempt %d", metadata, retries));
                retries++;

                if (retries > maxImmediateRetries) {
                    waitFor(retryWaitTime);
                }
            }
        }

        throw new OptimisticLockingRetryFailedException(format("Retry count of %d exceeded for command %s", maxRetryCount, metadata));
    }

    private void waitFor(final int retryWaitTime) {
        try {
            sleep(retryWaitTime);
        } catch (InterruptedException ex) {
            currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }
}