package uk.gov.justice.services.core.audit;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.interceptor.DefaultInterceptorChain;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.core.interceptor.Target;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Deque;
import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LocalAuditInterceptorTest {

    private static final String COMPONENT = "test-component";
    private static final String UNKNOWN_COMPONENT = "UNKNOWN";

    @Mock
    private JsonEnvelope inputEnvelope;

    @Mock
    private JsonEnvelope outputEnvelope;

    @InjectMocks
    private LocalAuditInterceptor localAuditInterceptor;

    @Mock
    private AuditService auditService;

    private InterceptorChain interceptorChain;

    @BeforeEach
    public void setup() throws Exception {
        final Deque<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(localAuditInterceptor);

        final Target target = context -> context.copyWithOutput(outputEnvelope);

        interceptorChain = new DefaultInterceptorChain(interceptors, target);
    }

    @Test
    public void shouldApplyAccessControlToInputIfLocalComponent() throws Exception {
        final InterceptorContext inputContext = interceptorContextWithInput(inputEnvelope);
        inputContext.setInputParameter("component", COMPONENT);

        interceptorChain.processNext(inputContext);

        verify(auditService).audit(inputEnvelope, COMPONENT);
        verify(auditService).audit(outputEnvelope, COMPONENT);
    }

    @Test
    public void shouldUseUnknownComponentIfComponentNotSet() throws Exception {
        final InterceptorContext inputContext = interceptorContextWithInput(inputEnvelope);

        interceptorChain.processNext(inputContext);

        verify(auditService).audit(inputEnvelope, UNKNOWN_COMPONENT);
        verify(auditService).audit(outputEnvelope, UNKNOWN_COMPONENT);
    }

    @Adapter(COMMAND_API)
    public static class TestCommandLocal {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }

    public static class TestCommandRemote {
        @Inject
        InterceptorChainProcessor interceptorChainProcessor;

        public void dummyMethod() {

        }
    }
}
