package uk.gov.justice.subscription.jms.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.api.subscription.CustomEventListenerExampleEventEventFilter;
import uk.gov.justice.api.subscription.CustomEventListenerExampleEventEventFilterInterceptor;
import uk.gov.justice.api.subscription.CustomEventListenerExampleEventEventInterceptorChainProvider;
import uk.gov.justice.api.subscription.CustomEventListenerExampleEventEventValidationInterceptor;
import uk.gov.justice.api.subscription.CustomEventListenerExampleEventJmsListener;
import uk.gov.justice.api.subscription.CustomEventListenerExampleEventJmsLoggerMetadataInterceptor;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventEventFilter;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventEventFilterInterceptor;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventEventInterceptorChainProvider;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventEventValidationInterceptor;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventJmsListener;
import uk.gov.justice.api.subscription.CustomEventListenerPeopleEventJmsLoggerMetadataInterceptor;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.services.adapter.messaging.DefaultJmsParameterChecker;
import uk.gov.justice.services.adapter.messaging.DefaultJmsProcessor;
import uk.gov.justice.services.adapter.messaging.DefaultSubscriptionJmsProcessor;
import uk.gov.justice.services.adapter.messaging.JmsLoggerMetadataAdder;
import uk.gov.justice.services.adapter.messaging.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.messaging.MdcWrapper;
import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherConfiguration;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;
import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.core.producers.EnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequestResponseEnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequesterProducer;
import uk.gov.justice.services.core.producers.SenderProducer;
import uk.gov.justice.services.event.buffer.api.AllowAllEventFilter;
import uk.gov.justice.services.generators.test.utils.interceptor.EnvelopeRecorder;
import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsMessagingConfiguration;
import uk.gov.justice.services.messaging.jms.OversizeMessageGuard;
import uk.gov.justice.services.messaging.logging.DefaultJmsMessageLoggerHelper;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.services.test.utils.core.handler.registry.TestHandlerRegistryCacheProducer;
import uk.gov.justice.services.test.utils.messaging.jms.DummyJmsEnvelopeSender;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Topic;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@RunWithApplicationComposer
public class JmsEndpointGenerationCustomIT extends AbstractJmsAdapterGenerationIT {
    @Module
    @Classes(cdi = true, value = {

            CustomEventListenerPeopleEventEventFilter.class,
            CustomEventListenerPeopleEventEventFilterInterceptor.class,
            CustomEventListenerPeopleEventEventInterceptorChainProvider.class,
            CustomEventListenerPeopleEventEventValidationInterceptor.class,
            CustomEventListenerPeopleEventJmsListener.class,
            CustomEventListenerPeopleEventJmsLoggerMetadataInterceptor.class,

            CustomEventListenerExampleEventEventFilter.class,
            CustomEventListenerExampleEventEventFilterInterceptor.class,
            CustomEventListenerExampleEventEventInterceptorChainProvider.class,
            CustomEventListenerExampleEventEventValidationInterceptor.class,
            CustomEventListenerExampleEventJmsListener.class,
            CustomEventListenerExampleEventJmsLoggerMetadataInterceptor.class,

            RecordingJsonSchemaValidator.class,

            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            DefaultJmsProcessor.class,
            SenderProducer.class,
            DummyJmsEnvelopeSender.class,
            DefaultEnvelopeConverter.class,
            JsonSchemaValidationInterceptor.class,
            DefaultJmsParameterChecker.class,
            TestServiceContextNameProvider.class,
            JsonSchemaLoader.class,
            SchemaCatalogResolverProducer.class,
            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapperProducer.class,
            Enveloper.class,
            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            DefaultAccessControlService.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            EnvelopePayloadTypeConverter.class,
            JsonEnvelopeRepacker.class,
            PolicyEvaluator.class,
            LoggerProducer.class,
            AllowAllEventFilter.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,
            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            DefaultJmsMessageLoggerHelper.class,
            DefaultTraceLogger.class,

            DefaultJsonValidationLoggerHelper.class,

            DefaultNameToMediaTypeConverter.class,
            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,
            SchemaIdMappingObserver.class,

            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class,

            SenderProducer.class,
            MediaTypeProvider.class,
            EnvelopeValidator.class,
            EnvelopeInspector.class,
            RequesterProducer.class,

            RecordingSubscriptionManager.class,
            InterceptorChainProcessor.class,
            DefaultInterceptorChainProcessor.class,
            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            DispatcherCache.class,

            DefaultSubscriptionJmsProcessor.class,
            TestSubscriptionManagerProducer.class,
            RecordingSubscriptionManager.class,

            DefaultEventSourceDefinitionFactory.class,
            JmsLoggerMetadataAdder.class,
            ComponentNameExtractor.class,

            JndiAppNameProvider.class,

            DispatcherConfiguration.class,
            FeatureControlAnnotationFinder.class,
            TestHandlerRegistryCacheProducer.class,

            RequestResponseEnvelopeValidatorFactory.class,
            EnvelopeValidatorFactory.class,

            MdcWrapper.class,

            OversizeMessageGuard.class,
            JmsMessagingConfiguration.class,
            ValueProducer.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("subscription.JmsEndpointGenerationCustomIT");
    }

    @Inject
    RecordingSubscriptionManager recordingSubscriptionManager;

    @Resource(name = "people.event")
    private Topic peopleEventsDestination;

    @Resource(name = "example.event")
    private Topic exampleEventDestination;

    @BeforeEach
    public void setup() throws Exception {
        cleanQueue(peopleEventsDestination);
        cleanQueue(exampleEventDestination);
    }

    @Test
    public void eventListenerDispatcherShouldReceiveCustomEventSpecifiedInMessageSelector() throws Exception {

        final String metadataId = randomUUID().toString();
        final String eventName = "people.eventbb";

        sendEnvelope(metadataId, eventName, peopleEventsDestination);

        final JsonEnvelope receivedEnvelope = recordingSubscriptionManager.awaitForEnvelopeWithMetadataOf("id", metadataId);
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(eventName));
    }

    @Test
    public void eventListenerDispatcherShouldReceiveCustomExampleEventSpecifiedInMessageSelector() throws Exception {

        final String metadataId = randomUUID().toString();
        final String eventName = "example.eventaa";

        sendEnvelope(metadataId, eventName, exampleEventDestination);

        final JsonEnvelope receivedEnvelope = recordingSubscriptionManager.awaitForEnvelopeWithMetadataOf("id", metadataId);
        assertThat(receivedEnvelope.metadata().id(), is(UUID.fromString(metadataId)));
        assertThat(receivedEnvelope.metadata().name(), is(eventName));
    }

    @ApplicationScoped
    public static class TestSubscriptionManagerProducer {

        @Inject
        RecordingSubscriptionManager recordingSubscriptionManager;

        @Produces
        @SubscriptionName
        public SubscriptionManager subscriptionManager() {
            return recordingSubscriptionManager;
        }
    }

    @ApplicationScoped
    public static class RecordingSubscriptionManager extends EnvelopeRecorder implements SubscriptionManager {

        @Override
        public void process(final JsonEnvelope jsonEnvelope) {
            record(jsonEnvelope);
        }
    }

    @ApplicationScoped
    public static class RecordingJsonSchemaValidator implements JsonSchemaValidator {

        private String validatedEventName;

        @Override
        public void validate(final String payload, final String actionName) {
            this.validatedEventName = actionName;
        }

        @Override
        public void validate(final String payload, final String actionName, final Optional<MediaType> mediaType) {
            this.validatedEventName = actionName;
        }

        public String validatedEventName() {
            return validatedEventName;
        }
    }

    @ApplicationScoped
    public static class TestServiceContextNameProvider implements ServiceContextNameProvider {

        @Override
        public String getServiceContextName() {
            return "test-component";
        }
    }
}
