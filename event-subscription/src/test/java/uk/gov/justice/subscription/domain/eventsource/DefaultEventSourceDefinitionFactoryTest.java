package uk.gov.justice.subscription.domain.eventsource;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultEventSourceDefinitionFactoryTest {

    @Mock
    private JndiAppNameProvider jndiAppNameProvider;

    @InjectMocks
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    @Test
    public void shouldCreateADefaultDataSource() throws Exception {

        when(jndiAppNameProvider.getAppName()).thenReturn("my-context-service");

        final EventSourceDefinition eventSourceDefinition = defaultEventSourceDefinitionFactory.createDefaultEventSource();

        assertThat(eventSourceDefinition.getName(), is("my-context-service-event-store"));
        assertThat(eventSourceDefinition.isDefault(), is(true));

        final Location location = eventSourceDefinition.getLocation();

        if (location.getDataSource().isPresent()) {
            assertThat(location.getDataSource().get(), is("java:/app/my-context-service/DS.eventstore"));
        } else {
            fail();
        }

        assertThat(location.getJmsUri(), is("JMS URI not used"));
        assertThat(location.getRestUri(), is(empty()));
    }
}
