package uk.gov.justice.services.jmx.runner;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

public class RunSystemCommandTask implements Callable<Boolean> {

    private final SystemCommandRunner systemCommandRunner;
    private final SystemCommand systemCommand;
    private final UUID commandId;
    private final JmxCommandRuntimeParameters jmxCommandRuntimeParameters;
    private final SystemCommandInvocationFailureHandler systemCommandInvocationFailureHandler;

    public RunSystemCommandTask(
            final SystemCommandRunner systemCommandRunner,
            final SystemCommand systemCommand,
            final UUID commandId,
            final JmxCommandRuntimeParameters jmxCommandRuntimeParameters,
            final SystemCommandInvocationFailureHandler systemCommandInvocationFailureHandler) {
        this.systemCommandRunner = systemCommandRunner;
        this.systemCommand = systemCommand;
        this.commandId = commandId;
        this.jmxCommandRuntimeParameters = jmxCommandRuntimeParameters;
        this.systemCommandInvocationFailureHandler = systemCommandInvocationFailureHandler;
    }

    @Override
    public Boolean call() {
        try {
            systemCommandRunner.run(systemCommand, commandId, jmxCommandRuntimeParameters);
        } catch (final Exception e) {
            systemCommandInvocationFailureHandler.handle(e, systemCommand, commandId, jmxCommandRuntimeParameters);
        }

        return true;
    }
}
