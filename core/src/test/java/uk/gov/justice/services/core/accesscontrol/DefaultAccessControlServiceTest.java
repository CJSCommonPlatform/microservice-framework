package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService.ACCESS_CONTROL_DISABLED_PROPERTY;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DefaultAccessControlServiceTest {

    private static final String ACTION_NAME = "action-name";

    @Mock
    private PolicyEvaluator policyEvaluator;

    @Mock
    private Logger logger;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @InjectMocks
    private DefaultAccessControlService accessControlService;

    @AfterEach
    public void resetSystemProperty() {
        System.clearProperty(ACCESS_CONTROL_DISABLED_PROPERTY);
    }

    @Test
    public void shouldDelegateTheAccessControlLogicToTheAccessController() throws Exception {
        final Metadata metadata = mock(Metadata.class);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);

        assertThat(System.getProperty(ACCESS_CONTROL_DISABLED_PROPERTY), is(nullValue()));

        final Optional<AccessControlViolation> accessControlViolation =
                of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor("command", jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl("command", jsonEnvelope),
                is(sameInstance(accessControlViolation)));

        assertLogStatement();
    }

    @Test
    public void shouldIgnoreAccessControlIfTheAccessControlDisabledPropertyIsTrue() throws Exception {

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "true");

        final Optional<AccessControlViolation> accessControlViolation =
                accessControlService.checkAccessControl("command", jsonEnvelope);

        assertThat(accessControlViolation.isPresent(), is(false));

        verifyNoInteractions(policyEvaluator);

        verify(logger).trace("Skipping access control due to configuration");
    }

    @Test
    public void shouldUseAccessControlIfTheAccessControlDisabledPropertyIsFalse() throws Exception {
        final Metadata metadata = mock(Metadata.class);
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);

        System.setProperty(ACCESS_CONTROL_DISABLED_PROPERTY, "false");

        final Optional<AccessControlViolation> accessControlViolation =
                of(mock(AccessControlViolation.class));

        when(policyEvaluator.checkAccessPolicyFor("command", jsonEnvelope)).thenReturn(accessControlViolation);

        assertThat(accessControlService.checkAccessControl("command", jsonEnvelope),
                is(sameInstance(accessControlViolation)));

        assertLogStatement();
    }

    private void assertLogStatement() {
        verify(logger).trace("Performing access control for action: {}", ACTION_NAME);
    }
}
