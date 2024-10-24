package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import java.io.Closeable;
import java.io.IOException;

import javax.management.remote.JMXConnector;


public class SystemCommanderClient implements Closeable {

    private final JMXConnector jmxConnector;
    private final MBeanConnector mBeanConnector;

    public SystemCommanderClient(final JMXConnector jmxConnector, final MBeanConnector mBeanConnector) {
        this.jmxConnector = jmxConnector;
        this.mBeanConnector = mBeanConnector;
    }

    public JmxCommandMBean getRemote(final String contextName) {

        return mBeanConnector.connect(
                contextName,
                JmxCommandMBean.class,
                jmxConnector
        );
    }

    @Override
    public void close() {

        try {
            jmxConnector.close();
        } catch (IOException ignored) {
        }
    }
}
