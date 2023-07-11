package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JsonEnvelopeRepackerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertJsonValueEnvelopeToJsonEnvelope() throws JsonProcessingException {
        final Envelope<JsonValue> envelope = mock(Envelope.class);
        final JsonEnvelopeRepacker jsonEnvelopeRepacker = new JsonEnvelopeRepacker();
        final JsonEnvelope jsonEnvelope = jsonEnvelopeRepacker.repack(envelope);
        assertThat(jsonEnvelope, isA(JsonEnvelope.class));
    }

    @Test
    public void shouldHandleJsonEnvelope() throws JsonProcessingException {
        final Envelope<JsonValue> envelope = mock(JsonEnvelope.class);
        final JsonEnvelopeRepacker jsonEnvelopeRepacker = new JsonEnvelopeRepacker();
        final JsonEnvelope jsonEnvelope = jsonEnvelopeRepacker.repack(envelope);
        assertThat(envelope, is(sameInstance(jsonEnvelope)));
    }
}