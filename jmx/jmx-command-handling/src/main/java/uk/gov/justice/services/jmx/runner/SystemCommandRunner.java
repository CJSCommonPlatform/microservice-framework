package uk.gov.justice.services.jmx.runner;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.jmx.api.SystemCommandInvocationException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class SystemCommandRunner {

    @Inject
    private SystemCommandStore systemCommandStore;

    @Inject
    private Logger logger;

    @Transactional(NEVER)
    public void run(final SystemCommand systemCommand, final UUID commandId, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) throws SystemCommandInvocationException {

        final Optional<UUID> commandRuntimeId = jmxCommandRuntimeParameters.getCommandRuntimeId();
        if(commandRuntimeId.isPresent())  {
            logger.info(format("Running system command '%s' with %s '%s'", systemCommand.getName(), systemCommand.commandRuntimeIdType(), commandRuntimeId.get()));
        } else {
            logger.info(format("Running system command '%s'", systemCommand.getName()));
        }

        systemCommandStore.findCommandProxy(systemCommand).invokeCommand(systemCommand, commandId, jmxCommandRuntimeParameters);
    }
}
