package uk.gov.justice.framework.command.client;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.mbean.CommandRunMode.GUARDED;

import uk.gov.justice.framework.command.client.io.CommandPrinter;
import uk.gov.justice.framework.command.client.jmx.SystemCommandInvoker;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.api.mbean.CommandRunMode;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {

    @Mock
    private SystemCommandInvoker systemCommandInvoker;

    @Mock
    private JmxRuntimeParametersFactory jmxRuntimeParametersFactory;

    @Mock
    private CommandPrinter commandPrinter;

    @Mock
    private CommandRunModeSelector commandRunModeSelector;

    @InjectMocks
    private CommandExecutor commandExecutor;

    @Test
    public void shouldLookupSystemCommandByNameAndExecute() throws Exception {

        final String commandName = "CATCHUP";
        final CommandRunMode commandRunMode = GUARDED;

        final SystemCommandDetails systemCommandDetails_1 = mock(SystemCommandDetails.class);
        final SystemCommandDetails systemCommandDetails_2 = mock(SystemCommandDetails.class);

        final CommandLine commandLine = mock(CommandLine.class);
        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = mock(JmxCommandRuntimeParameters.class);
        final List<SystemCommandDetails> systemCommands = asList(systemCommandDetails_1, systemCommandDetails_2);

        when(commandLine.hasOption("list")).thenReturn(false);
        when(commandLine.getOptionValue("command")).thenReturn(commandName);
        when(jmxRuntimeParametersFactory.createFrom(commandLine)).thenReturn(jmxCommandRuntimeParameters);
        when(commandRunModeSelector.selectCommandRunMode(commandLine)).thenReturn(commandRunMode);

        commandExecutor.executeCommand(commandLine, jmxParameters, systemCommands);

        verify(systemCommandInvoker).runSystemCommand(commandName, jmxParameters, jmxCommandRuntimeParameters, commandRunMode);
    }

    @Test
    public void shouldListSystemCommandsIfCommandLineOptionIsList() throws Exception {

        final SystemCommandDetails systemCommandDetails_1 = mock(SystemCommandDetails.class);
        final SystemCommandDetails systemCommandDetails_2 = mock(SystemCommandDetails.class);

        final CommandLine commandLine = mock(CommandLine.class);
        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final List<SystemCommandDetails> systemCommands = asList(systemCommandDetails_1, systemCommandDetails_2);

        when(commandLine.hasOption("list")).thenReturn(true);

        commandExecutor.executeCommand(commandLine, jmxParameters, systemCommands);

        verify(commandPrinter).printSystemCommands(systemCommands);
        verifyNoInteractions(systemCommandInvoker);
    }
}
