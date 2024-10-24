package uk.gov.justice.framework.command.client;

import uk.gov.justice.framework.command.client.jmx.CommandRuntimeIdConverter;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;

import org.apache.commons.cli.CommandLine;

public class JmxRuntimeParametersFactory {

    private final CommandRuntimeIdConverter commandRuntimeIdConverter;

    public JmxRuntimeParametersFactory(final CommandRuntimeIdConverter commandRuntimeIdConverter) {
        this.commandRuntimeIdConverter = commandRuntimeIdConverter;
    }

    public JmxCommandRuntimeParameters createFrom(final CommandLine commandLine) {

        final String commandRuntimeId = commandLine.getOptionValue("crid");
        final String commandRuntimeString = commandLine.getOptionValue("crs");

        final JmxCommandRuntimeParametersBuilder jmxCommandRuntimeParametersBuilder = new JmxCommandRuntimeParametersBuilder();

        if (commandRuntimeId != null) {
            jmxCommandRuntimeParametersBuilder.withCommandRuntimeId(commandRuntimeIdConverter.asUuid(commandRuntimeId));
        }

        if (commandRuntimeString != null) {
            jmxCommandRuntimeParametersBuilder.withCommandRuntimeString(commandRuntimeString);
        }

        return jmxCommandRuntimeParametersBuilder
                .build();
    }
}
