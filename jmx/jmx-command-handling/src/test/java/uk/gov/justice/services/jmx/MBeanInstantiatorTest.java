package uk.gov.justice.services.jmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.mbean.DefaultJmxCommandMBean;
import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameException;
import uk.gov.justice.services.jmx.util.ContextNameProvider;

import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class MBeanInstantiatorTest {

    @Mock
    private MBeanServer mbeanServer;

    @Mock
    private DefaultJmxCommandMBean systemCommander;

    @Mock
    private ContextNameProvider contextNameProvider;

    @Mock
    private CommandMBeanNameProvider commandMBeanNameProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private MBeanInstantiator mBeanInstantiator;

    @Test
    public void shouldRegisterSystemCommanderMBean() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer).registerMBean(systemCommander, objectName);
        verify(logger).info("Registering JMX mBean class 'DefaultJmxCommandMBean' using name 'mBeanName'");
    }

    @Test
    public void shouldNotRegisterSystemCommanderMBeanIfAlreadyRegistered() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        when(mbeanServer.isRegistered(objectName)).thenReturn(true);

        mBeanInstantiator.registerSystemCommanderMBean();

        verify(mbeanServer, never()).registerMBean(systemCommander, objectName);
        verifyNoInteractions(logger);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanRegisteringIncorrect() throws Exception {

        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));
        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        doThrow(mBeanRegistrationException).when(mbeanServer).registerMBean(systemCommander, objectName);

        try {
            mBeanInstantiator.registerSystemCommanderMBean();
            fail();
        } catch (final ObjectNameException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("Failed to register SystemCommander MBean using object name 'AnObjectName'"));
        }
    }

    @Test
    public void shouldUnregisterMBeans() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer).unregisterMBean(objectName);
        verify(logger).info("Unregistering JMX MBean class 'DefaultJmxCommandMBean' using name 'mBeanName'");
    }

    @Test
    public void shouldNotUnregisterMBeansIfNotAlreadyRegistered() throws Exception {

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "mBeanName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(false);
        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);

        mBeanInstantiator.unregisterMBeans();

        verify(mbeanServer, never()).unregisterMBean(objectName);
        verifyNoInteractions(logger);
    }

    @Test
    public void shouldThrowExceptionWhenMBeanUnregisteringIncorrect() throws Exception {
        final MBeanRegistrationException mBeanRegistrationException = new MBeanRegistrationException(new NullPointerException("Ooops"));

        final String contextName = "my-context";
        final ObjectName objectName = mock(ObjectName.class, "AnObjectName");

        when(mbeanServer.isRegistered(objectName)).thenReturn(true);
        when(contextNameProvider.getContextName()).thenReturn(contextName);
        when(commandMBeanNameProvider.create(contextName)).thenReturn(objectName);
        doThrow(mBeanRegistrationException).when(mbeanServer).unregisterMBean(objectName);

        try {
            mBeanInstantiator.unregisterMBeans();
            fail();
        } catch (final ObjectNameException expected) {
            assertThat(expected.getCause(), is(mBeanRegistrationException));
            assertThat(expected.getMessage(), is("Failed to unregister MBean with object name 'AnObjectName'"));
        }
    }
}
