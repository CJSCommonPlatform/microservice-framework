package uk.gov.justice.services.test.utils.common.helper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.common.util.Clock;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link StoppedClock} class.
 */
public class StoppedClockTest {

    @Test
    public void shouldReturnProvidedDateTime() {
        final ZonedDateTime now = ZonedDateTime.now();

        final Clock clock = new StoppedClock(now);

        assertThat(clock.now(), is(now));
    }
}
