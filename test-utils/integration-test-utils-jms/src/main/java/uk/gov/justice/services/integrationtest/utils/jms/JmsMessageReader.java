package uk.gov.justice.services.integrationtest.utils.jms;

import uk.gov.justice.services.integrationtest.utils.jms.converters.MessageConverter;
import uk.gov.justice.services.integrationtest.utils.jms.converters.ToStringMessageConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import java.util.Optional;
import java.util.function.Consumer;


class JmsMessageReader {

    private final ToStringMessageConverter toStringMessageConverter;

    JmsMessageReader() {
        toStringMessageConverter = new ToStringMessageConverter();
    }

    <T> void registerCallBack(final MessageConsumer messageConsumer, final MessageConverter<T> messageConverter, final Consumer<T> onMessageCallBack) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }

        messageConsumer.setMessageListener(message -> {
            final T convertedMessage = messageConverter.convert(getText((TextMessage) message));
            onMessageCallBack.accept(convertedMessage);
        });
    }

    <T> Optional<T> retrieveMessageNoWait(final MessageConsumer messageConsumer, final MessageConverter<T> messageConverter) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        return retrieve(messageConsumer::receiveNoWait, messageConverter);
    }

    <T> Optional<T> retrieveMessage(final MessageConsumer messageConsumer, final MessageConverter<T> messageConverter, final long timeout) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        return retrieve(() -> messageConsumer.receive(timeout), messageConverter);
    }

    private <T> Optional<T> retrieve(final MessageSupplier messageSupplier, final MessageConverter<T> messageConverter) throws JMSException {
        return Optional.ofNullable((TextMessage) messageSupplier.getMessage())
                .map(message -> messageConverter.convert(getText(message)));
    }

    private String getText(final TextMessage message) {
        try {
            return message.getText();
        } catch (JMSException e) {
            throw new JmsMessagingClientException("Failed to retrieve message", e);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    void clear(final MessageConsumer messageConsumer) throws JMSException {
        if (messageConsumer == null) {
            throw new JmsMessagingClientException("Message consumer not started");
        }
        while (retrieveMessageNoWait(messageConsumer, toStringMessageConverter).isPresent()) {
        }
    }

    @FunctionalInterface
    private interface MessageSupplier {
        Message getMessage() throws JMSException;
    }

}
