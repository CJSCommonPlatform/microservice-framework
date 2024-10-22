package uk.gov.justice.framework.command.client.jmx;

import uk.gov.justice.framework.command.client.CommandLineException;
import uk.gov.justice.framework.command.client.io.ToConsolePrinter;
import uk.gov.justice.services.jmx.api.SystemCommandInvocationFailedException;
import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.mbean.CommandRunMode;
import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClient;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClientFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.UUID;

public class SystemCommandInvoker {

    private final SystemCommanderClientFactory systemCommanderClientFactory;
    private final CommandPoller commandPoller;
    private final ToConsolePrinter toConsolePrinter;

    public SystemCommandInvoker(
            final SystemCommanderClientFactory systemCommanderClientFactory,
            final CommandPoller commandPoller,
            final ToConsolePrinter toConsolePrinter) {
        this.systemCommanderClientFactory = systemCommanderClientFactory;
        this.commandPoller = commandPoller;
        this.toConsolePrinter = toConsolePrinter;
    }

    public void runSystemCommand(
            final String commandName,
            final JmxParameters jmxParameters,
            final JmxCommandRuntimeParameters jmxCommandRuntimeParameters,
            final CommandRunMode commandRunMode) {

        final String contextName = jmxParameters.getContextName();

        toConsolePrinter.printf("Running system command '%s'", commandName);
        toConsolePrinter.printf("Connecting to %s context at '%s' on port %d", contextName, jmxParameters.getHost(), jmxParameters.getPort());

        jmxParameters.getCredentials().ifPresent(credentials -> toConsolePrinter.printf("Connecting with credentials for user '%s'", credentials.getUsername()));

        try (final SystemCommanderClient systemCommanderClient = systemCommanderClientFactory.create(jmxParameters)) {

            toConsolePrinter.printf("Connected to %s context", contextName);

            final JmxCommandMBean jmxCommandMBean = systemCommanderClient.getRemote(contextName);
            final UUID commandId = jmxCommandMBean.call(commandName, jmxCommandRuntimeParameters, commandRunMode);
            toConsolePrinter.printf("System command '%s' with id '%s' successfully sent to %s", commandName, commandId, contextName);
            commandPoller.runUntilComplete(jmxCommandMBean, commandId, commandName);

        } catch (final UnrunnableSystemCommandException e) {
            toConsolePrinter.printf("The command '%s' is not supported on this %s context", commandName, contextName);
            throw e;
        }  catch (final CommandLineException e) {
            toConsolePrinter.printf("The command '%s' failed: %s", commandName, e.getMessage());
            throw e;
        } catch (final SystemCommandInvocationFailedException e) {
            toConsolePrinter.printf("The command '%s' failed: %s", commandName, e.getMessage());
            toConsolePrinter.println(e.getServerStackTrace());
            throw e;
        }
    }

}
