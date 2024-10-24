package uk.gov.justice.framework.command.client.startup;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.framework.command.client.CommandExecutor;
import uk.gov.justice.framework.command.client.CommandRunModeSelector;
import uk.gov.justice.framework.command.client.JmxParametersFactory;
import uk.gov.justice.framework.command.client.JmxRuntimeParametersFactory;
import uk.gov.justice.framework.command.client.MainApplication;
import uk.gov.justice.framework.command.client.ReturnCodeFactory;
import uk.gov.justice.framework.command.client.cdi.producers.OptionsFactory;
import uk.gov.justice.framework.command.client.io.CommandPrinter;
import uk.gov.justice.framework.command.client.io.ToConsolePrinter;
import uk.gov.justice.framework.command.client.jmx.CommandChecker;
import uk.gov.justice.framework.command.client.jmx.CommandPoller;
import uk.gov.justice.framework.command.client.jmx.CommandRuntimeIdConverter;
import uk.gov.justice.framework.command.client.jmx.ListCommandsInvoker;
import uk.gov.justice.framework.command.client.jmx.SystemCommandInvoker;
import uk.gov.justice.framework.command.client.util.Sleeper;
import uk.gov.justice.framework.command.client.util.UtcClock;
import uk.gov.justice.services.jmx.system.command.client.ConnectorObjectFactory;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClientFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JMXConnectorFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.MBeanConnector;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ObjectFactoryTest {

    @Mock
    private ConnectorObjectFactory connectorObjectFactory;

    @InjectMocks
    private ObjectFactory objectFactory;

    @Test
    public void shouldCreateMainApplication() throws Exception {

        final MainApplication mainApplication = objectFactory.mainApplication();

        assertThat(getValueOfField(mainApplication, "commandLineArgumentParser", CommandLineArgumentParser.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "jmxParametersFactory", JmxParametersFactory.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "listCommandsInvoker", ListCommandsInvoker.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "optionsFactory", OptionsFactory.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "formatter", HelpFormatter.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "commandExecutor", CommandExecutor.class), is(notNullValue()));
        assertThat(getValueOfField(mainApplication, "returnCodeFactory", ReturnCodeFactory.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandLineArgumentParser() throws Exception {

        final CommandLineArgumentParser commandLineArgumentParser = objectFactory.commandLineArgumentParser();

        assertThat(getValueOfField(commandLineArgumentParser, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
        assertThat(getValueOfField(commandLineArgumentParser, "optionsFactory", OptionsFactory.class), is(notNullValue()));
        assertThat(getValueOfField(commandLineArgumentParser, "basicParser", BasicParser.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateJmxParametersFactory() throws Exception {

        final JmxRuntimeParametersFactory jmxRuntimeParametersFactory = objectFactory.jmxRuntimeParametersFactory();
        assertThat(jmxRuntimeParametersFactory, is(notNullValue()));
    }

    @Test
    public void shouldCreateListCommandsInvoker() throws Exception {

        final ListCommandsInvoker listCommandsInvoker = objectFactory.listCommandsInvoker();

        assertThat(getValueOfField(listCommandsInvoker, "systemCommanderClientFactory", SystemCommanderClientFactory.class), is(notNullValue()));
        assertThat(getValueOfField(listCommandsInvoker, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateOptionsFactory() throws Exception {

        final OptionsFactory optionsFactory = objectFactory.optionsFactory();
        assertThat(optionsFactory, is(notNullValue()));
    }

    @Test
    public void shouldCreateUuidConverter() throws Exception {

        final CommandRuntimeIdConverter commandRuntimeIdConverter = objectFactory.uuidConverter();
        assertThat(commandRuntimeIdConverter, is(notNullValue()));
    }

    @Test
    public void shouldCreateJmxRuntimeParametersFactory() throws Exception {

        final JmxRuntimeParametersFactory jmxRuntimeParametersFactory = objectFactory.jmxRuntimeParametersFactory();

        assertThat(getValueOfField(jmxRuntimeParametersFactory, "commandRuntimeIdConverter", CommandRuntimeIdConverter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandExecutor() throws Exception {

        final CommandExecutor commandExecutor = objectFactory.commandExecutor();

        assertThat(getValueOfField(commandExecutor, "systemCommandInvoker", SystemCommandInvoker.class), is(notNullValue()));
        assertThat(getValueOfField(commandExecutor, "commandPrinter", CommandPrinter.class), is(notNullValue()));
        assertThat(getValueOfField(commandExecutor, "commandRunModeSelector", CommandRunModeSelector.class), is(notNullValue()));
        assertThat(getValueOfField(commandExecutor, "jmxRuntimeParametersFactory", JmxRuntimeParametersFactory.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateHelpFormatter() throws Exception {

        final HelpFormatter helpFormatter = objectFactory.helpFormatter();
        assertThat(helpFormatter, is(notNullValue()));
    }

    @Test
    public void shouldCreateReturnCodeFactory() throws Exception {

        final ReturnCodeFactory returnCodeFactory = objectFactory.returnCodeFactory();
        assertThat(getValueOfField(returnCodeFactory, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommanderClientFactory() throws Exception {

        final SystemCommanderClientFactory systemCommanderClientFactory = objectFactory.systemCommanderClientFactory();
        assertThat(getValueOfField(systemCommanderClientFactory, "mBeanConnector", MBeanConnector.class), is(notNullValue()));
        assertThat(getValueOfField(systemCommanderClientFactory, "jmxConnectorFactory", JMXConnectorFactory.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateBasicParser() throws Exception {

        final BasicParser basicParser = objectFactory.basicParser();
        assertThat(basicParser, is(notNullValue()));
    }

    @Test
    public void shouldCreateSystemCommandInvoker() throws Exception {

        final SystemCommandInvoker systemCommandInvoker = objectFactory.systemCommandInvoker();

        assertThat(getValueOfField(systemCommandInvoker, "systemCommanderClientFactory", SystemCommanderClientFactory.class), is(notNullValue()));
        assertThat(getValueOfField(systemCommandInvoker, "commandPoller", CommandPoller.class), is(notNullValue()));
        assertThat(getValueOfField(systemCommandInvoker, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandPoller() throws Exception {

        final CommandPoller commandPoller = objectFactory.commandPoller();

        assertThat(getValueOfField(commandPoller, "commandChecker", CommandChecker.class), is(notNullValue()));
        assertThat(getValueOfField(commandPoller, "clock", UtcClock.class), is(notNullValue()));
        assertThat(getValueOfField(commandPoller, "sleeper", Sleeper.class), is(notNullValue()));
        assertThat(getValueOfField(commandPoller, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandChecker() throws Exception {

        final CommandChecker commandChecker = objectFactory.commandChecker();

        assertThat(getValueOfField(commandChecker, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
        assertThat(getValueOfField(commandChecker, "clock", UtcClock.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateUtcClock() throws Exception {

        final UtcClock utcClock = objectFactory.utcClock();
        assertThat(utcClock, is(notNullValue()));
    }

    @Test
    public void shouldCreateSleeper() throws Exception {

        final Sleeper sleeper = objectFactory.sleeper();
        assertThat(sleeper, is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandPrinter() throws Exception {

        final CommandPrinter commandPrinter = objectFactory.commandPrinter();
        assertThat(getValueOfField(commandPrinter, "toConsolePrinter", ToConsolePrinter.class), is(notNullValue()));
    }

    @Test
    public void shouldCreateCommandRunModeSelector() throws Exception {
        final CommandRunModeSelector commandRunModeSelector = objectFactory.commandRunModeSelector();
        assertThat(commandRunModeSelector, is(notNullValue()));
    }
}