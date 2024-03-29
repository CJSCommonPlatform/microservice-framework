package uk.gov.justice.raml.jms.core;


import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.adapter.messaging.JmsProcessor;
import uk.gov.justice.services.event.buffer.api.AbstractEventFilter;
import uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.raml.model.Raml;

public class JmsEndpointGenerator_EventFilterTest {

    private static final String BASE_PACKAGE = "uk.test";
    private static final String BASE_PACKAGE_FOLDER = "/uk/test";
    private static final JavaCompilerUtility COMPILER = javaCompilerUtil();

    @TempDir
    public File outputFolder;

    @Mock
    private JmsProcessor jmsProcessor;

    private GeneratorProperties generatorProperties;
    private Generator<Raml> generator;

    @BeforeEach
    public void setup() throws Exception {
        generator = new JmsEndpointGenerator();
        generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf(EVENT_LISTENER);
    }

    @Test
    public void shouldGenerateEventFilterForEventListener() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/structure")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventB+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "StructureEventListenerSomecontextControllerCommandEventFilter");

        final AbstractEventFilter eventFilter = (AbstractEventFilter) clazz.newInstance();

        assertThat(eventFilter.accepts("structure.eventA"), is(true));
        assertThat(eventFilter.accepts("structure.eventB"), is(true));
        assertThat(eventFilter.accepts("structure.eventC"), is(false));
    }

    @Test
    public void shouldNotGenerateEventFilterForEventProcessor() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/processor/message/structure")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventB+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getAbsolutePath() + BASE_PACKAGE_FOLDER);
        assertThat(asList(packageDir.listFiles()), not(hasItem(hasProperty("name", containsString("EventFilter")))));
    }

    @Test
    public void shouldNotGenerateEventFilterForGeneralMediaType() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/context")
                        .with(resource()
                                .withRelativeUri("/some.event")
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        final File packageDir = new File(outputFolder.getAbsolutePath() + BASE_PACKAGE_FOLDER);
        assertThat(asList(packageDir.listFiles()), not(hasItem(hasProperty("name", containsString("EventFilter")))));
    }

    @Test
    public void shouldAddDIAnnotations() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/structure")
                        .with(resource().withDefaultPostAction())
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "StructureEventListenerSomecontextControllerCommandEventFilter");
        assertThat(clazz.getAnnotation(ApplicationScoped.class), is(not(nullValue())));
    }

    @Test
    public void shouldGenerateEventFilterIfBaseUriContainsSpecialCharacters() throws Exception {
        generator.run(
                raml()
                        .withBaseUri("message://event/listener/message/my-hyphenated-service")
                        .with(resource()
                                .with(httpAction()
                                        .withHttpActionType(POST)
                                        .withMediaTypeWithoutSchema("application/vnd.structure.eventA+json")))
                        .build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, generatorProperties));

        Class<?> clazz = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "MyHyphenatedServiceEventListenerSomecontextControllerCommandEventFilter");

        final AbstractEventFilter eventFilter = (AbstractEventFilter) clazz.newInstance();

        assertThat(eventFilter.accepts("structure.eventA"), is(true));
    }
}
