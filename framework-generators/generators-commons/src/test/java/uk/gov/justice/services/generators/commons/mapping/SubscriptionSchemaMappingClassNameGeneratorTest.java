package uk.gov.justice.services.generators.commons.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubscriptionSchemaMappingClassNameGeneratorTest {

    @InjectMocks
    private SubscriptionSchemaMappingClassNameGenerator subscriptionSchemaMappingClassNameGenerator;

    @Test
    public void shouldCreateTheSchemaMappingClassNameFromTheContextNameAndItsInterfaceName() throws Exception {

        final String contextName = "my-context";
        final String componentName = "EVENT_LISTENER";

        final String mappingClassName = subscriptionSchemaMappingClassNameGenerator.createMappingClassNameFrom(
                contextName,
                componentName,
                MediaTypeToSchemaIdMapper.class);

        assertThat(mappingClassName, is("MyContextEventListenerMediaTypeToSchemaIdMapper"));
    }
}
