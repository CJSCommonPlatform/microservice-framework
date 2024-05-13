package uk.gov.justice.services.integrationtest.utils.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.test.utils.core.messaging.TopicFactory;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

@ExtendWith(MockitoExtension.class)
class JmsMessageProducerFactoryTest {

    private static final String QUEUE_URI = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "jms.topic.public.event";

    @Mock
    private JmsSessionFactory jmsSessionFactory;

    @Mock
    private TopicFactory topicFactory;

    private JmsMessageProducerFactory jmsMessageProducerFactory;

    @BeforeEach
    void setUp() {
        jmsMessageProducerFactory = new JmsMessageProducerFactory(jmsSessionFactory, topicFactory);
    }

    @Nested
    class CreateMessageProducerTest {

        @Test
        void shouldCreateNewSessionAndCreateMessageProducer() throws Exception {
            final Session session = mock(Session.class);
            final ActiveMQTopic topic = mock(ActiveMQTopic.class);
            final MessageProducer messageProducer = mock(MessageProducer.class);
            when(jmsSessionFactory.create(QUEUE_URI)).thenReturn(session);
            when(topicFactory.createTopic(TOPIC_NAME)).thenReturn(topic);
            when(session.createProducer(topic)).thenReturn(messageProducer);

            final MessageProducer result = jmsMessageProducerFactory.getOrCreateMessageProducer(TOPIC_NAME, QUEUE_URI);

            assertThat(result, is(messageProducer));
        }

        @Test
        void shouldReuseExistingSession() throws Exception {
            final Session existingSession = mock(Session.class);
            setField(jmsMessageProducerFactory, "session", existingSession);
            final ActiveMQTopic topic = mock(ActiveMQTopic.class);
            final MessageProducer messageProducer = mock(MessageProducer.class);
            when(topicFactory.createTopic(TOPIC_NAME)).thenReturn(topic);
            when(existingSession.createProducer(topic)).thenReturn(messageProducer);

            final MessageProducer result = jmsMessageProducerFactory.getOrCreateMessageProducer(TOPIC_NAME, QUEUE_URI);

            assertThat(result, is(messageProducer));
            verifyNoInteractions(jmsSessionFactory);
        }

        @Test
        void shouldReturnCachedMessageProducerIfExist() throws Exception {
            final Session session = mock(Session.class);
            when(jmsSessionFactory.create(QUEUE_URI)).thenReturn(session);
            final MessageProducer messageProducer = mock(MessageProducer.class);
            final Map<String, MessageProducer> existingProducers = new ConcurrentHashMap<>();
            existingProducers.put(TOPIC_NAME, messageProducer);
            ReflectionUtil.setField(jmsMessageProducerFactory, "messageProducers", existingProducers);

            final MessageProducer result = jmsMessageProducerFactory.getOrCreateMessageProducer(TOPIC_NAME, QUEUE_URI);

            assertThat(result, is(messageProducer));
            verifyNoInteractions(topicFactory);
            verifyNoInteractions(session);
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            final Session session = mock(Session.class);
            final ActiveMQTopic topic = mock(ActiveMQTopic.class);
            when(jmsSessionFactory.create(QUEUE_URI)).thenReturn(session);
            when(topicFactory.createTopic(TOPIC_NAME)).thenReturn(topic);
            doThrow(new JMSException("Test")).when(session).createProducer(topic);

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageProducerFactory.getOrCreateMessageProducer(TOPIC_NAME, QUEUE_URI));

            assertThat(e.getMessage(), is("Failed to create producer for topic:jms.topic.public.event"));
        }
    }

    @Nested
    class GetSessionTest {

        @Test
        void shouldCreateAndReturnSession() {
            final Session session = mock(Session.class);
            when(jmsSessionFactory.create(QUEUE_URI)).thenReturn(session);

            final Session result = jmsMessageProducerFactory.getSession(QUEUE_URI);

            assertThat(result, is(session));
        }

        @Test
        void shouldReuseExistingSession() {
            final Session existingSession = mock(Session.class);
            setField(jmsMessageProducerFactory, "session", existingSession);

            final Session result = jmsMessageProducerFactory.getSession(QUEUE_URI);

            assertThat(result, is(existingSession));
            verifyNoInteractions(jmsSessionFactory);
        }
    }

    @Nested
    class CloseTest {

        @Test
        void shouldCloseAllMessageProducersAndClearCache() throws Exception {
            final MessageProducer messageProducer = mock(MessageProducer.class);
            final Map<String, MessageProducer> existingProducers = new ConcurrentHashMap<>();
            existingProducers.put(TOPIC_NAME, messageProducer);
            ReflectionUtil.setField(jmsMessageProducerFactory, "messageProducers", existingProducers);

            jmsMessageProducerFactory.close();

            verify(messageProducer).close();
            assertThat(existingProducers.size(), is(0));
        }

        @Test
        void shouldCloseSession() throws Exception {
            jmsMessageProducerFactory.close();

            verify(jmsSessionFactory).close();
        }

        @Test
        void shouldConvertJmsException() throws Exception {
            doThrow(new JMSException("Test")).when(jmsSessionFactory).close();

            final JmsMessagingClientException e = assertThrows(JmsMessagingClientException.class,
                    () -> jmsMessageProducerFactory.close());

            assertThat(e.getMessage(), is("Failed to close producers"));
        }
    }
}