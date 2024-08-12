package uk.gov.justice.raml.jms.core;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorProperties;
import uk.gov.justice.raml.jms.config.GeneratorPropertiesFactory;
import uk.gov.justice.services.generators.commons.config.CommonGeneratorProperties;
import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.generators.test.utils.builder.ResourceBuilder;

import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raml.model.Resource;

@ExtendWith(MockitoExtension.class)
public class MessageListenerCodeGeneratorTest {

    @InjectMocks
    private MessageListenerCodeGenerator messageListenerCodeGenerator;

    @Test
    public void shouldGenerateMDBForEventProcessorAsQueue() {

        final TypeSpec typeSpec = getTypeSpecForEventProcessor("/queuestructure.event");

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("EVENT_PROCESSOR")
                @javax.ejb.MessageDriven(
                    activationConfig = {
                        @javax.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queuestructure.event"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('ctx.command.defcmd')")
                    }
                )
                @javax.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.Service2EventProcessorMessagingResourceUriJmsLoggerMetadataInterceptor.class,
                    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
                })
                public class Service2EventProcessorMessagingResourceUriJmsListener implements javax.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.Service2EventProcessorMessagingResourceUriJmsListener.class);
                                
                  @javax.inject.Inject
                  uk.gov.justice.services.core.interceptor.InterceptorChainProcessor interceptorChainProcessor;
                                
                  @javax.inject.Inject
                  uk.gov.justice.services.adapter.messaging.JmsProcessor jmsProcessor;
                                
                  @java.lang.Override
                  public void onMessage(javax.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    jmsProcessor.process(interceptorChainProcessor::process, message);
                  }
                }
                """));
    }

    @Test
    public void shouldGenerateMDBForEventProcessorAsTopic() {

        final TypeSpec typeSpec = getTypeSpecForEventProcessor("");

        assertThat(typeSpec.toString(), is("""
                @uk.gov.justice.services.core.annotation.Adapter("EVENT_PROCESSOR")
                @javax.ejb.MessageDriven(
                    activationConfig = {
                        @javax.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "somecontext.controller.command"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CPPNAME in('ctx.command.defcmd')"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
                        @javax.ejb.ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "service2.event.processor.somecontext.controller.command")
                    }
                )
                @javax.interceptor.Interceptors({
                    uk.gov.moj.base.package.name.Service2EventProcessorMessagingResourceUriJmsLoggerMetadataInterceptor.class,
                    uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor.class
                })
                public class Service2EventProcessorMessagingResourceUriJmsListener implements javax.jms.MessageListener {
                  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(uk.gov.moj.base.package.name.Service2EventProcessorMessagingResourceUriJmsListener.class);
                            
                  @javax.inject.Inject
                  uk.gov.justice.services.core.interceptor.InterceptorChainProcessor interceptorChainProcessor;
                            
                  @javax.inject.Inject
                  uk.gov.justice.services.adapter.messaging.JmsProcessor jmsProcessor;
                            
                  @java.lang.Override
                  public void onMessage(javax.jms.Message message) {
                    uk.gov.justice.services.messaging.logging.LoggerUtils.trace(LOGGER, () -> "Received JMS message");
                    jmsProcessor.process(interceptorChainProcessor::process, message);
                  }
                }
                """));
    }

    private TypeSpec getTypeSpecForEventProcessor(final String ramlResourceUri) {
        final String basePackageName = "uk.gov.moj.base.package.name";

        ResourceBuilder resourceBuilder = ResourceBuilder.resource().withDefaultPostAction();
        if (!isBlank(ramlResourceUri)) {
            resourceBuilder = resourceBuilder.withRelativeUri(ramlResourceUri);
        }

        final Resource resource = resourceBuilder.build();

        final MessagingAdapterBaseUri messagingAdapterBaseUri = new MessagingAdapterBaseUri("message://event/processor/message/service2");
        final MessagingResourceUri messagingResourceUri = new MessagingResourceUri("messagingResourceUri");
        final uk.gov.justice.raml.jms.core.ClassNameFactory classNameFactory = new ClassNameFactory(
                messagingAdapterBaseUri,
                messagingResourceUri,
                basePackageName);

        final GeneratorProperties generatorProperties = new GeneratorPropertiesFactory().withServiceComponentOf("EVENT_PROCESSOR");


        return messageListenerCodeGenerator.generate(resource, messagingAdapterBaseUri, (CommonGeneratorProperties) generatorProperties, classNameFactory);
    }
}
