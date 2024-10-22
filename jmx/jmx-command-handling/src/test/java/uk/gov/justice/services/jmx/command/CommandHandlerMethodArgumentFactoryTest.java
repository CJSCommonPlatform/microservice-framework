package uk.gov.justice.services.jmx.command;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandHandlerMethodArgumentFactoryTest {


    @InjectMocks
    private CommandHandlerMethodArgumentFactory commandHandlerMethodArgumentFactory;

    @Test
    public void shouldCreateArrayOfTwoArgumentsIfCommandRuntimeIdIsEmpty() throws Exception {

        final SystemCommandWithoutCommandId systemCommandWithoutCommandId = new SystemCommandWithoutCommandId();
        final UUID commandId = randomUUID();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        final Object[] methodArguments = commandHandlerMethodArgumentFactory.createMethodArguments(
                systemCommandWithoutCommandId,
                commandId,
                jmxCommandRuntimeParameters);

        assertThat(methodArguments.length, is(2));
        assertThat(methodArguments[0], is(systemCommandWithoutCommandId));
        assertThat(methodArguments[1], is(commandId));
    }

    @Test
    public void shouldCreateArrayOfThreeArgumentsIfCommandRuntimeIdIsPresent() throws Exception {

        final SystemCommandWithCommandId systemCommandWithCommandId = new SystemCommandWithCommandId();
        final UUID commandId = randomUUID();
        final UUID commandRuntimeId = randomUUID();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Object[] methodArguments = commandHandlerMethodArgumentFactory.createMethodArguments(
                systemCommandWithCommandId,
                commandId,
                jmxCommandRuntimeParameters);

        assertThat(methodArguments.length, is(3));
        assertThat(methodArguments[0], is(systemCommandWithCommandId));
        assertThat(methodArguments[1], is(commandId));
        assertThat(methodArguments[2], is(commandRuntimeId));
    }

    private static class SystemCommandWithoutCommandId extends BaseSystemCommand {

        SystemCommandWithoutCommandId() {
            super("COMMAND_WITHOUT_COMMAND_ID", "Dummy command without commandRuntimeId");
        }
    }
    private static class SystemCommandWithCommandId extends BaseSystemCommand {

        SystemCommandWithCommandId() {
            super("COMMAND_WITH_COMMAND_ID", "Dummy command with commandRuntimeId");
        }

        @Override
        public boolean requiresCommandRuntimeId() {
            return true;
        }

        @Override
        public String commandRuntimeIdType() {
            return "some-uuid";
        }
    }
}