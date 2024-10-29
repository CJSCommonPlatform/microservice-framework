package uk.gov.justice.services.jmx.system.command.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.mbean.CommandRunMode.GUARDED;
import static uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.withNoCommandParameters;
import static uk.gov.justice.services.management.suspension.commands.SuspendCommand.SUSPEND;
import static uk.gov.justice.services.management.suspension.commands.UnsuspendCommand.UNSUSPEND;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.system.command.client.connection.Credentials;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FrameworkSystemCommandCallerTest {

    @Mock
    private TestSystemCommanderClientFactory testSystemCommanderClientFactory;

    @Test
    public void shouldCallShutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = mock(JmxCommandRuntimeParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(
                jmxParameters,
                jmxCommandRuntimeParameters,
                testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        frameworkSystemCommandCaller.callShutter();

        verify(systemCommanderMBean).call(SUSPEND, jmxCommandRuntimeParameters, GUARDED);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallUnshutter() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = mock(JmxCommandRuntimeParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(
                jmxParameters,
                jmxCommandRuntimeParameters,
                testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        frameworkSystemCommandCaller.callUnshutter();

        verify(systemCommanderMBean).call(UNSUSPEND, jmxCommandRuntimeParameters, GUARDED);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCreateWithCorrectDefaultParametersIfInstantiatingUsingTheContextName() throws Exception {

        final String contextName = "contextName";
        final FrameworkSystemCommandCaller frameworkSystemCommandCaller = new FrameworkSystemCommandCaller(
                contextName,
                withNoCommandParameters());

        final JmxParameters jmxParameters = getValueOfField(frameworkSystemCommandCaller, "jmxParameters", JmxParameters.class);

        assertThat(jmxParameters.getContextName(), is(contextName));
        assertThat(jmxParameters.getHost(), is(getHost()));
        assertThat(jmxParameters.getPort(), is(9990));

        final Optional<Credentials> credentials = jmxParameters.getCredentials();

        if (credentials.isPresent()) {
            assertThat(credentials.get().getUsername(), is("admin"));
            assertThat(credentials.get().getPassword(), is("admin"));
        } else {
            fail();
        }
    }
}
