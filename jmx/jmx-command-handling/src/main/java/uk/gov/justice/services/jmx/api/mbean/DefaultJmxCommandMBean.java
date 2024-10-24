package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.jmx.api.mbean.CommandRunMode.FORCED;

import uk.gov.justice.services.jmx.api.CommandNotFoundException;
import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.command.CommandConverter;
import uk.gov.justice.services.jmx.command.SystemCommandLocator;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class DefaultJmxCommandMBean implements JmxCommandMBean {

    @Inject
    private SystemCommandLocator systemCommandLocator;

    @Inject
    private AsynchronousCommandRunner asynchronousCommandRunner;

    @Inject
    private SystemCommandScanner systemCommandScanner;

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Inject
    private CommandConverter commandConverter;

    @Inject
    private JmxCommandVerifier jmxCommandVerifier;

    @Inject
    private Logger logger;

    @Override
    public UUID call(final String systemCommandName, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) {
        logger.info(format("Received System Command '%s'", systemCommandName));
        logger.info(format("Running '%s' in '%s' mode", systemCommandName, FORCED));

        return doCall(systemCommandName, jmxCommandRuntimeParameters, FORCED);
    }

    @Override
    public UUID call(final String systemCommandName, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters, final CommandRunMode commandRunMode) {
        logger.info(format("Received System Command '%s'", systemCommandName));
        logger.info(format("Running '%s' in '%s' mode", systemCommandName, commandRunMode));

        return doCall(systemCommandName, jmxCommandRuntimeParameters, commandRunMode);
    }

    @Override
    public List<SystemCommandDetails> listCommands() {
        return systemCommandScanner.findCommands().stream()
                .map(commandConverter::toCommandDetails)
                .collect(toList());
    }

    @Override
    public SystemCommandStatus getCommandStatus(final UUID commandId) {

        return systemCommandStateBean
                .getCommandStatus(commandId)
                .orElseThrow(() -> new CommandNotFoundException(format("No SystemCommand found with id %s", commandId)));
    }

    private UUID doCall(final String systemCommandName, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters, final CommandRunMode commandRunMode) {
        final SystemCommand systemCommand = systemCommandLocator
                .forName(systemCommandName)
                .orElseThrow(() -> new UnrunnableSystemCommandException(format("The system command '%s' is not supported on this context.", systemCommandName)));

        jmxCommandVerifier.verify(systemCommand, jmxCommandRuntimeParameters);

        if (commandRunMode.isGuarded()) {
            if (systemCommandStateBean.commandInProgress(systemCommand)) {
                throw new UnrunnableSystemCommandException(format("Cannot run system command '%s'. A previous call to that command is still in progress.", systemCommand.getName()));
            }
        }

        return asynchronousCommandRunner.run(systemCommand, jmxCommandRuntimeParameters);
    }
}
