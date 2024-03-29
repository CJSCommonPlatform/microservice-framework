package uk.gov.justice.services.adapter.rest.mapping;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.generators.test.utils.builder.HeadersBuilder.headersWith;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasicActionMapperHelperTest {

    private BasicActionMapperHelper mapping;

    @BeforeEach
    public void setup() {
        mapping = new BasicActionMapperHelper();
        mapping.add("methodA", "application/vnd.blah+json", "actionNameA");
        mapping.add("methodA", "application/vnd.blah2+json", "actionNameB");
        mapping.add("methodA", "application/vnd.blah3+json", "actionNameB");
        mapping.add("methodB", "application/vnd.blah+json", "actionNameC");
    }

    @Test
    public void shouldReturnActionForGETRequests() throws Exception {
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah+json")), is("actionNameA"));
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah2+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah3+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodB", "GET",
                headersWith("Accept", "application/vnd.blah+json")), is("actionNameC"));
    }

    @Test
    public void shouldReturnActionForPOSTRequests() throws Exception {
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah+json")), is("actionNameA"));
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah2+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah3+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodB", "POST",
                headersWith("Content-Type", "application/vnd.blah+json")), is("actionNameC"));
    }

    @Test
    public void shouldReturnActionForGetRequestIfCharsetIncludedInMediaType() throws Exception {
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah+json; charset=ISO-8859-1")), is("actionNameA"));
    }

    @Test
    public void shouldReturnActionForPOSTRequestIfCharsetIncludedInMediaType() throws Exception {
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah+json; charset=ISO-8859-1")), is("actionNameA"));
    }

    @Test
    public void shouldThrowExceptionIfGetRequestMediaTypeDoesNotMatch() throws Exception {

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                mapping.actionOf("methodA", "GET", headersWith("Accept", "application/vnd.unknown+json"))
        );

        assertThat(badRequestException.getMessage(), is("No matching action for accept media types: [application/vnd.unknown+json]"));
    }
}