package uk.gov.justice.services.jmx.api.mbean;

import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.util.List;
import java.util.UUID;

public interface SystemCommanderMBean {

    UUID call(final String systemCommandName, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters);
    UUID call(final String systemCommandName, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters, final CommandRunMode commandRunMode);
    List<SystemCommandDetails> listCommands();
    SystemCommandStatus getCommandStatus(final UUID commandId);
}
