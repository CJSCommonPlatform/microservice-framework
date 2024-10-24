package uk.gov.justice.services.jmx.bootstrap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class DefaultJmxCommandMBeanBootstrapObjectFactoryTest {

    private final JmxCommandBootstrapObjectFactory jmxCommandBootstrapObjectFactory = new JmxCommandBootstrapObjectFactory();

    @Test
    public void shouldCreateCdiInstanceResolver() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.cdiInstanceResolver(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandHandlerProxyFactory() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.systemCommandHandlerProxyFactory(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandProxyResolver() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.systemCommandProxyResolver(), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandScanner() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.systemCommandScanner(), is(notNullValue()));
    }

    @Test
    public void shouldCreateHandlerMethodValidator() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.handlerMethodValidator(), is(notNullValue()));
    }

    @Test
    public void shouldCreateBlacklistedCommandsScanner() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.blacklistedCommandsScanner(), is(notNullValue()));
    }

    @Test
    public void shouldCreateBlacklistedCommandsFilter() throws Exception {
        assertThat(jmxCommandBootstrapObjectFactory.blacklistedCommandsFilter(), is(notNullValue()));
    }
}
