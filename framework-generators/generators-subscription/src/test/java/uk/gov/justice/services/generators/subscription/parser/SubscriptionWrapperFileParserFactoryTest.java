package uk.gov.justice.services.generators.subscription.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;

import org.junit.jupiter.api.Test;

public class SubscriptionWrapperFileParserFactoryTest {

    @Test
    public void shouldCreateJsonSchemaFileParser() throws Exception {

        final FileParser<SubscriptionWrapper> subscriptionDescriptorFileParser = new SubscriptionWrapperFileParserFactory().create();

        assertThat(subscriptionDescriptorFileParser, instanceOf(SubscriptionWrapperFileParser.class));
    }
}
