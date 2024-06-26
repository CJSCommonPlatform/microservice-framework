package uk.gov.justice.services.adapters.rest.generator;


import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithQueryApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.defaultPostResource;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.services.adapter.rest.application.CommonProviders;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.raml.model.Raml;

public class RestAdapterGenerator_ApplicationTest extends BaseRestAdapterGeneratorTest {

    private static final String EXISTING_FILE_PATH = "org/raml/test/resource/DefaultQueryApiPathAResource.java";

    @Test
    public void shouldGenerateApplicationClass() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(defaultPostResource()).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        assertThat(applicationClass.isInterface(), is(false));
    }

    @Test
    public void shouldGenerateApplicationClassIfServiceContainsHyphens() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service-with-hyphens")
                        .with(defaultPostResource()).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceWithHyphensApplication");

        assertThat(applicationClass.isInterface(), is(false));
    }

    @Test
    public void shouldGenerateNonFinalPublicApplicationClass() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(defaultPostResource()).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        assertThat(Modifier.isFinal(applicationClass.getModifiers()), is(false));
        assertThat(Modifier.isPublic(applicationClass.getModifiers()), is(true));
    }

    @Test
    public void shouldGenerateExtendingApplicationClass() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(defaultPostResource()
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        assertThat(applicationClass.getSuperclass(), equalTo(Application.class));
    }

    @Test
    public void shouldGenerateAnnotatedApplicationClass() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(defaultPostResource()
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        assertThat(applicationClass.getAnnotation(ApplicationPath.class), not(nullValue()));
        assertThat(applicationClass.getAnnotation(ApplicationPath.class).value(), is("/command/api/rest/service"));

    }

    @Test
    public void shouldGenerateApplicationClassWithGetClassesMethod() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(defaultPostResource()).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> applicationClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        final Method method = applicationClass.getDeclaredMethod("getClasses");
        assertThat(method, not(nullValue()));
        assertThat(method.getParameterCount(), equalTo(0));
        assertThat(isPublic(method.getModifiers()), is(true));
        assertThat(isStatic(method.getModifiers()), is(false));
        assertThat(isAbstract(method.getModifiers()), is(false));
        assertThat(method.getGenericReturnType(), equalTo(new TypeToken<Set<Class<?>>>() {
        }.getType()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnSetOfResourceClasses() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("http://localhost:8080/warname/command/api/rest/service")
                        .with(resource("/pathA").with(httpActionWithDefaultMapping(GET).withDefaultResponseType()))
                        .with(resource("/pathB").with(httpActionWithDefaultMapping(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Set<Class<?>> compiledClasses = COMPILER.compiledClassesOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE);

        final Class<?> applicationClass = COMPILER.classOf(
                compiledClasses,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        final Object application = applicationClass.newInstance();

        final CommonProviders commonProviders = mock(CommonProviders.class);
        when(commonProviders.providers()).thenReturn(newHashSet(JaxRsProviderA.class));

        setField(application, "commonProviders", commonProviders);

        final Method method = applicationClass.getDeclaredMethod("getClasses");
        final Object result = method.invoke(application);
        assertThat(result, is(instanceOf(Set.class)));
        final Set<Class<?>> classes = (Set<Class<?>>) result;
        assertThat(classes, hasItems(
                COMPILER.classOf(compiledClasses, BASE_PACKAGE, "resource", "DefaultCommandApiPathAResource"),
                COMPILER.classOf(compiledClasses, BASE_PACKAGE, "resource", "DefaultCommandApiPathBResource")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldIncludeStandardProviders() throws Exception {

        generator.run(
                restRamlWithDefaults()
                        .with(resource("/pathA").with(httpActionWithDefaultMapping(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Set<Class<?>> compiledClasses = COMPILER.compiledClassesOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE);

        final Class<?> applicationClass = COMPILER.classOf(
                compiledClasses,
                BASE_PACKAGE,
                "CommandApiRestServiceApplication");

        final Object application = applicationClass.newInstance();

        final CommonProviders commonProviders = mock(CommonProviders.class);
        when(commonProviders.providers()).thenReturn(newHashSet(JaxRsProviderA.class, JaxRsProviderB.class));

        setField(application, "commonProviders", commonProviders);

        final Method method = applicationClass.getDeclaredMethod("getClasses");
        final Object result = method.invoke(application);
        assertThat(result, is(instanceOf(Set.class)));
        final Set<Class<?>> classes = (Set<Class<?>>) result;
        assertThat(classes, hasItems(JaxRsProviderA.class, JaxRsProviderB.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotGenerateExistingClasses() throws Exception {
        final Path sourcePath = existingFilePath();

        generator.run(
                restRamlWithQueryApiDefaults()
                        .with(resource("/pathA").with(httpActionWithDefaultMapping(GET).withDefaultResponseType()))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties(), singletonList(sourcePath)));

        final Path outputPath = Paths.get(File.createTempFile("junit", null, outputFolder).getAbsolutePath(), EXISTING_FILE_PATH);

        assertThat(outputPath.toFile().exists(), equalTo(FALSE));
    }

    @Test
    public void shouldLogWarningIfClassExists() throws Exception {

        final Raml raml = restRamlWithQueryApiDefaults()
                .with(resource("/pathA")
                        .with(httpActionWithDefaultMapping(GET)
                                .withDefaultResponseType()))
                .build();
        final GeneratorConfig configuration = configurationWithBasePackage(
                BASE_PACKAGE,
                outputFolder,
                new CommonGeneratorProperties(),
                singletonList(existingFilePath()));

        generator.run(raml, configuration);
        generator.run(raml, configuration);

        verify(logger).warn("The class {} already exists, skipping code generation.", "QueryApiPathAResource");
    }

    private Path existingFilePath() {
        return Paths.get(outputFolder.getAbsolutePath());
    }

    private static class JaxRsProviderA {

    }

    private static class JaxRsProviderB {

    }
}
