package uk.gov.justice.services.adapters.rest.generator;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.test.utils.builder.HttpActionBuilder.httpActionWithDefaultMapping;
import static uk.gov.justice.services.generators.test.utils.builder.RamlBuilder.restRamlWithCommandApiDefaults;
import static uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.methodsOf;

import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

public class RestAdapterGenerator_MultipleMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = createObjectBuilder()
            .add("name", "Frederick Bloggs")
            .build();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithCommandApiDefaults()
                        .with(resource("/path")
                                .with(httpActionWithDefaultMapping(GET).withDefaultResponseType())
                                .with(httpActionWithDefaultMapping(POST).withHttpActionOfDefaultRequestType())
                                .with(httpActionWithDefaultMapping(PUT).withHttpActionOfDefaultRequestType())
                        ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, new CommonGeneratorProperties()));

        final Class<?> resourceClass = COMPILER.compiledClassOf(
                outputFolder,
                outputFolder,
                BASE_PACKAGE,
                "resource",
                "DefaultCommandApiPathResource");

        final String action = "theAction";
        when(actionMapper.actionOf(any(String.class), any(String.class), eq(httpHeaders))).thenReturn(action);

        final Object resourceObject = getInstanceOf(resourceClass);

        final Response processorResponse = Response.ok().build();
        when(restProcessor.process(anyString(), any(Function.class), anyString(), any(HttpHeaders.class), any(Collection.class))).thenReturn(processorResponse);
        when(restProcessor.process(anyString(), any(Function.class), anyString(), any(Optional.class), any(HttpHeaders.class),
                any(Collection.class))).thenReturn(processorResponse);

        final List<Method> methods = methodsOf(resourceClass);

        for (final Method method : methods) {
            final int parameterCount = method.getParameterCount();
            final boolean isGetMethod = parameterCount == 0;

            final Object result = isGetMethod ?
                    method.invoke(resourceObject) :
                    method.invoke(resourceObject, NOT_USED_JSONOBJECT);

            assertThat(result, is(processorResponse));
        }
    }
}
