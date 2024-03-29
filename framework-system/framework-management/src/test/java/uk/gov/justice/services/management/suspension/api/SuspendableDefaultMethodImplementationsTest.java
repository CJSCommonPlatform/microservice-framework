package uk.gov.justice.services.management.suspension.api;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;
import uk.gov.justice.services.management.suspension.process.SuspendableWithNoImplementations;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SuspendableDefaultMethodImplementationsTest {

    @Test
    public void shouldReturnClassSimpleNameForTheNameByDefault() throws Exception {
        assertThat(new SuspendableWithNoImplementations().getName(), is(SuspendableWithNoImplementations.class.getSimpleName()));
    }

    @Test
    public void shouldShutterShouldReturnFalseByDefault() throws Exception {
        assertThat(new SuspendableWithNoImplementations().shouldSuspend(), is(false));
    }

    @Test
    public void shouldUnshutterShouldReturnFalseByDefault() throws Exception {
        assertThat(new SuspendableWithNoImplementations().shouldUnsuspend(), is(false));
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionBuDefaultForShutter() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        try {
            new SuspendableWithNoImplementations().suspend(commandId, suspensionCommand);
            fail();
        } catch (final UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), is("Method not implemented"));
        }
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionBuDefaultForUnshutter() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        try {
            new SuspendableWithNoImplementations().unsuspend(commandId, suspensionCommand);
            fail();
        } catch (final UnsupportedOperationException expected) {
            assertThat(expected.getMessage(), is("Method not implemented"));
        }
    }
}
