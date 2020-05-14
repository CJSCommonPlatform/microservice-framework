package uk.gov.justice.services.test.utils.core.matchers;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;

/**
 * Matches a List of JsonEnvelopes.  This is very similar to {@link JsonEnvelopeStreamMatcher} and
 * is used internally by the JsonEnvelopeStreamMatcher.
 *
 * <pre>
 *  {@code
 *      assertThat(eventList, listContaining(
 *              jsonEnvelope(
 *                      metadata()
 *                          .withCausationIds(commandId)
 *                          .withName(EVENT_NAME),
 *                      payloadIsJson(allOf(
 *                          withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
 *                          withJsonPath("$.name", equalTo(RECIPE_NAME)),
 *                          withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
 *                          withJsonPath("$.ingredients", empty())
 *                      ))))
 *      );
 * }
 * </pre>
 */
public class JsonEnvelopeListMatcher extends TypeSafeDiagnosingMatcher<List<JsonEnvelope>> {

    private final Matcher[] matchers;

    public JsonEnvelopeListMatcher(final Matcher... matchers) {
        this.matchers = matchers;
    }

    public static JsonEnvelopeListMatcher listContaining(final Matcher... matchers) {
        return new JsonEnvelopeListMatcher(matchers);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("JsonEnvelope List that contains: ");
        stream(matchers).forEach(description::appendDescriptionOf);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean matchesSafely(final List<JsonEnvelope> jsonEnvelopes, final Description description) {
        final IsIterableContainingInOrder containingInOrder = new IsIterableContainingInOrder(asList(matchers));

        if (!containingInOrder.matches(jsonEnvelopes)) {
            containingInOrder.describeMismatch(jsonEnvelopes, description);
            return false;
        }

        return true;
    }
}