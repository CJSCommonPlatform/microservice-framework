package uk.gov.justice.services.generators.subscription.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.generators.commons.helper.FileParserException;
import uk.gov.justice.services.generators.commons.helper.PathToUrlResolver;
import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventSourcesFileParserTest {

    @Mock
    private EventSourcesParser eventSourcesParser;

    @Mock
    private PathToUrlResolver pathToUrlResolver;

    @Mock
    private EventSourceYamlClasspathFinder eventSourceYamlClasspathFinder;

    @InjectMocks
    private EventSourcesFileParser eventSourcesFileParser;

    @Test
    public void shouldReturnEventSourceDefinitions() throws Exception {
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Path baseDir = Paths.get("/yaml");

        final Path eventSourcePath = Paths.get("event-sources.yaml");
        final Path subscriptionDescriptorPath = Paths.get("subscriptions-descriptor.yaml");
        final List<Path> pathList = asList(eventSourcePath, subscriptionDescriptorPath);

        final URL eventSourceUrl = baseDir.resolve(eventSourcePath).toUri().toURL();
        final List<URL> urlList = singletonList(eventSourceUrl);

        when(pathToUrlResolver.resolveToUrl(baseDir, eventSourcePath)).thenReturn(eventSourceUrl);
        when(eventSourceYamlClasspathFinder.getEventSourcesPaths()).thenReturn(emptyList());
        when(eventSourcesParser.eventSourcesFrom(urlList)).thenReturn(Stream.of(eventSourceDefinition));

        final List<EventSourceDefinition> expectedEventSourceDefinition = eventSourcesFileParser.getEventSourceDefinitions(baseDir, pathList);

        assertThat(expectedEventSourceDefinition.size(), is(1));
        assertThat(expectedEventSourceDefinition.get(0), is(eventSourceDefinition));
    }

    @Test
    public void shouldThrowExceptionIfNoEventSourceDefinitionsPresent() {
        final Path baseDir = mock(Path.class);

        when(eventSourceYamlClasspathFinder.getEventSourcesPaths()).thenReturn(emptyList());

        try {
            eventSourcesFileParser.getEventSourceDefinitions(baseDir, emptyList());
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(FileParserException.class)));
            assertThat(e.getMessage(), is("No event-sources.yaml files found!"));
        }
    }
}