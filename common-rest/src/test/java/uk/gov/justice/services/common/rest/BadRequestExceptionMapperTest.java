package uk.gov.justice.services.common.rest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.JsonSchemaValidationException;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;

import javax.ws.rs.core.Response;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class BadRequestExceptionMapperTest {

    private static final String TEST_ERROR_MESSAGE = "Test Error Message.";

    @Mock
    private Logger logger;

    @Mock
    private Schema schema;

    @Spy
    private JsonValidationLoggerHelper jsonValidationLoggerHelper = new DefaultJsonValidationLoggerHelper();

    @InjectMocks
    private BadRequestExceptionMapper exceptionMapper;

    @Test
    public void shouldReturn400ResponseForBadRequestException() throws Exception {

        final Response response = exceptionMapper.toResponse(new BadRequestException(TEST_ERROR_MESSAGE));

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), notNullValue());
        assertThat(response.getEntity().toString(),
                hasJsonPath("$.error", equalTo(TEST_ERROR_MESSAGE)));
    }

    @Test
    public void shouldAddJsonValidationErrorsToResponse() {
        final ValidationException validationException = new ValidationException(schema, "Test Json");
        final JsonSchemaValidationException jsonSchemaValidationException = new JsonSchemaValidationException(validationException.getMessage(), validationException);
        final BadRequestException badRequestException = new BadRequestException(TEST_ERROR_MESSAGE,
                jsonSchemaValidationException);
        final Response response = exceptionMapper.toResponse(badRequestException);
        final String body = response.getEntity().toString();
        assertThat(body, hasJsonPath("$.validationErrors.message", equalTo("#: Test Json")));
    }
}
