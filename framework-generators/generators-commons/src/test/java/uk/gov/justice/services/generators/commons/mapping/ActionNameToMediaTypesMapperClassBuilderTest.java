package uk.gov.justice.services.generators.commons.mapping;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.services.core.annotation.MediaTypesMapper;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypes;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings("ConstantConditions")
public class ActionNameToMediaTypesMapperClassBuilderTest {

    private static final String PACKAGE_NAME = "uk.gov.justice.services.generators.test.mappers";

    @TempDir
    public File outputFolder;

    private final RamlSchemaMappingClassNameGenerator ramlSchemaMappingClassNameGenerator = new RamlSchemaMappingClassNameGenerator();
    private final ActionNameToMediaTypesMapperClassBuilder actionNameToMediaTypesMapperClassBuilder = new ActionNameToMediaTypesMapperClassBuilder(ramlSchemaMappingClassNameGenerator);

    @Test
    public void shouldGenerateAMediaTypeToNameMapper() throws Exception {

        final String baseUri = "http://localhost:8080/test-command-api/command/api/rest/test";

        final MediaType requestType_1 = new MediaType("application/vnd.requestMediaType_1+json");
        final MediaType requestType_2 = new MediaType("application/vnd.requestMediaType_2+json");

        final MediaType responseType_1 = new MediaType("application/vnd.responseMediaType_1+json");

        final ActionNameMapping mapping_1 = new ActionNameMapping(
                "mapping_1",
                requestType_1,
                responseType_1);

        final ActionNameMapping mapping_2 = new ActionNameMapping(
                "mapping_2",
                requestType_2,
                null);

        final TypeSpec typeSpec = actionNameToMediaTypesMapperClassBuilder.generate(asList(mapping_1, mapping_2), baseUri);

        final Class<?> nameToMediaTypesMapperClass = writeSourceFileAndCompile(PACKAGE_NAME, typeSpec);

        assertThat(nameToMediaTypesMapperClass.getSimpleName(), is("TestCommandApiActionNameToMediaTypesMapper"));
        assertThat(nameToMediaTypesMapperClass.getAnnotation(MediaTypesMapper.class), is(notNullValue()));

        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper = (ActionNameToMediaTypesMapper) nameToMediaTypesMapperClass.newInstance();

        final Map<String, MediaTypes> actionNameToMediaTypesMap = actionNameToMediaTypesMapper.getActionNameToMediaTypesMap();

        assertThat(actionNameToMediaTypesMap.size(), is(2));

        final MediaTypes mediaTypes_1 = actionNameToMediaTypesMap.get("mapping_1");
        assertThat(mediaTypes_1.getRequestMediaType(), is(of(requestType_1)));
        assertThat(mediaTypes_1.getResponseMediaType(), is(of(responseType_1)));

        final MediaTypes mediaTypes_2 = actionNameToMediaTypesMap.get("mapping_2");
        assertThat(mediaTypes_2.getRequestMediaType(), is(of(requestType_2)));
        assertThat(mediaTypes_2.getResponseMediaType(), is(empty()));
    }

    private Class<?> writeSourceFileAndCompile(final String packageName, final TypeSpec typeSpec) throws IOException {

        final File outputFolderRoot = outputFolder.getParentFile();

        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(outputFolderRoot);

        return javaCompilerUtil()
                .compiledClassesOf(outputFolderRoot, outputFolderRoot, packageName)
                .stream()
                .filter(clazz -> !clazz.getName().equals("java.lang.Object"))
                .findFirst().orElseGet(null);

    }
}
