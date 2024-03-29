package uk.gov.justice.services.adapter.rest.processor.multipart;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.multipart.InputPartFileNameExtractor;

import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InputPartFileNameExtractorTest {

    @InjectMocks
    private InputPartFileNameExtractor inputPartFileNameExtractor;

    @Test
    public void shouldExtractTheFileNameFromTheContentDispositionHeader() throws Exception {

        final String headerName = "Content-Disposition";
        final String headerValue = "form-data; name=\"file\"; filename=\"your_file.zip\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        assertThat(inputPartFileNameExtractor.extractFileName(inputPart), is("your_file.zip"));
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoContentDispositionHeaderFound() throws Exception {

        final String headerName = "Some-Other-Header-Name";
        final String headerValue = "form-data; name=\"file\"; filename=\"your_file.zip\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("No header found named 'Content-Disposition'"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoHeadersFound() throws Exception {

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>());

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("No header found named 'Content-Disposition'"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfNoFilenameFoundInContentDispositionHeader() throws Exception {

        final String headerName = "Content-Disposition";
        final String headerValue = "form-data; name=\"file\"";
        final Map<String, String> headers = of(headerName, headerValue);

        final InputPart inputPart = mock(InputPart.class);
        when(inputPart.getHeaders()).thenReturn(new MultivaluedHashMap<>(headers));

        try {
            inputPartFileNameExtractor.extractFileName(inputPart);
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Failed to find 'filename' in 'Content-Disposition' header"));
        }
    }
}