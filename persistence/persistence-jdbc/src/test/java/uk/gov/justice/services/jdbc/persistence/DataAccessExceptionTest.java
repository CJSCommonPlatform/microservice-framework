package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class DataAccessExceptionTest {

    @Test
    public void shouldCreateInstanceOfDataAccessExceptionWithMessageAndCause() throws Exception {
        final Throwable throwable = mock(Throwable.class);
        final DataAccessException exception = new DataAccessException("Test message", throwable);
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception.getCause(), is(throwable));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}