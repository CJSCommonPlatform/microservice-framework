package uk.gov.justice.services.core.extension;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.core.annotation.AnyLiteral;
import uk.gov.justice.services.core.annotation.CustomServiceComponent;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import org.slf4j.Logger;

/**
 * Scans all beans and processes framework specific annotations.
 */
public class ServiceComponentScanner implements Extension {

    private static final Logger LOGGER = getLogger(ServiceComponentScanner.class);

    @SuppressWarnings("unused")
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> processAnnotatedType, final BeanManager beanManager) {
        final AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Event.class)) {
            beanManager
                    .getEvent()
                    .select(EventFoundEvent.class)
                    .fire(new EventFoundEvent(
                            annotatedType.getJavaClass(),
                            annotatedType.getAnnotation(Event.class).value()));
        }
    }

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {

        final Set<Bean<?>> directAdapters = beanManager.getBeans(SynchronousDirectAdapter.class);
        allBeansFrom(beanManager)
                .filter(this::isServiceComponent)
                .filter(bean -> isNotDirectComponentWithoutAdapter(bean, directAdapters))
                .forEach(bean -> processServiceComponentsForEvents(bean, beanManager));
    }

    private Stream<Bean<?>> allBeansFrom(final BeanManager beanManager) {
        return beanManager.getBeans(Object.class, AnyLiteral.create()).stream();
    }

    private boolean isServiceComponent(final Bean<?> bean) {
        return isServiceComponent(bean.getBeanClass());
    }

    private boolean isServiceComponent(final Class<?> beanClass) {
        return beanClass.isAnnotationPresent(ServiceComponent.class)
                || beanClass.isAnnotationPresent(FrameworkComponent.class)
                || beanClass.isAnnotationPresent(CustomServiceComponent.class);
    }

    /**
     * Processes bean for annotations and adds events to the list.
     *
     * @param bean a bean that has an annotation and could be of interest to the framework wiring.
     */
    private void processServiceComponentsForEvents(final Bean<?> bean, final BeanManager beanManager) {

        final ComponentNameExtractor componentNameExtractor = new ComponentNameExtractor();
        final Class<?> beanClass = bean.getBeanClass();

        LOGGER.info("Found class {} as part of component {}", beanClass.getSimpleName(), componentNameExtractor.componentFrom(beanClass));

        beanManager
                .getEvent()
                .select(ServiceComponentFoundEvent.class)
                .fire(new ServiceComponentFoundEvent(
                        componentNameExtractor.componentFrom(beanClass),
                        bean,
                        componentLocationFrom(beanClass)));
    }

    private boolean isNotDirectComponentWithoutAdapter(final Bean<?> bean, final Set<Bean<?>> directAdapters) {

        final Class<?> beanClass = bean.getBeanClass();

        if (beanClass.isAnnotationPresent(Direct.class)) {
            final String targetComponentName = beanClass.getAnnotation(Direct.class).target();
            final Optional<Bean<?>> matchingAdapter = directAdapters.stream()
                    .filter(directAdapter -> directAdapter.getBeanClass().getAnnotation(DirectAdapter.class).value().equals(targetComponentName))
                    .findAny();
            return matchingAdapter.isPresent();
        }

        return true;
    }
}
