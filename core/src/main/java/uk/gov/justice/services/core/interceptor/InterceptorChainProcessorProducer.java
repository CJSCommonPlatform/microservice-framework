package uk.gov.justice.services.core.interceptor;

import static java.lang.String.format;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped

public class InterceptorChainProcessorProducer {

    @Inject
    private Logger logger;

    @Inject
    private DispatcherCache dispatcherCache;

    @Inject
    private InterceptorCache interceptorCache;

    @Inject
    private ComponentNameExtractor componentNameExtractor;

    /**
     * Produces an interceptor chain processor for the provided injection point.
     *
     * @param injectionPoint class where the {@link InterceptorChainProcessor} is being injected
     * @return the interceptor chain processor
     */
    @Produces
    public InterceptorChainProcessor produceProcessor(final InjectionPoint injectionPoint) {
        trace(logger, () -> format("Interceptor Chain Processor provided for %s", injectionPoint.getClass().getName()));

        final String component = componentNameExtractor.componentFrom(injectionPoint);
        return new DefaultInterceptorChainProcessor(interceptorCache, dispatcherCache.dispatcherFor(injectionPoint)::dispatch, component);
    }

    public InterceptorChainProcessor produceLocalProcessor(final String component) {
        trace(logger, () -> format("Interceptor Chain Processor provided for %s", component));

        final Dispatcher dispatcher = dispatcherCache.dispatcherFor(component, LOCAL);
        return new DefaultInterceptorChainProcessor(interceptorCache, dispatcher::dispatch, component);
    }

}