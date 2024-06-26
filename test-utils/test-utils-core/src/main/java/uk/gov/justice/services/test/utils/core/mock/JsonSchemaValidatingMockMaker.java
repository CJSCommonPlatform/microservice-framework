package uk.gov.justice.services.test.utils.core.mock;

import static java.util.Optional.empty;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherConfiguration;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandler;
import uk.gov.justice.services.core.envelope.EnvelopeValidator;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.envelope.RequestResponseEnvelopeValidator;
import uk.gov.justice.services.core.envelope.RethrowingValidationExceptionHandler;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidatorFactory;
import uk.gov.justice.services.core.json.MediaTypesMappingCacheMock;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.internal.creation.bytebuddy.SubclassByteBuddyMockMaker;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.MockHandler;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.mock.MockCreationSettings;
import org.mockito.plugins.MockMaker;

/**
 * Mockito extension to test if payloads passed to {@link Sender} and returned by {@link Requester},
 * conform to respective json schemas.
 */
public class JsonSchemaValidatingMockMaker implements MockMaker {

    private final MockMaker mockMakerDelegate = new SubclassByteBuddyMockMaker();

    private final EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler = new RethrowingValidationExceptionHandler();
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    private final MediaTypesMappingCacheMock mediaTypesMappingCache = new MediaTypesMappingCacheMock();

    private final EnvelopeValidator envelopeValidator = new EnvelopeValidator(
            new DefaultJsonSchemaValidatorFactory().getDefaultJsonSchemaValidator(),
            objectMapper,
            envelopeValidationExceptionHandler
    );

    private final RequestResponseEnvelopeValidator requestResponseEnvelopeValidator = new RequestResponseEnvelopeValidator(
            envelopeValidator,
            new DefaultNameToMediaTypeConverter(),
            new MediaTypeProvider(mediaTypesMappingCache),
            new EnvelopeInspector(),
            new DispatcherConfiguration());


    @Override
    public <T> T createMock(final MockCreationSettings<T> settings, final MockHandler handler) {
        final List<InvocationListener> invocationListeners = settings.getInvocationListeners();
        if (!shouldSkipValidation(invocationListeners)) {
            invocationListeners.add(this::validateAgainstJsonSchema);
        }

        return mockMakerDelegate.createMock(settings, handler);
    }

    @Override
    public TypeMockability isTypeMockable(final Class<?> aClass) {
        return mockMakerDelegate.isTypeMockable(aClass);
    }

    private boolean shouldSkipValidation(final List<InvocationListener> invocationListeners) {
        return invocationListeners.stream().filter(il -> il instanceof SkipJsonValidationListener).findAny().isPresent();
    }

    @Override
    public MockHandler getHandler(final Object object) {
        return mockMakerDelegate.getHandler(object);
    }

    @Override
    public void resetMock(final Object object, final MockHandler mockHandler, final MockCreationSettings mockCreationSettings) {
        mockMakerDelegate.resetMock(object, mockHandler, mockCreationSettings);
    }

    private void validateAgainstJsonSchema(final MethodInvocationReport methodInvocationReport) {
        envelopeToValidateFrom(methodInvocationReport).ifPresent(requestResponseEnvelopeValidator::validateRequest);
    }

    private Optional<JsonEnvelope> envelopeToValidateFrom(final MethodInvocationReport mockMethodInvocationReport) {

        final Invocation mockInvocation = (Invocation) mockMethodInvocationReport.getInvocation();
        if (mockedClassIs(Sender.class, mockInvocation)) {
            return envelopeOf(mockInvocation.getArguments()[0]);
        } else if (mockedClassIs(Requester.class, mockInvocation)) {
            return envelopeOf(mockMethodInvocationReport.getReturnedValue());
        }
        return empty();

    }

    private Optional<JsonEnvelope> envelopeOf(final Object envelope) {
        return Optional.ofNullable((JsonEnvelope) envelope);
    }

    private boolean mockedClassIs(final Class<?> aClass, final Invocation invocation) {
        return invocation.getMethod().getDeclaringClass().equals(aClass);
    }

}
