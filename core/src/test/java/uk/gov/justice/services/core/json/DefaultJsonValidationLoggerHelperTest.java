package uk.gov.justice.services.core.json;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultJsonValidationLoggerHelperTest {

    private final static String SCHEMA_LOCATION_PATTERN = "/json/schema/%s.json";
    private final static String JSON_LOCATION_PATTERN = "/json/%s.json";

    private String validationTrace;
    private JsonSchemaValidationException jsonSchemaValidationException;

    @InjectMocks
    private JsonValidationLoggerHelper jsonValidationLoggerHelper = new DefaultJsonValidationLoggerHelper();

    @BeforeEach
    public void setup() throws IOException {
        try {
            schema().validate(badObject());
            fail("Test should have resulted in validation errors");
        } catch (ValidationException ex) {
            jsonSchemaValidationException = new JsonSchemaValidationException(ex.getMessage(), ex);

        }
        validationTrace = jsonValidationLoggerHelper.toValidationTrace(jsonSchemaValidationException);
    }

    @Test
    public void shouldHaveTopLevelMessage() throws IOException {
        with(validationTrace)
                .assertEquals("$.message", "#: 6 schema violations found")
                .assertEquals("$.violation", "#");
    }

    @Test
    public void shouldHaveTopLevelTitle() throws IOException {
        with(validationTrace).assertEquals("$.violatedSchema", "Title of a schema for testing - ");

    }

    @Test
    public void shouldHaveSubLevelTitle() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[1].violatedSchema", "Title of a schema for testing - ");
    }

    @Test
    public void shouldHaveDescription() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].violatedSchema",
                        "ingredients - List ingredients and quantities for recipe");
    }

    @Test
    public void shouldSpotMissingQuantity() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].causingExceptions[0].causingExceptions[0].message",
                        "#/ingredients/1: required key [quantity] not found");
    }

    @Test
    public void shouldSpotMissingRequiredKeys() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].message",
                        "#/ingredients/3: required key [name] not found");
    }

    @Test
    public void shouldSpotMissingExtraneousKeys() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].causingExceptions[0].causingExceptions[1].message",
                        "#/ingredients/1: extraneous key [Muntity] is not permitted")
                .assertEquals("$.causingExceptions[1].message",
                        "#: extraneous key [elephant] is not permitted");
    }

    @Test
    public void shouldHaveCorrectNumberOfNodesAtTopLevel() {
        assertThat(validationTrace, hasJsonPath("$.causingExceptions", hasSize(2)));
    }

    @Test
    public void shouldHaveCorrectNumberOfNodesAtSecondLevel() {
        assertThat(validationTrace, hasJsonPath("$.causingExceptions[0].causingExceptions", hasSize(3)));
    }

    @Test
    public void shouldSpotIncorrectTypes() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].causingExceptions[1].message",
                        "#/ingredients/2/quantity: expected type: Number, found: String")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[0].message",
                        "#/ingredients/3/quantity: expected type: Number, found: String");
    }

    @Test
    public void shouldPrintAllRequiredFields() throws IOException {
        with(validationTrace)
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].message",
                        "#/ingredients/3: required key [name] not found")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].violatedSchema",
                        "ingredient")
                .assertEquals("$.causingExceptions[0].causingExceptions[2].causingExceptions[1].violation",
                        "#/ingredients/3");

    }

    private Schema schema() throws IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream(format(SCHEMA_LOCATION_PATTERN, "fail-schema"));
        final JSONObject schemaJsonObject = new JSONObject(IOUtils.toString(inputStream, defaultCharset().name()));
        return SchemaLoader.load(schemaJsonObject);
    }

    private JSONObject badObject() throws IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream(format(JSON_LOCATION_PATTERN, "fail"));
        return new JSONObject(IOUtils.toString(inputStream, defaultCharset().name()));
    }
}
