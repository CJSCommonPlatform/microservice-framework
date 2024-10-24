package uk.gov.justice.services.jmx.system.command.client.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;
import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameFactory;
import uk.gov.justice.services.jmx.system.command.client.MBeanClientConnectionException;
import uk.gov.justice.services.jmx.system.command.client.MBeanClientException;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MBeanConnectorTest {

    @Mock
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Mock
    private RemoteMBeanFactory remoteMBeanFactory;

    @InjectMocks
    private MBeanConnector mBeanConnector;

    @Test
    public void shouldConnectToARemoteInstanceOfTheJmxBean() throws Exception {

        final String contextName = "my-context";
        final Class<JmxCommandMBean> mBeanInterface = JmxCommandMBean.class;

        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final ObjectName objectName = mock(ObjectName.class);
        final MBeanServerConnection connection = mock(MBeanServerConnection.class);
        final JmxCommandMBean jmxCommandMBean = mock(JmxCommandMBean.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(connection);
        when(connection.isRegistered(objectName)).thenReturn(true);
        when(remoteMBeanFactory.createRemote(connection, objectName, mBeanInterface)).thenReturn(jmxCommandMBean);

        assertThat(mBeanConnector.connect(contextName, mBeanInterface, jmxConnector), is(jmxCommandMBean));
    }

    @Test
    public void shouldThrowExceptionIfConnectingToTheMBeanFails() throws Exception {

        final IOException ioException = new IOException("Ooops");

        final String contextName = "my-context";
        final Class<JmxCommandMBean> mBeanInterface = JmxCommandMBean.class;

        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final ObjectName objectName = mock(ObjectName.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(jmxConnector.getMBeanServerConnection()).thenThrow(ioException);

        try {
            mBeanConnector.connect(contextName, mBeanInterface, jmxConnector);
            fail();
        } catch (final MBeanClientConnectionException expected) {
            assertThat(expected.getCause(), is(ioException));
            assertThat(expected.getMessage(), is("Failed to get remote connection to MBean 'JmxCommandMBean'"));
        }
    }

    @Test
    public void shouldThrowExceptionIfMBeanNameNotCorrect() throws Exception {

        final String contextName = "people";
        final ObjectName realObjectName = createARealObjectName(contextName);

        final Class<JmxCommandMBean> mBeanInterface = JmxCommandMBean.class;
        final JMXConnector jmxConnector =  mock(JMXConnector.class);
        final MBeanServerConnection connection = mock(MBeanServerConnection.class);

        when(commandMBeanNameProvider.create(contextName)).thenReturn(realObjectName);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(connection);
        when(connection.isRegistered(realObjectName)).thenReturn(false);


        try {
            mBeanConnector.connect(contextName, mBeanInterface, jmxConnector);
            fail();
        } catch (final MBeanClientException expected) {
            assertThat(expected.getMessage(), is("No JMX bean found with name 'people-system-command-handler-mbean'. Is your context name of 'people' correct?"));
        }
    }

    private ObjectName createARealObjectName(final String contextName) {
        final CommandMBeanNameProvider commandMBeanNameProvider = new CommandMBeanNameProvider(new ObjectNameFactory());
        return commandMBeanNameProvider.create(contextName);
    }
}
