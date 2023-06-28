package uk.gov.justice.services.adapter.rest.envelope;

import static jakarta.ws.rs.core.MediaType.CHARSET_PARAMETER;
import static java.nio.charset.Charset.defaultCharset;

import java.util.Map;

import jakarta.ws.rs.core.MediaType;

public final class MediaTypes {

    public static final String JSON_MEDIA_TYPE_SUFFIX = "+json";

    private MediaTypes() {
    }

    public static String charsetFrom(final MediaType mediaType) {
        final Map<String, String> params = mediaType.getParameters();
        return params.containsKey(CHARSET_PARAMETER) ? params.get(CHARSET_PARAMETER) : defaultCharset().name();
    }
}
