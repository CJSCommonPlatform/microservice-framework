package uk.gov.justice.services.clients.unifiedsearch.generator.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;
import uk.gov.justice.services.clients.unifiedsearch.core.domain.UnifiedSearchDescriptor;

import org.junit.jupiter.api.Test;

public class UnifiedSearchDescriptorFileParserFactoryTest {

    @Test
    public void shouldCreateUninfiedSearchDescriptorParser() throws Exception {

        final FileParser<UnifiedSearchDescriptor> unifiedSearchDescriptorFileParser = new UnifiedSearchDescriptorFileParserFactory().create();

        assertThat(unifiedSearchDescriptorFileParser, is(instanceOf(UnifiedSearchDescriptorParser.class)));
    }

}