package uk.gov.justice.services.messaging;

import static jakarta.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import jakarta.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JsonEnvelopeWriterTest {

    private static final String EXPECTED_JSON =
            "{\n" +
                    "    \"aProperty\": \"value a\",\n" +
                    "    \"bProperty\": \"value b\",\n" +
                    "    \"cProperty\": \"value c\",\n" +
                    "    \"anObject\": {\n" +
                    "        \"innerProperty\": \"innerValue\"\n" +
                    "    }\n" +
                    "}";

    @Test
    public void shouldWriteAJsonObjectAsAPrettyPrintedString() throws Exception {

        final JsonObject jsonObject = createObjectBuilder()
                .add("aProperty", "value a")
                .add("bProperty", "value b")
                .add("cProperty", "value c")
                .add("anObject", createObjectBuilder()
                        .add("innerProperty", "innerValue"))
                .build();

        final String json = JsonEnvelopeWriter.writeJsonObject(jsonObject);

        assertThat(json, is(EXPECTED_JSON));
    }
}
