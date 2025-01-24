package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.error.JsonEnvelopeProcessingFailureHandler;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.subscription.SubscriptionManager;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultSubscriptionJmsProcessorTest {

    @Mock
    private TextMessage textMessage;

    @Mock
    private JsonEnvelope expectedEnvelope;

    @Mock
    private EnvelopeConverter envelopeConverter;

    @Mock
    private ObjectMessage objectMessage;

    @Mock
    private JsonEnvelopeProcessingFailureHandler jsonEnvelopeProcessingFailureHandler;

    @Mock
    private TraceLogger traceLogger;

    @InjectMocks
    private DefaultSubscriptionJmsProcessor subscriptionJmsProcessor;

    @Test
    public void shouldPassValidMessageToConsumerFunction() {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedEnvelope);

        subscriptionJmsProcessor.process(textMessage, subscriptionManager);

        verify(subscriptionManager).process(expectedEnvelope);
    }

    @Test
    public void shouldThrowExceptionWithWrongMessageType() {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        assertThrows(InvalildJmsMessageTypeException.class, () -> subscriptionJmsProcessor.process(objectMessage, subscriptionManager));
    }

    @Test
    public void shouldThrowExceptionWhenFailToRetrieveMessageId() throws Exception {
        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);

        doThrow(JMSException.class).when(objectMessage).getJMSMessageID();

        assertThrows(InvalildJmsMessageTypeException.class, () -> subscriptionJmsProcessor.process(objectMessage, subscriptionManager));
    }

    @Test
    public void shouldHandleExceptions() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        when(envelopeConverter.fromMessage(textMessage)).thenReturn(expectedEnvelope);
        doThrow(nullPointerException).when(subscriptionManager).process(expectedEnvelope);

        final JmsMessageHandlingException jmsMessageHandlingException = assertThrows(
                JmsMessageHandlingException.class,
                () -> subscriptionJmsProcessor.process(textMessage, subscriptionManager));

        assertThat(jmsMessageHandlingException.getCause(), is(nullPointerException));
        assertThat(jmsMessageHandlingException.getMessage(), is("Error processing JMS message"));

        verify(jsonEnvelopeProcessingFailureHandler).onJsonEnvelopeProcessingFailure(expectedEnvelope, nullPointerException);
    }
}