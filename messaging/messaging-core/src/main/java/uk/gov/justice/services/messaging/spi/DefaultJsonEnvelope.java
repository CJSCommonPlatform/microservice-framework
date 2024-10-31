package uk.gov.justice.services.messaging.spi;

import static java.util.Objects.requireNonNullElse;
import static javax.json.JsonValue.EMPTY_JSON_ARRAY;
import static javax.json.JsonValue.EMPTY_JSON_OBJECT;
import static javax.json.JsonValue.NULL;
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
import java.util.Objects;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Default implementation of an envelope.
 */
public class DefaultJsonEnvelope implements JsonEnvelope {

    private final Metadata metadata;
    private final JsonValue payload;

    DefaultJsonEnvelope(final Metadata metadata, final JsonValue payload) {
        // set payload to JsonValue.NULL if payload is null
        this.payload = requireNonNullElse(payload, NULL);
        this.metadata = metadata;
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

        if (payloadIsNull()) {
            throw new IncompatibleJsonPayloadTypeException(
                    "The payload of this JsonEnvelope is set to JsonValue.NULL which is not a JsonObject. " +
                            "Please call the method 'payload()' instead if your payload is expected to be null. " +
                            "To check for null payloads please call the method 'payloadIsNull()'."
            );
        }

        return (JsonObject) payload;
    }

    @Override
    public JsonArray payloadAsJsonArray() {

        if (payloadIsNull()) {
            throw new IncompatibleJsonPayloadTypeException(
                    "The payload of this JsonEnvelope is set to JsonValue.NULL which is not a JsonArray. " +
                            "Please call the method 'payload()' instead if your payload is expected to be null. " +
                            "To check for null payloads please call the method 'payloadIsNull()'."
            );
        }

        
        return (JsonArray) payload;
    }

    @Override
    public boolean payloadIsNull() {
        return NULL.equals(payload);
    }

    @Override
    public JsonNumber payloadAsJsonNumber() {

        if (payloadIsNull()) {
            throw new IncompatibleJsonPayloadTypeException(
                    "The payload of this JsonEnvelope is set to JsonValue.NULL which is not a JsonNumber. " +
                            "Please call the method 'payload()' instead if your payload is expected to be null. " +
                            "To check for null payloads please call the method 'payloadIsNull()'."
            );
        }

        return (JsonNumber) payload;
    }

    @Override
    public JsonString payloadAsJsonString() {

        if (payloadIsNull()) {
            throw new IncompatibleJsonPayloadTypeException(
                    "The payload of this JsonEnvelope is set to JsonValue.NULL which is not a JsonString. " +
                            "Please call the method 'payload()' instead if your payload is expected to be null. " +
                            "To check for null payloads please call the method 'payloadIsNull()'."
            );
        }

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
