package uk.gov.justice.services.generators.commons.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;

import org.junit.jupiter.api.Test;

public class RequestContentTypeRamlValidatorTest {

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        new RequestContentTypeRamlValidator(POST).validate(
                raml()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.command1+json")))
                        .build());
    }

    @Test
    public void shouldPassIfAllMediaTypesContainAValidCommand() throws Exception {

        new RequestContentTypeRamlValidator(POST, PUT).validate(
                raml()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.command1+json"))
                                .with(httpActionWithDefaultMapping(PUT, "application/vnd.command1+json")))
                        .build());
    }

    @Test
    public void shouldIgnoreInvalidMediaTypesInNonPOSTActions() throws Exception {

        new RequestContentTypeRamlValidator(POST).validate(
                raml()
                        .with(resource()
                                .with(httpActionWithDefaultMapping(GET, "application/vnd.command1+json"))
                                .with(httpActionWithDefaultMapping(POST, "application/vnd.command2+json"))
                                .with(httpActionWithDefaultMapping(HEAD, "application/vnd.command3+json"))
                                .with(httpActionWithDefaultMapping(PUT, "application/vnd.command4+json"))
                                .with(httpActionWithDefaultMapping(OPTIONS, "application/vnd.command5+json"))
                        )
                        .build());
    }

    @Test
    public void shouldNotPassIfMediaTypeContainsInvalidCommand() throws Exception {

        final RamlValidationException ramlValidationException = assertThrows(RamlValidationException.class, () ->
                new RequestContentTypeRamlValidator(POST).validate(
                        raml()
                                .with(resource()
                                        .with(httpActionWithDefaultMapping(POST)))
                                .build())
        );

        assertThat(ramlValidationException.getMessage(), is("Request type not set"));
    }

    @Test
    public void shouldNotPassIfAllMediaTypesContainInvalidCommand() throws Exception {

        final RamlValidationException ramlValidationException = assertThrows(RamlValidationException.class, () ->
                new RequestContentTypeRamlValidator(POST, PUT).validate(
                        raml()
                                .with(resource()
                                        .with(httpActionWithDefaultMapping(POST, "application/vnd.command1+json"))
                                        .with(httpActionWithDefaultMapping(PUT)))
                                .build())
        );

        assertThat(ramlValidationException.getMessage(), is("Request type not set"));
    }
}
