package uk.gov.justice.services.test.utils.core.helper;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.helper.ServiceComponents.verifyPassThroughQueryHandlerMethod;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ServiceComponentsQueryHandlerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldVerifyAllMethodsOfAPassThroughServiceComponent() throws Exception {
        verifyPassThroughQueryHandlerMethod(ValidQueryApi.class);
    }

    @Test
    public void shouldVerifyASingleMethod() throws Exception {
        verifyPassThroughQueryHandlerMethod(ValidQueryApi.class, "testA");
    }

    @Test
    public void shouldVerifyMultipleNamedMethods() throws Exception {
        verifyPassThroughQueryHandlerMethod(ValidQueryApi.class, "testA", "testB");
    }

    @Test
    public void shouldFailWhenNoHandlerMethod() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present, or no Handler methods for class InValidNoHandlerMethod");

        verifyPassThroughQueryHandlerMethod(InValidNoHandlerMethod.class);
    }

    @Test
    public void shouldFailWhenNoHandlesAnnotation() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present, or no Handler methods for class InValidNoHandlesAnnotation");

        verifyPassThroughQueryHandlerMethod(InValidNoHandlesAnnotation.class);
    }

    @Test
    public void shouldFailWhenNoHandlesAnnotationOnNamedMethod() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @Handles annotation present on Method testA");

        verifyPassThroughQueryHandlerMethod(InValidNoHandlesAnnotation.class, "testA");
    }

    @Test
    public void shouldFailWhenNoServiceComponentAnnotation() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No @ServiceComponent annotation present on Class NoServiceComponentAnnotation");

        verifyPassThroughQueryHandlerMethod(NoServiceComponentAnnotation.class, "testA");
    }

    @Test
    public void shouldFailWhenNoRequesterField() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("No field of class type Requester found in handler class");

        verifyPassThroughQueryHandlerMethod(NoRequesterField.class, "testA");
    }

    @Test
    public void shouldFailWhenRequesterIsNotInvoked() throws Exception {
        exception.expect(AssertionError.class);
        exception.expectMessage("JsonEnvelope response does not match expected response in method testA");

        verifyPassThroughQueryHandlerMethod(NoRequesterCall.class);
    }

    @ServiceComponent(QUERY_API)
    public static class ValidQueryApi {

        @Inject
        Requester requester;

        @Handles("testA")
        public JsonEnvelope testA(final JsonEnvelope query) {
            return requester.request(query);
        }

        @Handles("testB")
        public JsonEnvelope testB(final JsonEnvelope query) {
            return requester.request(query);
        }
    }

    @ServiceComponent(QUERY_API)
    public static class InValidNoHandlerMethod {

        @Inject
        Requester requester;
    }

    @ServiceComponent(QUERY_API)
    public static class InValidNoHandlesAnnotation {

        @Inject
        Requester requester;

        public JsonEnvelope testA(final JsonEnvelope query) {
            return requester.request(query);
        }
    }

    public static class NoServiceComponentAnnotation {

        @Inject
        Requester requester;

        @Handles("testA")
        public JsonEnvelope testA(final JsonEnvelope query) {
            return requester.request(query);
        }
    }

    @ServiceComponent(QUERY_API)
    public static class NoRequesterField {

        @Handles("testA")
        public JsonEnvelope testA(final JsonEnvelope query) {
            return null;
        }
    }

    @ServiceComponent(QUERY_CONTROLLER)
    public static class NoRequesterCall {
        @Inject
        private Requester requester;

        @Handles("testA")
        public JsonEnvelope testA(final JsonEnvelope query) {
            return null;
        }
    }
}
