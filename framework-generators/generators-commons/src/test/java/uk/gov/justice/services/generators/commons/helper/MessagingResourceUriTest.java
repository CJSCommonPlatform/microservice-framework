package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

public class  MessagingResourceUriTest {

    @Test
    public void shouldReturnStringRepresentation() {
        assertThat(new MessagingResourceUri("/contextAbc.handler.command").toString(), is("/contextAbc.handler.command"));
        assertThat(new MessagingResourceUri("/context2.event").toString(), is("/context2.event"));
    }

    @Test
    public void shouldReturnCapitalisedStringRepresentation() {
        assertThat(new MessagingResourceUri("/people.handler.command").toClassName(), is("PeopleHandlerCommand"));
        assertThat(new MessagingResourceUri("/public.event").toClassName(), is("PublicEvent"));
        assertThat(new MessagingResourceUri("/system-scheduling.handler.command").toClassName(), is("SystemSchedulingHandlerCommand"));
        assertThat(new MessagingResourceUri(" /9system-scheduling2.handler.command").toClassName(), is("SystemScheduling2HandlerCommand"));
        assertThat(new MessagingResourceUri("/89system-scheduling2plus.handler.command").toClassName(), is("SystemScheduling2PlusHandlerCommand"));
        assertThat(new MessagingResourceUri("/systemScheduling.handler.command").toClassName(), is("SystemSchedulingHandlerCommand"));
        assertThat(new MessagingResourceUri("/system$scheduling.handler_.command").toClassName(), is("System$SchedulingHandler_Command"));
        assertThat(new MessagingResourceUri("_system$scheduling.handler_.command").toClassName(), is("_System$SchedulingHandler_Command"));
    }

    @Test
    public void shouldReturnHyphenatedStringRepresentation() {
        assertThat(new MessagingResourceUri("/people.handler.command").hyphenated(), is("people-handler-command"));
        assertThat(new MessagingResourceUri("/public.event").hyphenated(), is("public-event"));
        assertThat(new MessagingResourceUri("/system-scheduling.handler.command").hyphenated(), is("system-scheduling-handler-command"));
        assertThat(new MessagingResourceUri(" /9system-scheduling2.handler.command").hyphenated(), is("9system-scheduling2-handler-command"));
        assertThat(new MessagingResourceUri("/systemScheduling.handler.command").hyphenated(), is("systemScheduling-handler-command"));

    }
}
