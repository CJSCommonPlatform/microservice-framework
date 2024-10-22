package uk.gov.justice.framework.command.client.jmx;

import static java.lang.String.format;
import static java.time.Duration.between;

import uk.gov.justice.framework.command.client.io.ToConsolePrinter;
import uk.gov.justice.framework.command.client.util.Sleeper;
import uk.gov.justice.framework.command.client.util.UtcClock;
import uk.gov.justice.services.jmx.api.mbean.JmxCommandMBean;

import java.time.ZonedDateTime;
import java.util.UUID;

public class CommandPoller {

    private final CommandChecker commandChecker;
    private final UtcClock clock;
    private final Sleeper sleeper;
    private final ToConsolePrinter toConsolePrinter;

    public CommandPoller(
            final CommandChecker commandChecker,
            final UtcClock clock,
            final Sleeper sleeper,
            final ToConsolePrinter toConsolePrinter) {
        this.commandChecker = commandChecker;
        this.clock = clock;
        this.sleeper = sleeper;
        this.toConsolePrinter = toConsolePrinter;
    }

    public void runUntilComplete(final JmxCommandMBean jmxCommandMBean, final UUID commandId, final String commandName) {

        final ZonedDateTime startTime = clock.now();

        int count = 0;
        while (! commandChecker.commandComplete(jmxCommandMBean, commandId, startTime)) {
            sleeper.sleepFor(1_000);
            count++;

            if (count % 10 == 0) {
                final long seconds = between(startTime, clock.now()).getSeconds();
                toConsolePrinter.println(format("%s running for %d seconds", commandName, seconds));
            }
        }
    }

}
