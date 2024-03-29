package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultInterceptorContextTest {

    @Mock
    private JsonEnvelope input;

    @Test
    public void shouldBuildInterceptorContextWithInputAndInjectionPoint() throws Exception {

        final InterceptorContext result = interceptorContextWithInput(input);

        assertThat(result.inputEnvelope(), is(input));
        assertThat(result.outputEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldCopyInterceptorContextWithInput() throws Exception {

        final JsonEnvelope expectedInput = mock(JsonEnvelope.class);
        final InterceptorContext initialInterceptorContext = interceptorContextWithInput(input);

        final InterceptorContext result = initialInterceptorContext.copyWithInput(expectedInput);

        assertThat(result.inputEnvelope(), is(expectedInput));
        assertThat(result.outputEnvelope(), is(Optional.empty()));
    }

    @Test
    public void shouldCopyInterceptorContextWithOutput() throws Exception {

        final JsonEnvelope output = mock(JsonEnvelope.class);
        final InterceptorContext initialInterceptorContext = interceptorContextWithInput(input);

        final InterceptorContext result = initialInterceptorContext.copyWithOutput(output);

        assertThat(result.inputEnvelope(), is(input));
        assertThat(result.outputEnvelope(), is(Optional.of(output)));
    }

    @Test
    public void shouldSetAndRetrieveInputParameter() throws Exception {

        final Object parameter = mock(Object.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(input);

        interceptorContext.setInputParameter("test", parameter);

        assertThat(interceptorContext.getInputParameter("test"), is(Optional.of(parameter)));
    }

    @Test
    public void shouldSetAndRetrieveOutputParameter() throws Exception {

        final Object parameter = mock(Object.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(input);

        interceptorContext.setOutputParameter("test", parameter);

        assertThat(interceptorContext.getOutputParameter("test"), is(Optional.of(parameter)));
    }

    @Test
    public void shouldThrowExceptionIfInputNotSet() throws Exception {
        assertThrows(IllegalStateException.class, () -> interceptorContextWithInput(null).inputEnvelope());
    }

    @Test
    public void shouldGetComponentName() {
        final InterceptorContext interceptorContext = interceptorContextWithInput(input);

        interceptorContext.setInputParameter("component", "testComponent");

        assertThat(interceptorContext.getComponentName(), is("testComponent"));
    }

    @Test
    public void shouldGetDefaultUnknownComponentNameIfNotSet() {
        final InterceptorContext interceptorContext = interceptorContextWithInput(input);

        assertThat(interceptorContext.getComponentName(), is("UNKNOWN"));
    }
}