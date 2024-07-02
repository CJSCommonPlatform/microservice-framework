package uk.gov.justice.subscription;

import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.services.yaml.YamlSchemaLoader;
import uk.gov.justice.services.yaml.YamlToJsonObjectConverter;
import uk.gov.justice.services.yaml.YamlValidationException;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class EventSourcesParserTest {

    private EventSourcesParser eventSourcesParser;

    @BeforeEach
    public void setUp() {

        final YamlParser yamlParser = new YamlParser();
        final YamlSchemaLoader yamlSchemaLoader = new YamlSchemaLoader();
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(new YamlToJsonObjectConverter(yamlParser, objectMapper), yamlSchemaLoader);

        eventSourcesParser = new EventSourcesParser(yamlParser, yamlFileValidator);
    }

    @Test
    public void shouldThrowExceptionWhenEventSourceYamlIsNotAvailable() throws Exception {

        try {

            final List<URL> urls = emptyList();
            eventSourcesParser.eventSourcesFrom(urls);
        } catch (final YamlFileLoadingException exception) {
            assertThat(exception.getMessage(), is("No event-sources.yaml defined on the classpath"));
        }
    }

    @Test
    public void shouldParseAllEventSources() throws Exception {

        final URL url = getFromClasspath("yaml/event-sources.yaml");

        final List<EventSourceDefinition> eventSourceDefinitions = eventSourcesParser
                .eventSourcesFrom(singletonList(url))
                .collect(toList());

        assertThat(eventSourceDefinitions.size(), is(3));

        final EventSourceDefinition eventSourceDefinition_1 = eventSourceDefinitions.get(0);
        assertThat(eventSourceDefinition_1.getName(), is("default"));
        assertThat(eventSourceDefinition_1.isDefault(), is(true));
        assertThat(eventSourceDefinition_1.getLocation(), is(notNullValue()));
        assertThat(eventSourceDefinition_1.getLocation().getJmsUri(), is("jms:topic:default"));
        assertThat(eventSourceDefinition_1.getLocation().getRestUri(), is(of("http://localhost:8080/default/event-source-api/rest")));
        assertThat(eventSourceDefinition_1.getLocation().getDataSource(), is(of("java:/app/default/DS.eventstore")));

        final EventSourceDefinition eventSourceDefinition_2 = eventSourceDefinitions.get(1);
        assertThat(eventSourceDefinition_2.getName(), is("no-data-source"));
        assertThat(eventSourceDefinition_2.isDefault(), is(false));
        assertThat(eventSourceDefinition_2.getLocation(), is(notNullValue()));
        assertThat(eventSourceDefinition_2.getLocation().getJmsUri(), is("jms:topic:no.data.source"));
        assertThat(eventSourceDefinition_2.getLocation().getRestUri(), is(of("http://localhost:8080/no-data-source/event-source-api/rest")));
        assertThat(eventSourceDefinition_2.getLocation().getDataSource(), is(empty()));

        final EventSourceDefinition eventSourceDefinition_3 = eventSourceDefinitions.get(2);
        assertThat(eventSourceDefinition_3.getName(), is("no-data-source-or-rest-uri"));
        assertThat(eventSourceDefinition_3.isDefault(), is(false));
        assertThat(eventSourceDefinition_3.getLocation(), is(notNullValue()));
        assertThat(eventSourceDefinition_3.getLocation().getJmsUri(), is("jms:topic:no.data.source.or.rest.uri"));
        assertThat(eventSourceDefinition_3.getLocation().getRestUri(), is(empty()));
        assertThat(eventSourceDefinition_3.getLocation().getDataSource(), is(empty()));
    }

    @Test
    public void shouldThrowExceptionIfIncorrectJmsUri() throws Exception {

        final URL url = getFromClasspath("yaml/incorrect-event-sources.yaml");

        try {
            eventSourcesParser
                    .eventSourcesFrom(singletonList(url))
                    .collect(toList());
            fail();
        } catch (final YamlValidationException e) {
            assertThat(e.getCause(), instanceOf(ValidationException.class));
            assertThat(e.getMessage(), containsString("Errors: [#/event_sources/1/location/jms_uri: string [jms:topic:example.event?timeToLive=1000] does not match pattern ^jms:(queue|topic):([a-z|A-Z|-|\\\\.])+$, #/event_sources/0/location/jms_uri: string [jms:topic] does not match pattern ^jms:(queue|topic):([a-z|A-Z|-|\\\\.])+$, #/event_sources/0: required key [name] not found]"));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private URL getFromClasspath(final String name) throws MalformedURLException {
        return get(getClass().getClassLoader().getResource(name).getPath()).toUri().toURL();
    }
}
