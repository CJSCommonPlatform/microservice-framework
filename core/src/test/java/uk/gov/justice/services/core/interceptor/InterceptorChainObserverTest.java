package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InterceptorChainObserverTest {

    @InjectMocks
    private InterceptorChainObserver interceptorChainObserver;

    @Test
    public void shouldRegisterInterceptorChainEntryProvider() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(eq(InterceptorChainEntryProvider.class), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorChainProviderBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }

    @Test
    public void shouldRegisterInterceptor() throws Exception {

        final BeanManager beanManager = mock(BeanManager.class);
        final AfterDeploymentValidation event = mock(AfterDeploymentValidation.class);

        final Bean bean_1 = mock(Bean.class);
        final Bean bean_2 = mock(Bean.class);

        final Set<Bean<?>> beans = new HashSet<>();
        beans.add(bean_1);
        beans.add(bean_2);

        when(beanManager.getBeans(any(), any(AnnotationLiteral.class))).thenReturn(beans);
        when(bean_1.getBeanClass()).thenReturn(Object.class);
        when(bean_2.getBeanClass()).thenReturn(Object.class);

        interceptorChainObserver.afterDeploymentValidation(event, beanManager);

        final List<Bean<?>> interceptorBeans = interceptorChainObserver.getInterceptorBeans();

        assertThat(interceptorBeans, containsInAnyOrder(bean_1, bean_2));
    }
}