package uk.gov.justice.services.messaging.spi;

public class IncompatibleJsonPayloadTypeException extends RuntimeException{

    public IncompatibleJsonPayloadTypeException(final String message) {
        super(message);
    }
}
