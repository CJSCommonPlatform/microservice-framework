package uk.gov.justice.framework.command.client.jmx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.framework.command.client.io.ToConsolePrinter;
import uk.gov.justice.framework.command.client.util.Sleeper;
import uk.gov.justice.framework.command.client.util.UtcClock;
import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandPollerTest {

    @Mock
    private CommandChecker commandChecker;

    @Mock
    private UtcClock clock;

    @Mock
    private Sleeper sleeper;

    @Mock
    private ToConsolePrinter toConsolePrinter;

    @InjectMocks
    private CommandPoller commandPoller;

    @Test
    public void shouldCheckCommandUntilComplete() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final String commandName = "CATCHUP";

        final ZonedDateTime startTime = new UtcClock().now();

        final JmxCommandMBean jmxCommandMBean = mock(JmxCommandMBean.class);

        when(clock.now()).thenReturn(startTime);
        when(commandChecker.commandComplete(jmxCommandMBean, commandId, startTime)).thenReturn(false, false, true);

        commandPoller.runUntilComplete(jmxCommandMBean, commandId, commandName);

        verify(sleeper, times(2)).sleepFor(1_000);
        verifyNoInteractions(toConsolePrinter);
    }

    @Test
    public void shouldLogProgressEveryTenthCall() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final String commandName = "CATCHUP";

        final ZonedDateTime startTime = new UtcClock().now();
        final ZonedDateTime now = startTime.plusSeconds(10);

        final JmxCommandMBean jmxCommandMBean = mock(JmxCommandMBean.class);

        when(clock.now()).thenReturn(startTime, now);
        when(commandChecker.commandComplete(jmxCommandMBean, commandId, startTime)).thenReturn(false, false, false, false, false, false, false, false, false, false, true);

        commandPoller.runUntilComplete(jmxCommandMBean, commandId, commandName);

        verify(sleeper, times(10)).sleepFor(1_000);
        verify(toConsolePrinter).println("CATCHUP running for 10 seconds");
    }
}
