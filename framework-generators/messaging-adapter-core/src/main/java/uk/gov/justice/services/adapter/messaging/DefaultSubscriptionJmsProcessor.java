package uk.gov.justice.services.adapter.messaging;

import static java.lang.String.format;

import uk.gov.justice.services.core.error.JsonEnvelopeProcessingFailureHandler;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.subscription.SubscriptionManager;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;

/**
 * In order to minimise the amount of generated code in the JMS Listener implementation classes,
 * this service encapsulates all the logic for validating a message and converting it to an
 * envelope, passing it to a subscription manager to manage the message.  This allows testing of
 * this logic independently from the automated generation code.
 */
public class DefaultSubscriptionJmsProcessor implements SubscriptionJmsProcessor {

    @Inject
    private EnvelopeConverter envelopeConverter;

    @Inject
    private TraceLogger traceLogger;

    @Inject
    private JmsMessageLoggerHelper jmsMessageLoggerHelper;

    @Inject
    private JsonEnvelopeProcessingFailureHandler jsonEnvelopeProcessingFailureHandler;


    @Inject
    private Logger logger;

    @Override
    public void process(final Message message,
                        final SubscriptionManager subscriptionManager) {

        traceLogger.trace(logger, () -> format("Processing JMS message: %s", jmsMessageLoggerHelper.toJmsTraceString(message)));

        if (!(message instanceof TextMessage)) {
            try {
                throw new InvalildJmsMessageTypeException(format("Message is not an instance of TextMessage %s", message.getJMSMessageID()));
            } catch (JMSException e) {
                throw new InvalildJmsMessageTypeException(format("Message is not an instance of TextMessage. Failed to retrieve messageId %s",
                        message), e);
            }
        }

        final JsonEnvelope jsonEnvelope = envelopeConverter.fromMessage((TextMessage) message);

        try {
            traceLogger.trace(logger, () -> format("JMS message converted to envelope: %s", jsonEnvelope));
            subscriptionManager.process(jsonEnvelope);
            traceLogger.trace(logger, () -> format("JMS message processed: %s", jsonEnvelope));
        } catch (final Exception e) {
            jsonEnvelopeProcessingFailureHandler.onJsonEnvelopeProcessingFailure(jsonEnvelope, e);
            throw new JmsMessageHandlingException("Error processing JMS message", e);
        }
    }
}
