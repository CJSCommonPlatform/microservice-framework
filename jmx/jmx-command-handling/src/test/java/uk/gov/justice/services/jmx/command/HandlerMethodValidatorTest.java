package uk.gov.justice.services.jmx.command;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import uk.gov.justice.services.jmx.api.InvalidHandlerMethodException;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HandlerMethodValidatorTest {
                                                 
    @InjectMocks
    private HandlerMethodValidator handlerMethodValidator;

    @Test
    public void givenNoCommandRuntimeId_shouldAcceptValidHandlerMethod() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        final Method validHandlerMethod = getMethod("validHandlerMethodWithOutRuntimeId", testCommandHandler.getClass());

        handlerMethodValidator.checkHandlerMethodIsValid(validHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
    }

    @Test
    public void givenNoCommandRuntimeId_shouldFailIfMethodNotPublic() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(),is(empty()));

        final Method invalidPrivateHandlerMethod = getMethod("invalidPrivateHandlerMethodWithOutRuntimeId", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidPrivateHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Handler method 'invalidPrivateHandlerMethodWithOutRuntimeId' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler' is not public."));
        }
    }

    @Test
    public void givenNoCommandRuntimeId_shouldFailIfMethodHasNoParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(),is(empty()));

        final Method invalidMissingParameterHandlerMethod = getMethod("invalidMissingParameterHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidMissingParameterHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidMissingParameterHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenNoCommandRuntimeId_shouldFailIfMethodHasTooManyParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(),is(empty()));

        final Method invalidTooManyParametersHandlerMethod = getMethod("invalidTooManyParametersHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidTooManyParametersHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidTooManyParametersHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenNoCommandRuntimeId_shouldFailIfMethodDoesNotHaveSystemCommandAsParameter() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(),is(empty()));

        final Method invalidNoSystemCommandHandlerMethod = getMethod("invalidNoSystemCommandHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoSystemCommandHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoSystemCommandHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenNoCommandRuntimeId_shouldFailIfMethodDoesNotHaveCommandIdAsParameter() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(),is(empty()));

        final Method invalidNoCommandIdMethod = getMethod("invalidNoCommandIdMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoCommandIdMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoCommandIdMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenCommandRuntimeId_shouldAcceptValidHandlerMethod() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method validHandlerMethod = getMethod("validHandlerMethodWithRuntimeId", testCommandHandler.getClass());

        handlerMethodValidator.checkHandlerMethodIsValid(validHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
    }

    @Test
    public void givenCommandRuntimeId_shouldFailIfMethodNotPublic() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method invalidPrivateHandlerMethod = getMethod("invalidPrivateHandlerMethodWithRuntimeId", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidPrivateHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Handler method 'invalidPrivateHandlerMethodWithRuntimeId' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler' is not public."));
        }
    }

    @Test
    public void givenCommandRuntimeId_shouldFailIfMethodHasNoParameters() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method invalidMissingParameterHandlerMethod = getMethod("invalidMissingParameterHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidMissingParameterHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidMissingParameterHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 3 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID' and third of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenCommandRuntimeId_shouldFailIfMethodHasTooManyParameters() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method invalidTooManyParametersHandlerMethod = getMethod("invalidTooManyParametersHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidTooManyParametersHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidTooManyParametersHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 3 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID' and third of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenCommandRuntimeId_shouldFailIfMethodDoesNotHaveSystemCommandAsParameter() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method invalidNoSystemCommandHandlerMethod = getMethod("invalidNoSystemCommandHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoSystemCommandHandlerMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoSystemCommandHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 3 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID' and third of type 'java.util.UUID'."));
        }
    }

    @Test
    public void givenCommandRuntimeId_shouldFailIfMethodDoesNotHaveCommandIdAsParameter() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final TestCommandHandler testCommandHandler = new TestCommandHandler();
        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        final Method invalidNoCommandIdMethod = getMethod("invalidNoCommandRuntimeIdMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoCommandIdMethod, testCommandHandler, jmxCommandRuntimeParameters);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoCommandRuntimeIdMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 3 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID' and third of type 'java.util.UUID'."));
        }
    }

    private Method getMethod(final String methodName, final Class<?> handlerClass) {

        for(final Method method: handlerClass.getDeclaredMethods()) {
            if(method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }
}
