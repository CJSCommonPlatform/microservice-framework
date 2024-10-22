package uk.gov.justice.services.jmx.api.mbean;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class JmxCommandVerifierTest {

    private final JmxCommandVerifier jmxCommandVerifier = new JmxCommandVerifier();

    @Test
    public void shouldVerifySuccessfullyCommandThatRequiresRuntimeId() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final JmxCommandWithRuntimeId jmxCommandWithRuntimeId = new JmxCommandWithRuntimeId();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        jmxCommandVerifier.verify(jmxCommandWithRuntimeId, jmxCommandRuntimeParameters);
    }

    @Test
    public void shouldFailIfCommandThatRequiresRuntimeIdHasMissingRuntimeId() throws Exception {

        final JmxCommandWithRuntimeId jmxCommandWithRuntimeId = new JmxCommandWithRuntimeId();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString().isEmpty(), is(true));

        final UnrunnableSystemCommandException unrunnableSystemCommandException = assertThrows(
                UnrunnableSystemCommandException.class,
                () -> jmxCommandVerifier.verify(jmxCommandWithRuntimeId, jmxCommandRuntimeParameters));

        assertThat(unrunnableSystemCommandException.getMessage(), is("The JMX command 'JMX_COMMAND_WITH_RUNTIME_ID' requires 'eventId' to be able to run. Please re-run using the '--command-runtime-id' switch"));
    }

    @Test
    public void shouldVerifySuccessfullyCommandThatRequiresRuntimeString() throws Exception {

        final String commandRuntimeString = "some-command-string";
        final JmxCommandWithRuntimeString jmxCommandWithRuntimeString = new JmxCommandWithRuntimeString();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeString(commandRuntimeString)
                .build();

        jmxCommandVerifier.verify(jmxCommandWithRuntimeString, jmxCommandRuntimeParameters);
    }

    @Test
    public void shouldFailIfCommandThatRequiresRuntimeStingHasMissingRuntimeString() throws Exception {

        final JmxCommandWithRuntimeString jmxCommandWithRuntimeString = new JmxCommandWithRuntimeString();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString().isEmpty(), is(true));

        final UnrunnableSystemCommandException unrunnableSystemCommandException = assertThrows(
                UnrunnableSystemCommandException.class,
                () -> jmxCommandVerifier.verify(jmxCommandWithRuntimeString, jmxCommandRuntimeParameters));

        assertThat(unrunnableSystemCommandException.getMessage(), is("The JMX command 'JMX_COMMAND_WITH_RUNTIME_STRING' requires 'aggregateClass' to be able to run. Please re-run using the '--command-runtime-string' switch"));
    }

    @Test
    public void shouldVerifySuccessfullyCommandThatDoesNotRequireRuntimeIdNorRuntimeString() throws Exception {

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString().isEmpty(), is(true));

        final JmxCommandWithoutRuntimeId jmxCommandWithoutRuntimeId = new JmxCommandWithoutRuntimeId();

        jmxCommandVerifier.verify(jmxCommandWithoutRuntimeId, jmxCommandRuntimeParameters);
    }

    private static class JmxCommandWithRuntimeId implements SystemCommand {

        @Override
        public String getName() {
            return "JMX_COMMAND_WITH_RUNTIME_ID";
        }

        @Override
        public String getDescription() {
            return "JmxCommandWithRuntimeId. Dumb class for testing";
        }

        @Override
        public boolean requiresCommandRuntimeId() {
            return true;
        }

        @Override
        public String commandRuntimeIdType() {
            return "eventId";
        }
    }

    private static class JmxCommandWithoutRuntimeId implements SystemCommand {

        @Override
        public String getName() {
            return "JMX_COMMAND_WITHOUT_RUNTIME_ID";
        }

        @Override
        public String getDescription() {
            return "JmxCommandWithoutRuntimeId. Dumb class for testing";
        }
    }

    private static class JmxCommandWithRuntimeString implements SystemCommand {

        @Override
        public String getName() {
            return "JMX_COMMAND_WITH_RUNTIME_STRING";
        }

        @Override
        public String getDescription() {
            return "JmxCommandWithRuntimeString. Dumb class for testing";
        }

        @Override
        public boolean requiresCommandRuntimeString() {
            return true;
        }

        @Override
        public String commandRuntimeStringType() {
            return "aggregateClass";
        }
    }
}