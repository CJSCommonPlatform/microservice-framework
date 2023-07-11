package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultInterceptorChainProcessorTest {

    @Mock
    private InterceptorCache interceptorCache;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> dispatch;

    @Test
    public void shouldProcessInterceptorContext() throws Exception {

        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(inputEnvelope);
        final String component = "component";

        when(interceptorCache.getInterceptors(component)).thenReturn(interceptors());
        when(dispatch.apply(inputEnvelope)).thenReturn(outputEnvelope);

        final DefaultInterceptorChainProcessor interceptorChainProcessor = new DefaultInterceptorChainProcessor(interceptorCache, dispatch, component);

        final Optional<JsonEnvelope> result = interceptorChainProcessor.process(interceptorContext);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(outputEnvelope));
    }

    @Test
    public void shouldProcessesDispatcherThatReturnsNull() throws Exception {

        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(inputEnvelope);
        final String component = "component";

        when(interceptorCache.getInterceptors(component)).thenReturn(interceptors());
        when(dispatch.apply(inputEnvelope)).thenReturn(null);

        final DefaultInterceptorChainProcessor interceptorChainProcessor = new DefaultInterceptorChainProcessor(interceptorCache, dispatch, component);
        final Optional<JsonEnvelope> result = interceptorChainProcessor.process(interceptorContext);

        assertThat(result, is(Optional.empty()));
    }

    private LinkedList<Interceptor> interceptors() {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(new TestInterceptor());
        return interceptors;
    }

    private static class TestInterceptor implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }
}