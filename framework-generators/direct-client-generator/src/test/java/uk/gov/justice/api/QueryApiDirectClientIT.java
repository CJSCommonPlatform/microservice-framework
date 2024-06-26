package uk.gov.justice.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapterCache;
import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
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
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.featurecontrol.FeatureControlAnnotationFinder;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainObserver;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaValidationErrorMessageGenerator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.core.producers.EnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequestResponseEnvelopeValidatorFactory;
import uk.gov.justice.services.core.producers.RequesterProducer;
import uk.gov.justice.services.core.producers.SenderProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsMessagingConfiguration;
import uk.gov.justice.services.messaging.jms.OversizeMessageGuard;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.common.envelope.EnvelopeRecordingInterceptor;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;
import uk.gov.justice.services.test.utils.common.validator.DummyJsonSchemaValidator;
import uk.gov.justice.services.test.utils.core.handler.registry.TestHandlerRegistryCacheProducer;
import uk.gov.justice.services.test.utils.messaging.jms.DummyJmsEnvelopeSender;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.Test;

@RunWithApplicationComposer
@FrameworkComponent("QUERY_API")
public class QueryApiDirectClientIT {

    private static final String GET_USER_ACTION = "people.get-user";

    @Module
    @Classes(cdi = true, value = {
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            FileBasedJsonSchemaValidator.class,
            DefaultEnvelopeConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultTraceLogger.class,
            EnvelopeValidationExceptionHandlerProducer.class,

            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EnvelopeRecordingInterceptor.class,

            SenderProducer.class,
            DummyJmsEnvelopeSender.class,
            EnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectMapper.class,

            DispatcherCache.class,
            DispatcherFactory.class,
            EnvelopePayloadTypeConverter.class,
            JsonEnvelopeRepacker.class,
            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,

            JndiBasedServiceContextNameProvider.class,
            ValueProducer.class,
            GlobalValueProducer.class,

            JsonSchemaLoader.class,

            DirectQueryApi2QueryViewRestExampleClient.class,
            SynchronousDirectAdapterCache.class,
            TestDirectAdapter.class,

            SchemaCatalogService.class,
            SchemaCatalogResolverProducer.class,
            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,

            CatalogProducer.class,
            SchemaCatalogService.class,

            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,
            MediaTypeProvider.class,
            DummyJsonSchemaValidator.class,
            EnvelopeInspector.class,

            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class,

            DefaultEventSourceDefinitionFactory.class,
            ComponentNameExtractor.class,
            JndiAppNameProvider.class,

            SchemaValidationErrorMessageGenerator.class,

            DispatcherConfiguration.class,
            FeatureControlAnnotationFinder.class,
            TestHandlerRegistryCacheProducer.class,

            RequestResponseEnvelopeValidatorFactory.class,
            EnvelopeValidatorFactory.class,

            OversizeMessageGuard.class,
            JmsMessagingConfiguration.class,
            ValueProducer.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("direct-client-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Inject
    Requester requester;

    @Inject
    TestDirectAdapter testDirectAdapter;

    @Test
    public void shouldPassEnvelopeToAdapter() throws Exception {

        final JsonEnvelope envelopePassedToAdapter = envelope()
                .with(metadataWithRandomUUID(GET_USER_ACTION))
                .withPayloadOf("Freddy O'Bloggs", "name")
                .build();
        requester.request(envelopePassedToAdapter);

        assertThat(testDirectAdapter.firstRecordedEnvelope(), is(envelopePassedToAdapter));

    }


    @Test
    public void shouldReturnEnvelopeReturnedByAdapter() {
        final JsonEnvelope inputEnvelope = envelope()
                .with(metadataWithRandomUUID(GET_USER_ACTION))
                .withPayloadOf("Mavis Davis", "name")
                .build();
        final JsonEnvelope responseEnvelope = envelope()
                .with(metadataWithDefaults())
                .withPayloadOf("Mavis Davis", "name")
                .build();
        testDirectAdapter.setUpResponse(responseEnvelope);

        assertThat(requester.request(inputEnvelope),
                is(responseEnvelope));

    }

    @DirectAdapter("QUERY_VIEW")
    @ApplicationScoped
    public static class TestDirectAdapter extends TestEnvelopeRecorder implements SynchronousDirectAdapter {

        private JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();

        public void setUpResponse(final JsonEnvelope responseEnvelope) {
            this.responseEnvelope = responseEnvelope;
        }

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            record(envelope);
            return responseEnvelope;
        }
    }
}
