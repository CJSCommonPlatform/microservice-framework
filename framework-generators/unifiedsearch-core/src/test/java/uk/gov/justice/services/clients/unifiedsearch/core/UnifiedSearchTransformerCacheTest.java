package uk.gov.justice.services.clients.unifiedsearch.core;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UnifiedSearchTransformerCacheTest {

    @Spy
    private UnifiedSearchFileFinder unifiedSearchFileFinder;

    @Spy
    private FileContentsAsStringLoader fileContentsAsStringLoader;

    @InjectMocks
    private UnifiedSearchTransformerCache unifiedSearchTransformerCache;

    @Test
    public void shouldPopulateCache() {

        final String eventName1 = "example.recipe-added";
        final String eventName2 = "example.recipe-removed";

        unifiedSearchTransformerCache.populateCache();

        final String transformerString1 = unifiedSearchTransformerCache.getTransformerConfigBy(eventName1);
        final String transformerString2 = unifiedSearchTransformerCache.getTransformerConfigBy(eventName2);

        with(transformerString1)
                .assertThat("$.operations[0].operation", is("shift1"))
                .assertThat("$.operations[0].spec.cakeId", is("cakeId1"))
                .assertThat("$.operations[0].spec.recipeId", is("recipeId1"))
                .assertThat("$.operations[0].spec.deliveryDate", is("deliveryDate1"));

        with(transformerString2)
                .assertThat("$.operations[0].operation", is("shift2"))
                .assertThat("$.operations[0].spec.cakeId", is("cakeId2"))
                .assertThat("$.operations[0].spec.recipeId", is("recipeId2"))
                .assertThat("$.operations[0].spec.deliveryDate", is("deliveryDate2"));
    }


}
