package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.SystemCommandInvocationException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class SystemCommandHandlerProxy {

    private final String commandName;
    private final Method method;
    private final Object instance;
    private final HandlerMethodValidator handlerMethodValidator;
    private final CommandHandlerMethodArgumentFactory commandHandlerMethodArgumentFactory;

    public SystemCommandHandlerProxy(
            final String commandName, final Method method,
            final Object instance,
            final HandlerMethodValidator handlerMethodValidator,
            final CommandHandlerMethodArgumentFactory commandHandlerMethodArgumentFactory) {
        this.commandName = commandName;
        this.method = method;
        this.instance = instance;
        this.handlerMethodValidator = handlerMethodValidator;
        this.commandHandlerMethodArgumentFactory = commandHandlerMethodArgumentFactory;
    }

    public String getCommandName() {
        return commandName;
    }

    public Object getInstance() {
        return instance;
    }

    public void invokeCommand(final SystemCommand systemCommand, final UUID commandId, final Optional<UUID> commandRuntimeId) throws SystemCommandInvocationException {

        handlerMethodValidator.checkHandlerMethodIsValid(method, instance, commandRuntimeId);

        final Object[] methodArguments = commandHandlerMethodArgumentFactory.createMethodArguments(
                systemCommand,
                commandId,
                commandRuntimeId);

        try {
            method.invoke(instance, methodArguments);
        } catch (final IllegalAccessException e) {

            final String message = format("Failed to call method '%s()' on %s. Is the method public?",
                    method.getName(),
                    instance.getClass().getName());
            throw new SystemCommandInvocationException(
                    message,
                    e
            );
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            final String message = format("%s thrown when calling method '%s()' on %s",
                    targetException.getClass().getSimpleName(),
                    method.getName(),
                    instance.getClass().getName());
            throw new SystemCommandInvocationException(
                    message,
                    targetException);
        }
    }
}
