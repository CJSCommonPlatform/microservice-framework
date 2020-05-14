package uk.gov.justice.services.metrics.interceptor;


import static java.lang.String.format;

import uk.gov.justice.services.core.interceptor.InterceptorContext;

public class IndividualActionMetricsInterceptor extends AbstractMetricsInterceptor {

    @Override
    protected String timerNameOf(final InterceptorContext interceptorContext) {
        return format("%s.action.%s", componentName(interceptorContext), interceptorContext.inputEnvelope().metadata().name());
    }
}