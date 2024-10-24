package uk.gov.justice.framework.command.client.startup;

import uk.gov.justice.framework.command.client.CommandExecutor;
import uk.gov.justice.framework.command.client.CommandRunModeSelector;
import uk.gov.justice.framework.command.client.JmxParametersFactory;
import uk.gov.justice.framework.command.client.JmxRuntimeParametersFactory;
import uk.gov.justice.framework.command.client.MainApplication;
import uk.gov.justice.framework.command.client.ReturnCodeFactory;
import uk.gov.justice.framework.command.client.cdi.producers.OptionsFactory;
import uk.gov.justice.framework.command.client.io.CommandPrinter;
import uk.gov.justice.framework.command.client.io.ToConsolePrinter;
import uk.gov.justice.framework.command.client.jmx.CommandChecker;
import uk.gov.justice.framework.command.client.jmx.CommandPoller;
import uk.gov.justice.framework.command.client.jmx.CommandRuntimeIdConverter;
import uk.gov.justice.framework.command.client.jmx.ListCommandsInvoker;
import uk.gov.justice.framework.command.client.jmx.SystemCommandInvoker;
import uk.gov.justice.framework.command.client.util.Sleeper;
import uk.gov.justice.framework.command.client.util.UtcClock;
import uk.gov.justice.services.jmx.system.command.client.ConnectorObjectFactory;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClientFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;

public class ObjectFactory {

    private final ConnectorObjectFactory connectorObjectFactory;

    public ObjectFactory() {
        this.connectorObjectFactory = new ConnectorObjectFactory();
    }

    public MainApplication mainApplication() {

        return new MainApplication(
                commandLineArgumentParser(),
                jmxParametersFactory(),
                listCommandsInvoker(),
                optionsFactory(),
                helpFormatter(),
                commandExecutor(),
                returnCodeFactory()
        );
    }

    public CommandLineArgumentParser commandLineArgumentParser() {
        return new CommandLineArgumentParser(toConsolePrinter(), optionsFactory(), basicParser());
    }

    public JmxParametersFactory jmxParametersFactory() {
        return new JmxParametersFactory();
    }

    public ListCommandsInvoker listCommandsInvoker() {
        return new ListCommandsInvoker(systemCommanderClientFactory(), toConsolePrinter());
    }

    public OptionsFactory optionsFactory() {
        return new OptionsFactory();
    }

    public CommandRuntimeIdConverter uuidConverter() {
        return new CommandRuntimeIdConverter();
    }

    public JmxRuntimeParametersFactory jmxRuntimeParametersFactory() {
        return new JmxRuntimeParametersFactory(uuidConverter());
    }

    public CommandExecutor commandExecutor() {
        return new CommandExecutor(
                systemCommandInvoker(),
                commandPrinter(),
                commandRunModeSelector(),
                jmxRuntimeParametersFactory());
    }

    public HelpFormatter helpFormatter() {
        return new HelpFormatter();
    }

    public ReturnCodeFactory returnCodeFactory() {
        return new ReturnCodeFactory(toConsolePrinter());
    }

    public ToConsolePrinter toConsolePrinter() {
        return new ToConsolePrinter();
    }

    public SystemCommanderClientFactory systemCommanderClientFactory() {
        return new SystemCommanderClientFactory(
                connectorObjectFactory.mBeanConnector(),
                connectorObjectFactory.jmxConnectorFactory()
        );
    }

    public BasicParser basicParser() {
        return new BasicParser();
    }

    public SystemCommandInvoker systemCommandInvoker() {
        return new SystemCommandInvoker(
                systemCommanderClientFactory(),
                commandPoller(),
                toConsolePrinter()
        );
    }

    public CommandPoller commandPoller() {
        return new CommandPoller(commandChecker(), utcClock(), sleeper(), toConsolePrinter());
    }

    public CommandChecker commandChecker() {
        return new CommandChecker(toConsolePrinter(), utcClock());
    }

    public UtcClock utcClock() {
        return new UtcClock();
    }

    public Sleeper sleeper() {
        return new Sleeper();
    }

    public CommandPrinter commandPrinter() {
        return new CommandPrinter(toConsolePrinter());
    }

    public CommandRunModeSelector commandRunModeSelector() {
        return new CommandRunModeSelector();
    }
}
