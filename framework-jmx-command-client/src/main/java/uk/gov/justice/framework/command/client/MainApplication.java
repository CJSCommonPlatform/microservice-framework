package uk.gov.justice.framework.command.client;

import static uk.gov.justice.framework.command.client.ReturnCode.SUCCESS;

import uk.gov.justice.framework.command.client.cdi.producers.OptionsFactory;
import uk.gov.justice.framework.command.client.jmx.ListCommandsInvoker;
import uk.gov.justice.framework.command.client.startup.CommandLineArgumentParser;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

public class MainApplication {

    private final CommandLineArgumentParser commandLineArgumentParser;
    private final JmxParametersFactory jmxParametersFactory;
    private final ListCommandsInvoker listCommandsInvoker;
    private final OptionsFactory optionsFactory;
    private final HelpFormatter formatter;
    private final CommandExecutor commandExecutor;
    private final ReturnCodeFactory returnCodeFactory;

    public MainApplication(
            final CommandLineArgumentParser commandLineArgumentParser,
            final JmxParametersFactory jmxParametersFactory,
            final ListCommandsInvoker listCommandsInvoker,
            final OptionsFactory optionsFactory,
            final HelpFormatter formatter,
            final CommandExecutor commandExecutor,
            final ReturnCodeFactory returnCodeFactory) {

        this.commandLineArgumentParser = commandLineArgumentParser;
        this.jmxParametersFactory = jmxParametersFactory;
        this.listCommandsInvoker = listCommandsInvoker;
        this.optionsFactory = optionsFactory;
        this.formatter = formatter;
        this.commandExecutor = commandExecutor;
        this.returnCodeFactory = returnCodeFactory;
    }


    public ReturnCode run(final String[] args) {

        final Optional<CommandLine> commandLineOptional = commandLineArgumentParser.parse(args);

        if (commandLineOptional.isPresent()) {

            final CommandLine commandLine = commandLineOptional.get();

            final JmxParameters jmxParameters = jmxParametersFactory.createFrom(commandLine);

            try {

                final Optional<List<SystemCommandDetails>> systemCommandsOptional = listCommandsInvoker.listSystemCommands(jmxParameters);
                systemCommandsOptional.ifPresent(systemCommands -> commandExecutor.executeCommand(commandLine, jmxParameters, systemCommands));

            } catch (final RuntimeException e) {
                return returnCodeFactory.createFor(e);
            }

        } else {
            formatter.printHelp("java -jar framework-jmx-command-client-<version>.jar", optionsFactory.createOptions());
        }

        return SUCCESS;
    }
}
