package uk.gov.justice.framework.command.client.startup;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.framework.command.client.CommandLineException;
import uk.gov.justice.framework.command.client.cdi.producers.OptionsFactory;
import uk.gov.justice.framework.command.client.io.ToConsolePrinter;

import java.util.Optional;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class CommandLineArgumentParser {

    private final ToConsolePrinter toConsolePrinter;
    private final OptionsFactory optionsFactory;
    private final BasicParser basicParser;

    public CommandLineArgumentParser(
            final ToConsolePrinter toConsolePrinter,
            final OptionsFactory optionsFactory,
            final BasicParser basicParser) {
        this.toConsolePrinter = toConsolePrinter;
        this.optionsFactory = optionsFactory;
        this.basicParser = basicParser;
    }

    public Optional<CommandLine> parse(final String[] args) {

        try {
            final CommandLine commandLine = basicParser.parse(optionsFactory.createOptions(), args);

            if (commandLine.hasOption("command") || commandLine.hasOption("list")) {
                return of(commandLine);
            }

            toConsolePrinter.println("No system command specified.");

            return empty();

        } catch (final ParseException e) {
            throw new CommandLineException(format("Failed to parse command line args '%s'", asList(args)), e);
        }

    }

}
