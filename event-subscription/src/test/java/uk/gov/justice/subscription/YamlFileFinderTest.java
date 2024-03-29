package uk.gov.justice.subscription;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class YamlFileFinderTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private YamlFileFinder yamlFileFinder;

    @Test
    public void shouldFindAllSubscriptionDescriptorsOnTheClasspathWhichHaveTheCorrectName() throws Exception {

        final List<URL> urls = yamlFileFinder.getSubscriptionsDescriptorsPaths();

        assertThat(urls.size(), is(1));

        assertThat(urls.get(0).toString(), endsWith("/yaml/subscriptions-descriptor.yaml"));
    }

    @Test
    public void shouldFindAllEventSourcesOnTheClasspathWhichHaveTheCorrectName() throws Exception {

        final List<URL> urls = yamlFileFinder.getEventSourcesPaths();

        assertThat(urls.size(), is(1));

        assertThat(urls.get(0).toString(), endsWith("/yaml/event-sources.yaml"));
    }

    @Test
    public void shouldLogFoundResources() throws Exception {
        yamlFileFinder.getSubscriptionsDescriptorsPaths();

        verify(logger).debug("Found 1 resources on the classpath for yaml/subscriptions-descriptor.yaml");
    }
}
