package uk.gov.justice.services.common.rest;

import static jakarta.json.Json.createObjectBuilder;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;

import uk.gov.justice.services.common.exception.ForbiddenRequestException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ForbiddenRequestExceptionMapper implements ExceptionMapper<ForbiddenRequestException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForbiddenRequestExceptionMapper.class);

    @Override
    public Response toResponse(final ForbiddenRequestException exception) {
        LOGGER.debug("Forbidden Request", exception);

        return Response.status(FORBIDDEN)
                .entity(createObjectBuilder().add("error", exception.getMessage()).build().toString())
                .build();
    }

}
