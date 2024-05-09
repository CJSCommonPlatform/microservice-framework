package uk.gov.justice.services.integrationtest.utils.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

/**
 * Use {@link JmsMessageProducerClientBuilder} to create instance
 * It's safe to create multiple instances of this class with same parameters, as underlying jms producer is cached and it retrieves existing producer
 * Life cycle of underlying jms producer is not managed by this class (Managed by {@link JmsMessageConsumerPool} through Junit hooks {@link JmsResourceManagementExtension}) and hence these instances can be created without worrying about cleaning underlying jms resources
 * This class provides all various helper methods to send message to underlying topic
 * If there is no convenient method that you are looking for, please enhance this class rather than creating them in context ITs. This approach avoids duplication and promotes reusability across different context Integration tests
 */@SuppressWarnings("unused")
public class JmsMessageProducerClient {

    private static final String QUEUE_URI = queueUri();

    private final JmsMessageProducerFactory jmsMessageProducerFactory;
    private MessageProducer messageProducer;

    JmsMessageProducerClient(JmsMessageProducerFactory jmsMessageProducerFactory) {
        this.jmsMessageProducerFactory = jmsMessageProducerFactory;
    }

    void createProducer(String topicName) {
        this.messageProducer = jmsMessageProducerFactory.getOrCreateMessageProducer(topicName, QUEUE_URI);
    }

    //Add convenient methods to send message
    public void sendMessage(final String commandName, final JsonObject payload) {

        final JsonEnvelope jsonEnvelope = createEnvelope(commandName, payload);

        sendMessage(commandName, jsonEnvelope);
    }

    public void sendMessage(final String commandName, final JsonEnvelope jsonEnvelope) {
        if (messageProducer == null) {
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }

        @SuppressWarnings("deprecation") final String json = jsonEnvelope.toDebugStringPrettyPrint();

        try {
            final TextMessage message = createTextMessage(commandName, json);
            messageProducer.send(message);
        } catch (final JMSException e) {
            throw new RuntimeException("Failed to send message. commandName: '" + commandName + "', json: " + json, e);
        }
    }

    private TextMessage createTextMessage(String commandName, String json) throws JMSException {
        final TextMessage message = jmsMessageProducerFactory.getSession().createTextMessage(); //TODO not a nice way to get session find out alternate way to get session for creating TextMessage
        message.setText(json);
        message.setStringProperty("CPPNAME", commandName);

        return message;
    }
}
