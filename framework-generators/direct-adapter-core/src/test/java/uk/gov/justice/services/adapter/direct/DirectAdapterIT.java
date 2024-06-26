package uk.gov.justice.services.adapter.direct;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.schema.catalog.util.ClasspathResourceLoader;
import uk.gov.justice.schema.catalog.util.UriResolver;
import uk.gov.justice.schema.catalog.util.UrlConverter;
import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.cdi.LoggerProducer;
import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
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
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;
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

import java.util.LinkedList;
import java.util.List;

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
public class DirectAdapterIT {


    private static final String GET_USER_ACTION = "people.get-user";

    @Inject
    QueryViewDirectAdapter directAdapter;

    @Inject
    EnvelopeRecordingInterceptor testInterceptor;

    @Inject
    GetUserRecordingHandler testHandler;

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
            TestQueryApiInterceptorChainProvider.class,

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

            QueryViewDirectAdapter.class,
            GetUserRecordingHandler.class,

            JsonSchemaLoader.class,

            UriResolver.class,
            UrlConverter.class,
            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,
            ClasspathResourceLoader.class,

            CatalogProducer.class,
            SchemaCatalogService.class,
            SchemaCatalogResolverProducer.class,

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
                .contextRoot("direct-adapter-test")
                .addServlet("TestApp", Application.class.getName());
    }


    @Test
    public void shouldProcessEnvelopePassedToAdapter() throws Exception {

        final JsonEnvelope envelopePassedToAdapter = envelope()
                .with(metadataWithRandomUUID(GET_USER_ACTION))
                .withPayloadOf("Frederique Blouggheuxs", "nom")
                .build();
        directAdapter.process(envelopePassedToAdapter);

        assertThat(testInterceptor.firstRecordedEnvelope(), is(envelopePassedToAdapter));
        assertThat(testHandler.firstRecordedEnvelope(), is(envelopePassedToAdapter));

    }

    @Test
    public void shouldReturnEnvelopeReturnedByHandler() {
        final JsonEnvelope inputEnvelope = envelope()
                .with(metadataWithRandomUUID(GET_USER_ACTION))
                .withPayloadOf("Friederich Blraugtenhiem", "name")
                .build();
        final JsonEnvelope responseEnvelope = envelope()
                .with(metadataWithDefaults())
                .withPayloadOf("Friederich Blraugtenhiem", "name")
                .build();
        testHandler.setUpResponse(responseEnvelope);

        assertThat(directAdapter.process(inputEnvelope),
                is(responseEnvelope));

    }

    @ApplicationScoped
    public static class TestQueryApiInterceptorChainProvider implements InterceptorChainEntryProvider {

        @Override
        public String component() {
            return QUERY_VIEW;
        }

        @Override
        public List<InterceptorChainEntry> interceptorChainTypes() {
            final List<InterceptorChainEntry> interceptorChainTypes = new LinkedList<>();
            interceptorChainTypes.add(new InterceptorChainEntry(1, EnvelopeRecordingInterceptor.class));
            return interceptorChainTypes;
        }
    }

    @ServiceComponent(QUERY_VIEW)
    @ApplicationScoped
    public static class GetUserRecordingHandler extends TestEnvelopeRecorder {

        private JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();

        public void setUpResponse(final JsonEnvelope responseEnvelope) {

            this.responseEnvelope = responseEnvelope;
        }

        @Handles(GET_USER_ACTION)
        public JsonEnvelope handle(final JsonEnvelope envelope) {
            record(envelope);
            return responseEnvelope;
        }

    }
}
