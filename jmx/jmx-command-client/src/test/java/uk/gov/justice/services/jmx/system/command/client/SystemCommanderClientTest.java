package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import javax.management.remote.JMXConnector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemCommanderClientTest {

    @Mock
    private JMXConnector jmxConnector;

    @Mock
    private MBeanConnector mBeanConnector;

    @InjectMocks
    private SystemCommanderClient systemCommanderClient;

    @Test
    public void shouldGetTheRemoteSystemCommanderMBean() throws Exception {

        final String contextName = "my-context";
        final JmxCommandMBean jmxCommandMBean = mock(JmxCommandMBean.class);

        when(mBeanConnector.connect(
                contextName,
                JmxCommandMBean.class,
                jmxConnector)).thenReturn(jmxCommandMBean);

        assertThat(systemCommanderClient.getRemote(contextName), is(jmxCommandMBean));
    }

    @Test
    public void shouldCloseTheJmxConnectorOnTryWithResources() throws Exception {

        try(final SystemCommanderClient anotherSystemCommanderClient = new SystemCommanderClient(jmxConnector, mBeanConnector)) {
            assertThat(anotherSystemCommanderClient, is(notNullValue()));
        }

        verify(jmxConnector).close();
    }
}
