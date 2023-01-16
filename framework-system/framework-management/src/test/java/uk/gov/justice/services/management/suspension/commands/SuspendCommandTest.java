package uk.gov.justice.services.management.suspension.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SuspendCommandTest {

    @InjectMocks
    private SuspendCommand suspendCommand;

    @Test
    public void shouldBeSuspendCommand() throws Exception {

        assertThat(suspendCommand.isSuspendCommand(), is(true));
    }
}
