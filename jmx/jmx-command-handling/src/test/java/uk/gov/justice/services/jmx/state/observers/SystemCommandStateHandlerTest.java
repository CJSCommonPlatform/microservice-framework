package uk.gov.justice.services.jmx.state.observers;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.command.TestCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemCommandStateHandlerTest {

    @Mock
    private SystemCommandStateBean systemCommandStateBean;

    @InjectMocks
    private SystemCommandStateHandler systemCommandStateHandler;

    @Captor
    private ArgumentCaptor<SystemCommandStatus> systemCommandStatusCaptor;

    @Test
    public void shouldHandleSystemCommand() throws Exception {

        final UUID commandId = randomUUID();
        final TestCommand systemCommand = new TestCommand();
        final CommandState commandState = COMMAND_IN_PROGRESS;
        final ZonedDateTime statusChangedAt = new UtcClock().now();
        final String message = "message";

        final SystemCommandStateChangedEvent systemCommandStateChangedEvent = new SystemCommandStateChangedEvent(
                commandId,
                systemCommand,
                commandState,
                statusChangedAt,
                message
        );

        systemCommandStateHandler.handleSystemCommandStateChanged(systemCommandStateChangedEvent);

        verify(systemCommandStateBean).addSystemCommandState(systemCommandStatusCaptor.capture());

        final SystemCommandStatus systemCommandStatus = systemCommandStatusCaptor.getValue();

        assertThat(systemCommandStatus.getCommandId(), is(commandId));
        assertThat(systemCommandStatus.getSystemCommandName(), is(systemCommand.getName()));
        assertThat(systemCommandStatus.getCommandState(), is(commandState));
        assertThat(systemCommandStatus.getStatusChangedAt(), is(statusChangedAt));
        assertThat(systemCommandStatus.getMessage(), is(message));
    }
}
