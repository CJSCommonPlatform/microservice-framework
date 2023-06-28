package uk.gov.justice.services.messaging;


import static jakarta.json.Json.createWriterFactory;
import static jakarta.json.stream.JsonGenerator.PRETTY_PRINTING;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;


/**
 * Writer class to pretty print Json.
 */
public class JsonEnvelopeWriter {

    private static final JsonWriterFactory writerFactory;

    static {
        final Map<String, Object> properties = new HashMap<>(1);
        properties.put(PRETTY_PRINTING, true);
        writerFactory = createWriterFactory(properties);
    }

    public static String writeJsonObject(final JsonObject json) {
        final StringWriter stringWriter = new StringWriter();

        try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.writeObject(json);
            return stringWriter.toString();
        }
    }

    private JsonEnvelopeWriter() {

    }
}
