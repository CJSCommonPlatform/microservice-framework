package uk.gov.justice.services.messaging.spi;

import static uk.gov.justice.services.common.converter.JSONObjectValueObfuscator.obfuscated;
import static uk.gov.justice.services.messaging.JsonEnvelopeWriter.writeJsonObject;
import static uk.gov.justice.services.messaging.JsonMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonMetadata.SOURCE;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.List;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Default implementation of an envelope.
 */
public class DefaultJsonEnvelope implements JsonEnvelope {

    private Metadata metadata;

    private JsonValue payload;

    DefaultJsonEnvelope(final Metadata metadata, final JsonValue payload) {
        this.metadata = metadata;
        this.payload = payload;
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public JsonValue payload() {
        return payload;
    }

    @Override
    public JsonObject payloadAsJsonObject() {
        return (JsonObject) payload;
    }

    @Override
    public JsonArray payloadAsJsonArray() {
        return (JsonArray) payload;
    }

    @Override
    public JsonNumber payloadAsJsonNumber() {
        return (JsonNumber) payload;
    }

    @Override
    public JsonString payloadAsJsonString() {
        return (JsonString) payload;
    }

    @Override
    public JsonObject asJsonObject() {
        return createObjectBuilder(payloadAsJsonObject())
                .add(METADATA, metadata().asJsonObject()).build();
    }

    @Override
    public String toString() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        if (metadata != null) {
            builder.add("id", String.valueOf(metadata.id()))
                    .add("name", metadata.name());


            metadata.clientCorrelationId().ifPresent(s -> builder.add(CORRELATION, s));
            metadata.sessionId().ifPresent(s -> builder.add(SESSION_ID, s));
            metadata.userId().ifPresent(s -> builder.add(USER_ID, s));
            metadata.source().ifPresent(s -> builder.add(SOURCE, s));

            final JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

            final List<UUID> causes = metadata.causation();

            if (causes != null) {
                metadata.causation().forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));
            }
            builder.add("causation", causationBuilder);
        }
        return builder.build().toString();
    }

    @Override
    public String toDebugStringPrettyPrint() {
        return writeJsonObject(asJsonObject());
    }

    @Override
    public String toObfuscatedDebugString() {
        return writeJsonObject(createObjectBuilder((JsonObject) obfuscated(payloadAsJsonObject()))
                .add(METADATA, metadata.asJsonObject())
                .build());
    }

}
