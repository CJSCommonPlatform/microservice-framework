package uk.gov.justice.services.jmx.system.command.client;

import uk.gov.justice.services.jmx.api.name.CommandMBeanNameProvider;
import uk.gov.justice.services.jmx.api.name.ObjectNameFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.ConnectorWrapper;
import uk.gov.justice.services.jmx.system.command.client.connection.EnvironmentFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JMXConnectorFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxUrlFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;
import uk.gov.justice.services.jmx.system.command.client.connection.RemoteMBeanFactory;

public class ConnectorObjectFactory {

    public MBeanConnector mBeanConnector() {
        return new MBeanConnector(
                commandMBeanNameProvider(),
                remoteMBeanFactory());
    }
    public JMXConnectorFactory jmxConnectorFactory() {
        return new JMXConnectorFactory(
                jmxUrlFactory(),
                connectorWrapper(),
                environmentFactory()
        );
    }

    private JmxUrlFactory jmxUrlFactory() {
        return new JmxUrlFactory();
    }

    private ConnectorWrapper connectorWrapper() {
        return new ConnectorWrapper();
    }

    private EnvironmentFactory environmentFactory() {
        return new EnvironmentFactory();
    }

    private CommandMBeanNameProvider commandMBeanNameProvider() {
        return new CommandMBeanNameProvider(new ObjectNameFactory());
    }

    private RemoteMBeanFactory remoteMBeanFactory() {
        return new RemoteMBeanFactory();
    }
}
