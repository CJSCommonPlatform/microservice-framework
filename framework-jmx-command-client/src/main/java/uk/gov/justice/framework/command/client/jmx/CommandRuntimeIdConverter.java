package uk.gov.justice.framework.command.client.jmx;

import static java.lang.String.format;
import static java.util.UUID.fromString;

import uk.gov.justice.framework.command.client.CommandLineException;

import java.util.UUID;

public class CommandRuntimeIdConverter {

    public UUID asUuid(final String commandRuntimeId) {
        try{
            return fromString(commandRuntimeId);
        } catch(final IllegalArgumentException e) {
            throw new CommandLineException(format("'--commandRuntimeId' switch '%s' is not a valid uuid", commandRuntimeId), e);
        }
    }
}
