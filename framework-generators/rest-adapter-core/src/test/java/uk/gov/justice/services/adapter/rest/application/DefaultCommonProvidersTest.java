package uk.gov.justice.services.adapter.rest.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.adapter.rest.cors.CorsFeature;
import uk.gov.justice.services.adapter.rest.filter.JsonValidatorRequestFilter;
import uk.gov.justice.services.adapter.rest.interceptor.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.adapter.rest.mapper.ConflictedResourceExceptionMapper;
import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class DefaultCommonProvidersTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnAllCommonProviders() throws Exception {
        Set<Class<?>> providers = new DefaultCommonProviders().providers();
        assertThat(providers, containsInAnyOrder(
                BadRequestExceptionMapper.class,
                ConflictedResourceExceptionMapper.class,
                ForbiddenRequestExceptionMapper.class,
                JsonSchemaValidationInterceptor.class,
                JsonValidatorRequestFilter.class,
                CorsFeature.class));
    }
}
