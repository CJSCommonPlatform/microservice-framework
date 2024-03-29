package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.junit.jupiter.api.Test;

public class SubscriptionBuilderTest {


    @Test
    public void shouldBuildASubscription() throws Exception {

        final String name = "name";
        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final String eventSourceName = "eventSoureName";
        final int prioritisation = 1;

        final Subscription subscription = subscription()
                .withName(name)
                .withEvents(asList(event_1, event_2))
                .withEventSourceName(eventSourceName)
                .withPrioritisation(prioritisation)
                .build();

        assertThat(subscription.getName(), is(name));
        assertThat(subscription.getEventSourceName(), is(eventSourceName));
        assertThat(subscription.getPrioritisation(), is(prioritisation));
        assertThat(subscription.getEvents(), hasItem(event_1));
        assertThat(subscription.getEvents(), hasItem(event_2));
    }

    @Test
    public void shouldBeAbleToAddEventsOneAtATime() throws Exception {

        final String name = "name";
        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);
        final String eventSourceName = "eventSourceName";
        final int prioritisation = 1;

        final Subscription subscription = subscription()
                .withName(name)
                .withEvent(event_1)
                .withEvent(event_2)
                .withEventSourceName(eventSourceName)
                .withPrioritisation(prioritisation)
                .build();

        assertThat(subscription.getName(), is(name));
        assertThat(subscription.getEventSourceName(), is(eventSourceName));
        assertThat(subscription.getPrioritisation(), is(prioritisation));
        assertThat(subscription.getEvents(), hasItem(event_1));
        assertThat(subscription.getEvents(), hasItem(event_2));
    }
}
