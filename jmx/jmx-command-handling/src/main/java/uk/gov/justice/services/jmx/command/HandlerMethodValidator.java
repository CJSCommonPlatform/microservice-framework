package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;

import uk.gov.justice.services.jmx.api.InvalidHandlerMethodException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class HandlerMethodValidator {


    public void checkHandlerMethodIsValid(final Method handlerMethod, final Object instance, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) throws InvalidHandlerMethodException {

        checkMethodPublic(handlerMethod, instance);
        checkMethodParameters(handlerMethod, instance, jmxCommandRuntimeParameters);
    }

    private void checkMethodPublic(final Method handlerMethod, final Object instance) throws InvalidHandlerMethodException {

        if (!isPublic(handlerMethod.getModifiers())) {
            throw new InvalidHandlerMethodException(format("Handler method '%s' on class '%s' is not public.", handlerMethod.getName(), instance.getClass().getName()));
        }
    }

    private void checkMethodParameters(final Method handlerMethod, final Object instance, final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) throws InvalidHandlerMethodException {
        final Class<?>[] parameterTypes = handlerMethod.getParameterTypes();

        if (jmxCommandRuntimeParameters.getCommandRuntimeId().isPresent()) {
            if (parameterTypes.length != 3) {
                throw new InvalidHandlerMethodException(format("Invalid handler method '%s' on class '%s'. Method should have 3 parameters. First of type '%s' and second of type '%s' and third of type '%s'.",
                        handlerMethod.getName(),
                        instance.getClass().getName(),
                        SystemCommand.class.getName(),
                        UUID.class.getName(),
                        UUID.class.getName()));
            }
        } else {
            if (parameterTypes.length != 2) {
                throw new InvalidHandlerMethodException(format("Invalid handler method '%s' on class '%s'. Method should have 2 parameters. First of type '%s' and second of type '%s'.",
                        handlerMethod.getName(),
                        instance.getClass().getName(),
                        SystemCommand.class.getName(),
                        UUID.class.getName()));
            }
        }

        if (!SystemCommand.class.isAssignableFrom(parameterTypes[0])) {
            throw new InvalidHandlerMethodException(format("Invalid handler method '%s' on class '%s'. Method should have first parameter of type '%s'.",
                    handlerMethod.getName(),
                    instance.getClass().getName(),
                    SystemCommand.class.getName()));
        }
    }
}
