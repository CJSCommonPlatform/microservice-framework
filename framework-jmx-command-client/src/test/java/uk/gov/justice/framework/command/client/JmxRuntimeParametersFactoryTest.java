package uk.gov.justice.framework.command.client;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.framework.command.client.jmx.CommandRuntimeIdConverter;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JmxRuntimeParametersFactoryTest {

    @Spy
    private CommandRuntimeIdConverter commandRuntimeIdConverter = new CommandRuntimeIdConverter();

    @InjectMocks
    private JmxRuntimeParametersFactory jmxRuntimeParametersFactory;

    @Test
    public void shouldBuildJmxCommandRuntimeParametersFromCommandLine() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final String commandRuntimeString = "some-command-runtime-string";

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("crid")).thenReturn(commandRuntimeId.toString());
        when(commandLine.getOptionValue("crs")).thenReturn(commandRuntimeString);

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = jmxRuntimeParametersFactory
                .createFrom(commandLine);

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(of(commandRuntimeId)));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(of(commandRuntimeString)));
    }

    @Test
    public void shouldHandleMissingCommandRuntimeId() throws Exception {
        final String commandRuntimeString = "some-command-runtime-string";

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("crid")).thenReturn(null);
        when(commandLine.getOptionValue("crs")).thenReturn(commandRuntimeString);

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = jmxRuntimeParametersFactory
                .createFrom(commandLine);

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(empty()));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(of(commandRuntimeString)));

    }

    @Test
    public void shouldHandleMissingCommandRuntimeString() throws Exception {

        final UUID commandRuntimeId = randomUUID();

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue("crid")).thenReturn(commandRuntimeId.toString());

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = jmxRuntimeParametersFactory
                .createFrom(commandLine);

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(of(commandRuntimeId)));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(empty()));
    }
}