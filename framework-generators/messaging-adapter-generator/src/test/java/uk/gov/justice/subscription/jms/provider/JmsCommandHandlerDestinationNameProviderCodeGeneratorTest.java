package uk.gov.justice.subscription.jms.provider;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.JavaFile.builder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.compiler.JavaCompilerUtility.javaCompilerUtil;
import static uk.gov.justice.subscription.jms.core.ClassNameFactory.JMS_HANDLER_DESTINATION_NAME_PROVIDER;

import uk.gov.justice.services.generators.subscription.parser.SubscriptionWrapper;
import uk.gov.justice.services.messaging.jms.JmsCommandHandlerDestinationNameProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.jms.core.ClassNameFactory;

import java.io.File;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JmsCommandHandlerDestinationNameProviderCodeGeneratorTest {

    @TempDir
    public File temporaryFolder;


    @InjectMocks
    private JmsCommandHandlerDestinationNameProviderCodeGenerator jmsCommandHandlerDestinationNameProviderCodeGenerator;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldCreateJmsCommandHandlerDestinationNameProvider() throws Exception {

        final String packageName = "uk.gov.justice.api.interceptor.provider";
        final String simpleName = "MyJmsCommandHandlerDestinationNameProvider";
        final String destination = "structure.controller.command";
        final String eventSourceName = "event-source";

        final ClassName jmsCommandHandlerDestinationNameProviderClassName = get(packageName, simpleName);
        final SubscriptionWrapper subscriptionWrapper = mock(SubscriptionWrapper.class);
        final Subscription subscription = mock(Subscription.class);
        final ClassNameFactory classNameFactory = mock(ClassNameFactory.class);
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);

        when(classNameFactory.classNameFor(JMS_HANDLER_DESTINATION_NAME_PROVIDER)).thenReturn(jmsCommandHandlerDestinationNameProviderClassName);

        when(subscription.getEventSourceName()).thenReturn(eventSourceName);
        when(subscriptionWrapper.getEventSourceByName(eventSourceName)).thenReturn(eventSourceDefinition);
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getJmsUri()).thenReturn("jms:topic:" + destination);

        final TypeSpec typeSpec = jmsCommandHandlerDestinationNameProviderCodeGenerator.generate(subscriptionWrapper, subscription, classNameFactory);

        final File outputDirectory = new File(temporaryFolder, "test-generation");
        outputDirectory.mkdirs();
        builder(packageName, typeSpec)
                .build()
                .writeTo(outputDirectory);

        File compilationOutputDir = new File(temporaryFolder, getClass().getSimpleName());
        compilationOutputDir.mkdirs();
        final Class<?> compiledClass = javaCompilerUtil().compiledClassOf(
                outputDirectory,
                compilationOutputDir,
                packageName,
                simpleName);

        final Object newInstance = compiledClass.newInstance();

        assertThat(newInstance instanceof JmsCommandHandlerDestinationNameProvider, is(true));

        final JmsCommandHandlerDestinationNameProvider jmsCommandHandlerDestinationNameProvider = (JmsCommandHandlerDestinationNameProvider) newInstance;

        assertThat(jmsCommandHandlerDestinationNameProvider.destinationName(), is(destination));
    }
}