package uk.gov.justice.services.core.interceptor;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.AnyLiteral;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.slf4j.Logger;

/**
 * Observes for {@link AfterDeploymentValidation} and adds all {@link InterceptorChainEntryProvider}
 * implementations to the {@link InterceptorCache}
 */
public class InterceptorChainObserver implements Extension {

    private static final Logger LOGGER = getLogger(InterceptorChainObserver.class);

    private final List<Bean<?>> interceptorChainProviderBeans = new ArrayList<>();
    private final List<Bean<?>> interceptorBeans = new ArrayList<>();

    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        beanManager.getBeans(InterceptorChainEntryProvider.class, AnyLiteral.create()).stream()
                .peek(this::logInterceptorChainProvider)
                .forEach(interceptorChainProviderBeans::add);

        beanManager.getBeans(Interceptor.class, AnyLiteral.create()).stream()
                .peek(this::logInterceptor)
                .forEach(interceptorBeans::add);
    }

    List<Bean<?>> getInterceptorChainProviderBeans() {
        return interceptorChainProviderBeans;
    }

    List<Bean<?>> getInterceptorBeans() {
        return interceptorBeans;
    }

    private void logInterceptorChainProvider(final Bean<?> bean) {
        LOGGER.info("Found interceptor chain provider {}", bean.getBeanClass().getSimpleName());
    }

    private void logInterceptor(final Bean<?> bean) {
        LOGGER.info("Found interceptor {}", bean.getBeanClass().getSimpleName());
    }
}
