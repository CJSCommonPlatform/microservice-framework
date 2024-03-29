package uk.gov.justice.subscription.jms.it;

import static javax.json.Json.createObjectBuilder;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.artemis.EmbeddedArtemisServer;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJmsAdapterGenerationIT {

    private static int port = -1;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJmsAdapterGenerationIT.class);
    private static final String RANDOM_JMS_PORT = System.getProperty("random.jms.port");
    private final static ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
            "tcp://localhost:" + RANDOM_JMS_PORT + "?broker.persistent=false&jms.useAsyncSend=false");

    @BeforeAll
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        try {
            EmbeddedArtemisServer.startServer();
        } catch (Throwable e) {
            LOG.error("", e);
            fail(e.getMessage());
        }
    }

    @AfterAll
    public static void afterClass() {
        try {
            EmbeddedArtemisServer.stopServer();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .property("httpejbd.port", Integer.toString(port))
                .property(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    protected void sendEnvelope(String metadataId, String commandName, Destination queue) throws JMSException {
        sendEnvelope(metadataId, commandName, queue, createObjectBuilder().build());
    }

    protected void sendEnvelope(String metadataId, String commandName, Destination queue, JsonObject payload) throws JMSException {
        try (final Connection connection = getConnection();
             final Session session = connection.createSession()) {
            final TextMessage message = session.createTextMessage();
            final String envopleJson = envelope()
                    .with(metadataOf(metadataId, commandName))
                    .withPayloadOf("Frederico Bloggaso", "name")
                    .toJsonString();
            message.setText(envopleJson);
            message.setStringProperty("CPPNAME", commandName);
            try (final MessageProducer producer = session.createProducer(queue)) {
                producer.send(message);
            }
        }
    }

    protected void cleanQueue(final Destination queue) throws JMSException {
        try (final Connection connection = getConnection();
             final Session session = connection.createSession()) {

            final MessageConsumer consumer = session.createConsumer(queue);

            while (consumer.receiveNoWait() != null) {
                //Do nothing
            }
        }
    }

    private Connection getConnection() throws JMSException {
        final Connection connection = cf.createConnection();
        connection.start();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            LOG.warn(e.toString());
        }
        return connection;
    }

    protected Session getSession() throws JMSException {
        try {
            return getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
