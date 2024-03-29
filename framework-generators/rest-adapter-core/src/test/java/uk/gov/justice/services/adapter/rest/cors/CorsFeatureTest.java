package uk.gov.justice.services.adapter.rest.cors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.FeatureContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the {@link CorsFeature} class.
 */
@ExtendWith(MockitoExtension.class)
public class CorsFeatureTest {

    private static final String ALLOWED_ORIGIN = "TEST-ORIGIN";
    private static final String ALLOWED_METHODS = "TEST-METHODS";
    private static final String ALLOWED_HEADERS = "TEST-HEADERS";

    @Mock
    private FeatureContext featureContext;

    private CorsFeature corsFeature;

    @BeforeEach
    public void setup() {
        corsFeature = new CorsFeature();
        corsFeature.allowedOrigin = ALLOWED_ORIGIN;
        corsFeature.allowedMethods = ALLOWED_METHODS;
        corsFeature.allowedHeaders = ALLOWED_HEADERS;
        corsFeature.configure(featureContext);
    }

    @Test
    public void shouldSetAllowedOrigin() {
        ArgumentCaptor<CorsFilter> captor = ArgumentCaptor.forClass(CorsFilter.class);
        verify(featureContext).register(captor.capture());
        assertThat(captor.getValue().getAllowedOrigins(), containsInAnyOrder(ALLOWED_ORIGIN));
    }

    @Test
    public void shouldSetAllowedMethods() {
        ArgumentCaptor<CorsFilter> captor = ArgumentCaptor.forClass(CorsFilter.class);
        verify(featureContext).register(captor.capture());
        assertThat(captor.getValue().getAllowedMethods(), equalTo(ALLOWED_METHODS));
    }

    @Test
    public void shouldSetAllowedHeaders() {
        ArgumentCaptor<CorsFilter> captor = ArgumentCaptor.forClass(CorsFilter.class);
        verify(featureContext).register(captor.capture());
        assertThat(captor.getValue().getAllowedHeaders(), equalTo(ALLOWED_HEADERS));
    }
}
