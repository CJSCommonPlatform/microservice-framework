package uk.gov.justice.services.core.mapping;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class DefaultMediaTypesMappingCacheTest {

    @Mock
    private MediaTypesMappingCacheInitialiser mediaTypesMappingCacheInitialiser;


    @InjectMocks
    private DefaultMediaTypesMappingCache defaultMediaTypesMappingCache;


    @Test
    public void shouldReturnMediaTypesForActionName() throws Exception {

        final String actionName = "example.add-recipe";

        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        when(mediaTypesMappingCacheInitialiser.initialiseCache()).thenReturn(of(actionName, new MediaTypes(requestMediaType, responseMediaType)));


        final Optional<MediaTypes> mediaTypes = defaultMediaTypesMappingCache.mediaTypesFor(actionName);

        assertThat(mediaTypes.get().getRequestMediaType().get(), is(requestMediaType));
        assertThat(mediaTypes.get().getResponseMediaType().get(), is(responseMediaType));
    }

    @Test
    public void shouldInititaliseTheCacheOnlyOnce() throws Exception {

        final String actionName = "example.add-recipe";

        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        when(mediaTypesMappingCacheInitialiser.initialiseCache()).thenReturn(of(actionName, new MediaTypes(requestMediaType, responseMediaType)));


        defaultMediaTypesMappingCache.mediaTypesFor(actionName);
        defaultMediaTypesMappingCache.mediaTypesFor(actionName);
        defaultMediaTypesMappingCache.mediaTypesFor(actionName);
        defaultMediaTypesMappingCache.mediaTypesFor(actionName);
        defaultMediaTypesMappingCache.mediaTypesFor(actionName);

        verify(mediaTypesMappingCacheInitialiser, times(1)).initialiseCache();

    }

    @Test
    public void shouldReturnEmptyIfNoMappingFound() throws Exception {

        when(mediaTypesMappingCacheInitialiser.initialiseCache()).thenReturn(new HashMap<>());

        final Optional<MediaTypes> mediaTypes = defaultMediaTypesMappingCache.mediaTypesFor("some-unknown-action");

        assertThat(mediaTypes.isPresent(), is(false));
    }
}
