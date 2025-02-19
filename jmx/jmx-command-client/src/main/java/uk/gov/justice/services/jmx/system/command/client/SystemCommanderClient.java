package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import java.io.Closeable;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;


public class SystemCommanderClient implements Closeable {

    private final JMXConnector jmxConnector;
    private final MBeanConnector mBeanConnector;

    public SystemCommanderClient(final JMXConnector jmxConnector, final MBeanConnector mBeanConnector) {
        this.jmxConnector = jmxConnector;
        this.mBeanConnector = mBeanConnector;
    }

    public SystemCommanderMBean getRemote(final String contextName) {

        return mBeanConnector.connect(
                contextName,
                SystemCommanderMBean.class,
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
