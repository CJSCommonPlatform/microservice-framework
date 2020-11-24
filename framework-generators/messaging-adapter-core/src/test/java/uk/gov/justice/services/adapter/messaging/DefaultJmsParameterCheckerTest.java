package uk.gov.justice.services.adapter.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultJmsParameterCheckerTest {

    @Test
    public void shouldThrowExceptionIfMoreThanOneParameter() throws Exception {

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
                new DefaultJmsParameterChecker().check(new Object[]{new Object(), new Object()})
        );

        assertThat(illegalArgumentException.getMessage(), is("Can only be used on single argument methods"));
    }

    @Test
    public void shouldThrowExceptionIfLessThanOneParameter() throws Exception {

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
                new DefaultJmsParameterChecker().check(new Object[]{})
        );

        assertThat(illegalArgumentException.getMessage(), is("Can only be used on single argument methods"));
    }

    @Test
    public void shouldThrowExceptionIfSingleParameterIsNotTextMessageObject() throws Exception {

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
                new DefaultJmsParameterChecker().check(new Object[]{new Object()})
        );

        assertThat(illegalArgumentException.getMessage(), is("Can only be used on a JMS TextMessage, not java.lang.Object"));
    }

    @Test
    public void shouldNotThrowExceptionIfSingleParameterIsTextMessageObject() throws Exception {
        new DefaultJmsParameterChecker().check(new Object[]{mock(TextMessage.class)});
    }
}