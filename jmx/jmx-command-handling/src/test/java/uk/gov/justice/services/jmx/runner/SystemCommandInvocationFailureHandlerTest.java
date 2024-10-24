package uk.gov.justice.services.jmx.runner;

import static java.util.Optional.empty;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class SystemCommandInvocationFailureHandlerTest {

    private static final UUID commandId = fromString("a3b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b3b");

    @Mock
    private Logger logger;

    @Mock
    private Event<SystemCommandStateChangedEvent> stateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private SystemCommandInvocationFailureHandler systemCommandInvocationFailureHandler;

    @Test
    public void shouldLogErrorAndRaiseEventGivenCommandRuntimeId() {

        final RuntimeException e = new RuntimeException("exceptionMessage");
        final UUID commandRuntimeId = fromString("d3b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b3b");
        final String commandName = "PING";
        final String commandRuntimeIdType = "eventId";
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ZonedDateTime now = ZonedDateTime.now();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        when(systemCommand.getName()).thenReturn(commandName);
        when(systemCommand.commandRuntimeIdType()).thenReturn(commandRuntimeIdType);
        when(clock.now()).thenReturn(now);

        systemCommandInvocationFailureHandler.handle(e, systemCommand, commandId, jmxCommandRuntimeParameters);

        final String expectedErrorMessage = "Failed to run System Command 'PING' for eventId 'd3b3b3b3-3b3b-3b3b-3b3b-3b3b3b3b3b3b'";
        verify(logger).error(expectedErrorMessage, e);
        verify(stateChangedEventFirer).fire(new SystemCommandStateChangedEvent(
                commandId,
                systemCommand,
                COMMAND_FAILED,
                now,
                expectedErrorMessage + ". Caused by java.lang.RuntimeException: exceptionMessage"
        ));
    }

    @Test
    public void shouldLogErrorAndRaiseEventGivenNoCommandRuntimeId() {
        final RuntimeException e = new RuntimeException("exceptionMessage");
        final String commandName = "PING";
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ZonedDateTime now = new UtcClock().now();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(empty()));

        when(systemCommand.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(now);

        systemCommandInvocationFailureHandler.handle(e, systemCommand, commandId, jmxCommandRuntimeParameters);

        final String expectedErrorMessage = "Failed to run System Command 'PING'";
        verify(logger).error(expectedErrorMessage, e);
        verify(stateChangedEventFirer).fire(new SystemCommandStateChangedEvent(
                commandId,
                systemCommand,
                COMMAND_FAILED,
                now,
                expectedErrorMessage + ". Caused by java.lang.RuntimeException: exceptionMessage"
        ));
    }
}