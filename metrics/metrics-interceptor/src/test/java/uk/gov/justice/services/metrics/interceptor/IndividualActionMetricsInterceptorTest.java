package uk.gov.justice.services.metrics.interceptor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndividualActionMetricsInterceptorTest {

    @Mock
    private MetricRegistry metricsRegistry;

    @Mock
    private InterceptorChain interceptorChain;

    @Mock
    private Timer timer;

    @Mock
    private Timer.Context timerContext;

    @InjectMocks
    private IndividualActionMetricsInterceptor interceptor;


    @Test
    public void shouldGetTimerFromRegistryByContextName() {

        when(metricsRegistry.timer("someCtxName.action.actionNameABC")).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        final InterceptorContext interceptorContext = interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("actionNameABC")).build());
        interceptorContext.setInputParameter("component", "someCtxName");

        interceptor.process(interceptorContext, interceptorChain);

        verify(metricsRegistry).timer("someCtxName.action.actionNameABC");
    }

    @Test
    public void shouldStartAndStopTimer() throws Exception {

        when(metricsRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        final InterceptorContext currentContext = interceptorContextWithInput(envelope().with(metadataWithDefaults()).build());

        interceptor.process(currentContext, interceptorChain);

        final InOrder inOrder = Mockito.inOrder(timer, interceptorChain, timerContext);
        inOrder.verify(timer).time();
        inOrder.verify(interceptorChain).processNext(currentContext);
        inOrder.verify(timerContext).stop();

    }

}