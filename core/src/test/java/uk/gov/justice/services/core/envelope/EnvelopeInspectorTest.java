package uk.gov.justice.services.core.envelope;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EnvelopeInspectorTest {


    @InjectMocks
    private EnvelopeInspector envelopeInspector;

    @Test
    public void shouldGetTheMetadataOfAnEnvelope() throws Exception {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(jsonEnvelope.metadata()).thenReturn(metadata);

        assertThat(envelopeInspector.getMetadataFor(jsonEnvelope), is(metadata));
    }

    @Test
    public void shouldThrowValidationExceptionIfTheJsonEnvelopeContainsNoMetadata() throws Exception {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(jsonEnvelope.metadata()).thenReturn(null);

        try {
            envelopeInspector.getMetadataFor(jsonEnvelope);
            fail();
        } catch (final EnvelopeValidationException expected) {
            assertThat(expected.getMessage(), is("Metadata not set in the envelope."));
        }
    }

    @Test
    public void shouldGetTheActionNameOfAnEnvelope() throws Exception {

        final String actionName = "example.an-action";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);

        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(actionName);

        assertThat(envelopeInspector.getActionNameFor(jsonEnvelope), is(actionName));
    }
}
