package uk.gov.justice.services.jmx.command;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.jmx.api.InvalidHandlerMethodException;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodValidatorTest {
                                                 
    @InjectMocks
    private HandlerMethodValidator handlerMethodValidator;

    @Test
    public void shouldAcceptValidHandlerMethod() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method validHandlerMethod = getMethod("validHandlerMethod", testCommandHandler.getClass());

        handlerMethodValidator.checkHandlerMethodIsValid(validHandlerMethod, testCommandHandler);
    }

    @Test
    public void shouldFailIfMethodNotPublic() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidPrivateHandlerMethod = getMethod("invalidPrivateHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidPrivateHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Handler method 'invalidPrivateHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler' is not public."));
        }
    }

    @Test
    public void shouldFailIfMethodHasNoParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidMissingParameterHandlerMethod = getMethod("invalidMissingParameterHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidMissingParameterHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidMissingParameterHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void shouldFailIfMethodHasTooManyParameters() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidTooManyParametersHandlerMethod = getMethod("invalidTooManyParametersHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidTooManyParametersHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidTooManyParametersHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void shouldFailIfMethodDoesNotHaveSystemCommandAsParameter() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidNoSystemCommandHandlerMethod = getMethod("invalidNoSystemCommandHandlerMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoSystemCommandHandlerMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoSystemCommandHandlerMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
        }
    }

    @Test
    public void shouldFailIfMethodDoesNotHaveCommandIdAsParameter() throws Exception {

        final TestCommandHandler testCommandHandler = new TestCommandHandler();

        final Method invalidNoCommandIdMethod = getMethod("invalidNoCommandIdMethod", testCommandHandler.getClass());

        try {
            handlerMethodValidator.checkHandlerMethodIsValid(invalidNoCommandIdMethod, testCommandHandler);
            fail();
        } catch (final InvalidHandlerMethodException expected) {
            assertThat(expected.getMessage(), is("Invalid handler method 'invalidNoCommandIdMethod' on class 'uk.gov.justice.services.jmx.command.TestCommandHandler'. Method should have 2 parameters. First of type 'uk.gov.justice.services.jmx.api.command.SystemCommand' and second of type 'java.util.UUID'."));
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
