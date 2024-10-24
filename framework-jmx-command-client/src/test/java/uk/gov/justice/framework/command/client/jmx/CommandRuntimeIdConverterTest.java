package uk.gov.justice.framework.command.client.jmx;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.justice.framework.command.client.CommandLineException;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommandRuntimeIdConverterTest {

    @InjectMocks
    private CommandRuntimeIdConverter commandRuntimeIdConverter;

    @Test
    public void shouldConvertStringToUuid() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        assertThat(commandRuntimeIdConverter.asUuid(commandRuntimeId.toString()), is(commandRuntimeId));
    }

    @Test
    public void shouldFailIfStringIsNotValidUuid() throws Exception {

        final CommandLineException commandLineException_1 = assertThrows(
                CommandLineException.class,
                () -> commandRuntimeIdConverter.asUuid("something-silly"));

        assertThat(commandLineException_1.getMessage(), is("'--commandRuntimeId' switch 'something-silly' is not a valid uuid"));
        assertThat(commandLineException_1.getCause(), is(instanceOf(IllegalArgumentException.class)));
        assertThat(commandLineException_1.getCause().getMessage(), is("Invalid UUID string: something-silly"));

        final CommandLineException commandLineException_2 = assertThrows(
                CommandLineException.class,
                () -> commandRuntimeIdConverter.asUuid("e6b38acbb68baeefd259d7c61365ba0e2fad4e64"));

        assertThat(commandLineException_2.getMessage(), is("'--commandRuntimeId' switch 'e6b38acbb68baeefd259d7c61365ba0e2fad4e64' is not a valid uuid"));
        assertThat(commandLineException_2.getCause(), is(instanceOf(IllegalArgumentException.class)));
        assertThat(commandLineException_2.getCause().getMessage(), is("UUID string too large"));
    }
}