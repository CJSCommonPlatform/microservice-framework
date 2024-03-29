package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultInterceptorChainTest {

    private final LinkedList<Interceptor> interceptors = new LinkedList<>();
    private InterceptorChain interceptorChain;

    @BeforeEach
    public void setup() throws Exception {
        interceptors.offer(new TestInterceptor());

        interceptorChain = new DefaultInterceptorChain(interceptors, new TestTarget());
    }

    @Test
    public void shouldProcessNextInterceptorInTheQueue() throws Exception {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        final InterceptorContext result = interceptorChain.processNext(interceptorContext);

        assertThat(result, is(interceptorContext));
    }

    @Test
    public void shouldSendEachInterceptorContextToTheNextInterceptorInTheQueue() throws Exception {

        final InterceptorContext interceptorContext_1 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_2 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_3 = mock(InterceptorContext.class);

        final Stream<InterceptorContext> interceptorContexts = Stream.of(
                interceptorContext_1,
                interceptorContext_2,
                interceptorContext_3);

        final List<InterceptorContext> results = interceptorChain.processNext(interceptorContexts);

        assertThat(results.size(), is(3));
        assertThat(results, hasItems(
                interceptorContext_1,
                interceptorContext_2,
                interceptorContext_3));
    }

    @Test
    public void shouldCloseStreamIfExceptionOccurs() throws Exception {
        interceptors.offer(new ExceptionThrowingInterceptor());

        final InterceptorContext interceptorContext_1 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_2 = mock(InterceptorContext.class);

        final StreamCloseSpy streamSpy = new StreamCloseSpy();
        final Stream<InterceptorContext> interceptorContexts = Stream.of(
                interceptorContext_1,
                interceptorContext_2)
                .onClose(streamSpy);

        try {
            interceptorChain.processNext(interceptorContexts);
        } catch (TestException e) {
        }

        assertThat(streamSpy.streamClosed(), is(true));

    }

    public static class TestInterceptor implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }
    }

    private static class ExceptionThrowingInterceptor implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            throw new TestException();
        }
    }

    private static class TestException extends RuntimeException {

    }

    public static class TestTarget implements Target {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext) {
            return interceptorContext;
        }
    }
}