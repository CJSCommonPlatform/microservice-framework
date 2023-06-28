package uk.gov.justice.services.core.json;

import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCache;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

/**
 * Service for validating JSON payloads against a schema contained in a catalog.
 */
@ApplicationScoped
public class SchemaCatalogAwareJsonSchemaValidator {

    @Inject
    FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Inject
    SchemaIdMappingCache schemaIdMappingCache;

    @Inject
    SchemaCatalogService schemaCatalogService;

    @Inject
    PayloadExtractor payloadExtractor;

    /**
     * Validate a JSON payload against a schema contained in the schema catalog for the given
     * message type name. If the JSON contains metadata, this is removed first.  If no schema for
     * the media type can be found then it falls back to checking for schemas on the class path.
     *
     * @param envelopeJson      the payload to validate
     * @param actionName        the action name
     * @param mediaTypeOptional the message type
     */
    public void validate(final String envelopeJson, final String actionName, final Optional<MediaType> mediaTypeOptional) {

        mediaTypeOptional.ifPresent(mediaType -> doValidate(envelopeJson, actionName, mediaType));
    }

    private void doValidate(final String envelopeJson, final String actionName, final MediaType mediaType) {

        final Optional<Schema> schema = schemaIdMappingCache.schemaIdFor(mediaType).flatMap(schemaCatalogService::findSchema);

        if (schema.isPresent()) {
            final JSONObject payload = payloadExtractor.extractPayloadFrom(envelopeJson);
            try {
                schema.get().validate(payload);
            } catch (final ValidationException ex) {
                throw new JsonSchemaValidationException(ex.getMessage(), ex);
            }
        } else {
            fileBasedJsonSchemaValidator.validateWithoutSchemaCatalog(envelopeJson, actionName);
        }
    }
}
