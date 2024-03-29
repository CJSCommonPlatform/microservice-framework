package uk.gov.justice.services.generators.subscription.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class JmsUriToDestinationConverterTest {

    @Test
    public void shouldConvertJmsTopicUriToDestination() {

        final String destination = new JmsUriToDestinationConverter().convert("jms:topic:public.event");

        assertThat(destination, is("public.event"));
    }

    @Test
    public void shouldConvertJmsQueueUriToDestination() {

        final String destination = new JmsUriToDestinationConverter().convert("jms:queue:command.handler");

        assertThat(destination, is("command.handler"));
    }
}