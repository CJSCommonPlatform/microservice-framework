package uk.gov.justice.subscription.jms.it;

import uk.gov.justice.services.core.error.JsonEnvelopeProcessingFailureHandler;
import uk.gov.justice.services.messaging.JsonEnvelope;

public class DummyJsonEnvelopeProcessingFailureHandler implements JsonEnvelopeProcessingFailureHandler {

    @Override
    public void onJsonEnvelopeProcessingFailure(final JsonEnvelope jsonEnvelope, final Throwable exception) {

    }
}
