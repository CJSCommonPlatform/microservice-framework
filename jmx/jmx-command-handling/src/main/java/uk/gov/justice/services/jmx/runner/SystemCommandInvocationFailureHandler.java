package uk.gov.justice.services.jmx.runner;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommandInvocationFailureHandler {

    @Inject
    private Logger logger;

    @Inject
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Inject
    private UtcClock clock;

    public void handle(final Exception e, final SystemCommand systemCommand,
                       final UUID commandId,
                       final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) {
        final StringBuilder message = new StringBuilder(format("Failed to run System Command '%s'", systemCommand.getName()));

        final Optional<UUID> commandRuntimeId = jmxCommandRuntimeParameters.getCommandRuntimeId();
        commandRuntimeId.ifPresent(commandRuntimeUuid -> message.append(format(" for %s '%s'", systemCommand.commandRuntimeIdType(), commandRuntimeUuid)));

        logger.error(message.toString(), e);

        final String errorMessage = message.append(". Caused by ")
                .append(e.getClass().getName())
                .append(": ")
                .append(e.getMessage())
                .toString();

        stateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                systemCommand,
                COMMAND_FAILED,
                clock.now(),
                errorMessage
        ));
    }
}
