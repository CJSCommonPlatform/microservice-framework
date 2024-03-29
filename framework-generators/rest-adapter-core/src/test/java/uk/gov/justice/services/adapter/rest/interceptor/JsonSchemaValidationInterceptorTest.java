package uk.gov.justice.services.adapter.rest.interceptor;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.json.JsonSchemaValidationException;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.messaging.exception.InvalidMediaTypeException;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptorContext;

import com.google.common.io.CharStreams;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

/**
 * Unit tests for the {@link JsonSchemaValidationInterceptor} class.
 */
@ExtendWith(MockitoExtension.class)
public class JsonSchemaValidationInterceptorTest {

    private static final String PAYLOAD = "test payload";
    private static final String MEDIA_TYPE_TYPE = "application";
    private static final String MEDIA_SUBTYPE = "vnd.test-name+json";
    private static final MediaType MEDIA_TYPE = new MediaType(MEDIA_TYPE_TYPE, MEDIA_SUBTYPE);
    private static final uk.gov.justice.services.core.mapping.MediaType CONVERTED_MEDIA_TYPE
            = new uk.gov.justice.services.core.mapping.MediaType(MEDIA_TYPE_TYPE, MEDIA_SUBTYPE);
    private static final String NON_JSON_MEDIA_SUBTYPE = "vnd.test-name+xml";

    @Mock
    private Logger logger;

    @Mock
    private ReaderInterceptorContext context;

    @Mock
    private Object proceed = mock(Object.class);

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private JsonValidationLoggerHelper jsonValidationLoggerHelper;

    @Mock
    private HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    @InjectMocks
    private JsonSchemaValidationInterceptor jsonSchemaValidationInterceptor;

    @BeforeEach
    public void setup() throws Exception {
        when(context.getMediaType()).thenReturn(MEDIA_TYPE);
    }

    @Test
    public void shouldReturnResultOfContextProceed() throws Exception {
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));
        when(context.proceed()).thenReturn(proceed);

        assertThat(jsonSchemaValidationInterceptor.aroundReadFrom(context), equalTo(proceed));
    }

    @Test
    public void shouldSetInputStreamToOriginalPayload() throws Exception {
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));
        when(context.proceed()).thenReturn(proceed);

        jsonSchemaValidationInterceptor.aroundReadFrom(context);
        verify(context).setInputStream(argThat(inputStreamEqualTo(PAYLOAD)));
    }

    @Test
    public void shouldValidatePayloadAgainstSchema() throws Exception {

        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));
        when(context.proceed()).thenReturn(proceed);


        jsonSchemaValidationInterceptor.aroundReadFrom(context);

        verify(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
    }

    @Test
    public void shouldSkipValidationIfNonJsonPayloadType() throws Exception {

        final String actionName = "example.action-name";

        when(context.getMediaType()).thenReturn(new MediaType(MEDIA_TYPE_TYPE, NON_JSON_MEDIA_SUBTYPE));

        jsonSchemaValidationInterceptor.aroundReadFrom(context);
        verify(jsonSchemaValidator, never()).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldThrowBadRequestExceptionIfValidatorFailsWithValidationException() throws Exception {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));

        doThrow(new JsonSchemaValidationException("")).when(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
        when(context.getHeaders()).thenReturn(headers);

        assertThrows(BadRequestException.class, () -> jsonSchemaValidationInterceptor.aroundReadFrom(context));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRemoveUserIdHeaderParamFromBadRequestExceptionIfValidatorFailsWithValidationException() throws Exception {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap();
        headers.add(USER_ID, randomUUID().toString());
        headers.add(ID, randomUUID().toString());
        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));

        doThrow(new JsonSchemaValidationException("")).when(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));
        when(context.getHeaders()).thenReturn(headers);

        final ArgumentCaptor<MultivaluedHashMap> captor = ArgumentCaptor.forClass(MultivaluedHashMap.class);

        assertThrows(BadRequestException.class, () -> jsonSchemaValidationInterceptor.aroundReadFrom(context));

        verify(httpTraceLoggerHelper).toHttpHeaderTrace(captor.capture());

        final MultivaluedHashMap<String, String> headerMap = captor.getValue();

        assertThat(headerMap, hasKey(ID));
        assertThat(headerMap, not(hasKey(USER_ID)));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void shouldThrowBadRequestExceptionIfValidatorFailsWithInvalidMediaTypeException() throws Exception {
        final String actionName = "example.action-name";

        when(nameToMediaTypeConverter.convert(CONVERTED_MEDIA_TYPE)).thenReturn(actionName);
        when(context.getInputStream()).thenReturn(inputStream(PAYLOAD));

        doThrow(new InvalidMediaTypeException("", mock(Exception.class))).when(jsonSchemaValidator).validate(PAYLOAD, actionName, of(CONVERTED_MEDIA_TYPE));

        assertThrows(BadRequestException.class, () -> jsonSchemaValidationInterceptor.aroundReadFrom(context));
    }



    private InputStream inputStream(final String input) throws IOException {
        return new ByteArrayInputStream(input.getBytes("UTF-8"));
    }

    private Matcher<InputStream> inputStreamEqualTo(final String input) throws IOException {

        return new TypeSafeMatcher<InputStream>() {

            @Override
            protected boolean matchesSafely(final InputStream item) {
                try {
                    final String actual = CharStreams.toString(new InputStreamReader(item));
                    item.reset();
                    return input.equals(actual);
                } catch (IOException ex) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(input);
            }
        };
    }
}
