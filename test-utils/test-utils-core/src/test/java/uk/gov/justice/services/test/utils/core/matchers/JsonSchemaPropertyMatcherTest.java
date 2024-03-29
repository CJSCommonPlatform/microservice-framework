package uk.gov.justice.services.test.utils.core.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaPropertyMatcher.hasProperty;
import static uk.gov.justice.services.test.utils.core.matchers.JsonSchemaPropertyMatcher.hasRequiredProperty;

import org.junit.jupiter.api.Test;

public class JsonSchemaPropertyMatcherTest {

    private static final String PATH_TO_SCHEMA = "json/schema/example.schema-property-matcher.json";

    @Test
    public void matchesOneLevelRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, hasRequiredProperty("urn"));
    }

    @Test
    public void doesntMatchOneLevelRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, not(hasRequiredProperty("anotherProperty")));
    }

    @Test
    public void doesntMatchNonExistingOneLevelRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, not(hasRequiredProperty("thisPropertyDoesNotExist")));
    }

    @Test
    public void matchesThreeLevelRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, hasRequiredProperty("schemaArray.schemaSubArray.subArrayId"));
    }

    @Test
    public void doesntMatchThreeLevelRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, not(hasRequiredProperty("schemaArray.schemaSubArray.subArrayProperty")));
    }

    @Test
    public void matchesRefRequiredProperty() {
        assertThat(PATH_TO_SCHEMA, hasRequiredProperty("address"));
    }

    @Test
    public void matchesRequiredPropertyInsideRef() {
        assertThat(PATH_TO_SCHEMA, hasRequiredProperty("address.postCode"));
    }

    @Test
    public void doesntMatchRequiredPropertyInsideRef() {
        assertThat(PATH_TO_SCHEMA, not(hasRequiredProperty("address.addressLine2")));
    }

    @Test
    public void invalidXpathShouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> assertThat(PATH_TO_SCHEMA, hasRequiredProperty("schemaArray.invalidProperty.subArrayId")));
    }

    @Test
    public void invalidFileShouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> assertThat("json/schema/invalid.file.json", hasRequiredProperty("urn")));
    }

    @Test
    public void matchesOneLevelProperty() {
        assertThat(PATH_TO_SCHEMA, hasProperty("anotherProperty"));
    }

    @Test
    public void doesntMatchOneLevelProperty() {
        assertThat(PATH_TO_SCHEMA, not(hasProperty("thisPropertyDoesNotExist")));
    }

    @Test
    public void matchesMultiLevelProperty() {
        assertThat(PATH_TO_SCHEMA, hasProperty("schemaArray.schemaSubArray.offence.wording"));
    }

    @Test
    public void doesntMatchMultiLevelProperty() {
        assertThat(PATH_TO_SCHEMA, not(hasProperty("schemaArray.schemaSubArray.offence.thisPropertyDoesNotExist")));
    }
}
