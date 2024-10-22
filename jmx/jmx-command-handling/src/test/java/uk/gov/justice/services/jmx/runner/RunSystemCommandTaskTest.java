package uk.gov.justice.services.jmx.runner;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RunSystemCommandTaskTest {

    @Mock
    private SystemCommandRunner systemCommandRunner;

    @Mock
    private SystemCommandInvocationFailureHandler systemCommandInvocationFailureHandler;

    @Test
    public void shouldRunTheSystemCommand() throws Exception {

        final UUID commandId = randomUUID();

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = mock(JmxCommandRuntimeParameters.class);

        final RunSystemCommandTask runSystemCommandTask = new RunSystemCommandTask(
                systemCommandRunner,
                systemCommand,
                commandId,
                jmxCommandRuntimeParameters,
                systemCommandInvocationFailureHandler);

        runSystemCommandTask.call();

        verify(systemCommandRunner).run(systemCommand, commandId, jmxCommandRuntimeParameters);
    }

    @Test
    public void onFailureShouldInvokeExceptionHandler() throws Exception {
        final RuntimeException exception = new RuntimeException();
        final UUID commandId = randomUUID();

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = mock(JmxCommandRuntimeParameters.class);

        final RunSystemCommandTask runSystemCommandTask = new RunSystemCommandTask(
                systemCommandRunner,
                systemCommand,
                commandId,
                jmxCommandRuntimeParameters,
                systemCommandInvocationFailureHandler);

        doThrow(exception).when(systemCommandRunner).run(systemCommand, commandId, jmxCommandRuntimeParameters);

        runSystemCommandTask.call();

        verify(systemCommandInvocationFailureHandler).handle(exception, systemCommand, commandId, jmxCommandRuntimeParameters);
    }
}
