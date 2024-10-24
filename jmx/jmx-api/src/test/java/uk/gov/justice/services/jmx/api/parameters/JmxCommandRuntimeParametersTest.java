package uk.gov.justice.services.jmx.api.parameters;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.newOutputStream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.JmxCommandRuntimeParametersBuilder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JmxCommandRuntimeParametersTest {

    @TempDir
    private Path tempDir;

    @Test
    public void shouldBuildJmxCommandParameters() throws Exception {

        final UUID commandRuntimeId = randomUUID();
        final String comandRuntimeString = "some-command-runtime-string";

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .withCommandRuntimeString(comandRuntimeString)
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(of(commandRuntimeId)));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(of(comandRuntimeString)));
    }

    @Test
    public void shouldReturnEmptyCommandRuntimeIdIfNotSetInTheBuilder() throws Exception {

        final String comandRuntimeString = "some-command-runtime-string";

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeString(comandRuntimeString)
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(empty()));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(of(comandRuntimeString)));
    }

    @Test
    public void shouldReturnEmptyCommandRuntimeStringIfNotSetInTheBuilder() throws Exception {

        final UUID commandRuntimeId = randomUUID();

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeId(), is(of(commandRuntimeId)));
        assertThat(jmxCommandRuntimeParameters.getCommandRuntimeString(), is(empty()));
    }

    @Test
    public void shouldCreateEmptyJmxParameters() throws Exception {
        final JmxCommandRuntimeParameters emptyJmxCommandRuntimeParameters = JmxCommandRuntimeParameters.empty();
        assertThat(emptyJmxCommandRuntimeParameters.getCommandRuntimeId(), is(empty()));
        assertThat(emptyJmxCommandRuntimeParameters.getCommandRuntimeString(), is(empty()));
    }

    @Test
    public void shouldGenerateCorrectToStringForAllParameters() throws Exception {

        final UUID commandRuntimeId = fromString("5f45c300-2da2-4a84-a05e-d8a43db4a50c");
        final String comandRuntimeString = "some-command-runtime-string";

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .withCommandRuntimeString(comandRuntimeString)
                .build();

        assertThat(jmxCommandRuntimeParameters.toString(), is("JmxCommandRuntimeParameters{commandRuntimeId='5f45c300-2da2-4a84-a05e-d8a43db4a50c', commandRuntimeString='some-command-runtime-string'}"));
    }

    @Test
    public void shouldGenerateCorrectToStringIfNoCommandRuntimeIdSupplied() throws Exception {

        final String comandRuntimeString = "some-command-runtime-string";

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeString(comandRuntimeString)
                .build();

        assertThat(jmxCommandRuntimeParameters.toString(), is("JmxCommandRuntimeParameters{commandRuntimeString='some-command-runtime-string'}"));
    }

    @Test
    public void shouldGenerateCorrectToStringIfNoCommandRuntimeStringSupplied() throws Exception {

        final UUID commandRuntimeId = fromString("5f45c300-2da2-4a84-a05e-d8a43db4a50c");

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .build();

        assertThat(jmxCommandRuntimeParameters.toString(), is("JmxCommandRuntimeParameters{commandRuntimeId='5f45c300-2da2-4a84-a05e-d8a43db4a50c'}"));
    }

    @Test
    public void shouldGenerateCorrectToStringIfNoParametersProvided() throws Exception {

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .build();

        assertThat(jmxCommandRuntimeParameters.toString(), is("JmxCommandRuntimeParameters{}"));
    }

    @Test
    public void shouldSerializeJmxCommandRuntimeParameters() throws Exception {

        final UUID commandRuntimeId = fromString("5f45c300-2da2-4a84-a05e-d8a43db4a50c");
        final String comandRuntimeString = "some-command-runtime-string";

        final JmxCommandRuntimeParameters jmxCommandRuntimeParameters = new JmxCommandRuntimeParametersBuilder()
                .withCommandRuntimeId(commandRuntimeId)
                .withCommandRuntimeString(comandRuntimeString)
                .build();

        final Path tempFile = createFile(tempDir.resolve("delete-me.dat"));

        try(final ObjectOutputStream objectOutputStream = new ObjectOutputStream(newOutputStream(tempFile))) {
            objectOutputStream.writeObject(jmxCommandRuntimeParameters);
        }

        try(final ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(tempFile))) {
            final JmxCommandRuntimeParameters deserializedJmxCommandRuntimeParameters = (JmxCommandRuntimeParameters) objectInputStream.readObject();

            assertThat(deserializedJmxCommandRuntimeParameters, is(jmxCommandRuntimeParameters));
        }
    }
}