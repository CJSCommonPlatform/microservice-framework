package uk.gov.justice.services.adapter.messaging;

public class JmsMessageHandlingException extends RuntimeException{

    public JmsMessageHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
