package uk.gov.justice.services.test.utils.core.matchers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonValueNullMatcher.isJsonValueNull;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.Test;

public class JsonEnvelopeMatcherTest {

    private static final UUID ID = randomUUID();
    private static final String NAME = "someName";

    @Test
    public void shouldMatchJsonEnvelope() throws Exception {
        assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.action"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                )));
    }

    @Test
    public void shouldMatchJsonEnvelopeWithSchema() throws Exception {
        assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope().thatMatchesSchema());
    }

    @Test
    public void shouldMatchMetadataIfPayloadIsJsonValueNull() throws Exception {
        assertThat(jsonEnvelopeWithJsonValueNullPayload(), JsonEnvelopeMatcher.jsonEnvelope()
                .withMetadataOf(metadata().withName("event.action")));
    }

    @Test
    public void shouldMatchPayloadIfPayloadIsJsonValueNull() throws Exception {
        assertThat(jsonEnvelopeWithJsonValueNullPayload(), JsonEnvelopeMatcher.jsonEnvelope()
                .withPayloadOf(payload(isJsonValueNull())));
    }

    @Test
    public void shouldNotMatchJsonEnvelopeWithJsonObject() throws Exception {
        assertThrows(AssertionError.class, () -> assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope()
                .withPayloadOf(payload(isJsonValueNull()))));
    }

    @Test
    public void shouldNotMatchJsonEnvelopeWithSchema() throws Exception {
        final JsonEnvelope invalidJsonEnvelope = envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(ID.toString(), "someId")
                .build();

        assertThrows(AssertionError.class, () -> assertThat(invalidJsonEnvelope, JsonEnvelopeMatcher.jsonEnvelope().thatMatchesSchema()));
    }

    @Test
    public void shouldFailToMatchDifferentMetadata() throws Exception {
        assertThrows(AssertionError.class, () -> assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.not.match"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(ID.toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                ))));
    }

    @Test
    public void shouldFailToMatchDifferentPayload() throws Exception {
        assertThrows(AssertionError.class, () -> assertThat(jsonEnvelope(), JsonEnvelopeMatcher.jsonEnvelope(
                metadata().withName("event.action"),
                payloadIsJson(allOf(
                        withJsonPath("$.someId", equalTo(randomUUID().toString())),
                        withJsonPath("$.name", equalTo(NAME)))
                ))));
    }

    private JsonEnvelope jsonEnvelope() {
        return envelope()
                .with(metadataWithRandomUUID("event.action"))
                .withPayloadOf(ID.toString(), "someId")
                .withPayloadOf(NAME, "name")
                .build();
    }

    private JsonEnvelope jsonEnvelopeWithJsonValueNullPayload() {
        return envelopeFrom(metadataWithRandomUUID("event.action").build(), JsonValue.NULL);
    }
}