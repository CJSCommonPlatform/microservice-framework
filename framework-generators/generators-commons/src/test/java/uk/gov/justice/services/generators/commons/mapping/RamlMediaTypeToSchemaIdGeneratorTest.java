package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.VALID_JSON_SCHEMA;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.raml.model.MimeType;

public class RamlMediaTypeToSchemaIdGeneratorTest {

    private static final MediaType MEDIA_TYPE_1 = new MediaType("application/vnd.ctx.command.command1+json");
    private static final String BASE_PACKAGE = "org.raml.test";
    private static final JavaCompilerUtility COMPILER = javaCompilerUtil();

    @TempDir
    public File outputFolder;

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithPost() throws Exception {
        final String schemaId = "http://justice.gov.uk/test/schema.json";
        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString(), schemaId);

        new RamlMediaTypeToSchemaIdGenerator().generateMediaTypeToSchemaIdMapper(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(POST)
                                        .withMediaTypeWithDefaultSchema(mimeType_1)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> schemaIdMapperClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "mapper",
                "WarnameMediaTypeToSchemaIdMapper");

        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(1));
        assertThat(mediaTypeToSchemaIdMap.get(MEDIA_TYPE_1), is(schemaId));
    }

    @Test
    public void shouldCreateMediaTypeToSchemaIdMapperForGivenRamlWithGet() throws Exception {
        final String schemaId = "http://justice.gov.uk/test/schema.json";
        final MimeType mimeType_1 = createMimeTypeWith(MEDIA_TYPE_1.toString(), schemaId);

        new RamlMediaTypeToSchemaIdGenerator().generateMediaTypeToSchemaIdMapper(
                restRamlWithQueryApiDefaults()
                        .with(resource("/user")
                                .with(httpAction(GET)
                                        .withResponseTypes(mimeType_1)
                                )
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> schemaIdMapperClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "mapper",
                "WarnameMediaTypeToSchemaIdMapper");

        final MediaTypeToSchemaIdMapper instance = (MediaTypeToSchemaIdMapper) schemaIdMapperClass.newInstance();

        final Map<MediaType, String> mediaTypeToSchemaIdMap = instance.getMediaTypeToSchemaIdMap();

        assertThat(mediaTypeToSchemaIdMap.size(), is(1));
        assertThat(mediaTypeToSchemaIdMap.get(MEDIA_TYPE_1), is(schemaId));
    }

    private MimeType createMimeTypeWith(final String type, final String schemaId) {
        final MimeType mimeType = new MimeType();
        mimeType.setType(type);
        mimeType.setSchema(format(VALID_JSON_SCHEMA, schemaId));

        return mimeType;
    }
}
