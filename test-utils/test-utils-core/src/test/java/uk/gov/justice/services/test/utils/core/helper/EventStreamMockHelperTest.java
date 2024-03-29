package uk.gov.justice.services.test.utils.core.helper;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class EventStreamMockHelperTest {

    @Test
    public void shouldVerifyAppendCallAndReturnStreamOfJsonEnvelopes() throws Exception {
        final EventStream eventStream = mock(EventStream.class);
        final JsonEnvelope jsonEnvelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope jsonEnvelope_2 = mock(JsonEnvelope.class);

        eventStream.append(Stream.of(jsonEnvelope_1, jsonEnvelope_2));

        final Stream<JsonEnvelope> jsonEnvelopeStream = EventStreamMockHelper.verifyAppendAndGetArgumentFrom(eventStream);

        final List<JsonEnvelope> jsonEnvelopes = jsonEnvelopeStream.collect(toList());
        assertThat(jsonEnvelopes, contains(jsonEnvelope_1, jsonEnvelope_2));
    }
}