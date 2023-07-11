package uk.gov.justice.services.core.audit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DefaultAuditServiceTest {

    private static final String ACTION_NAME = "test.action";
    private static final String COMPONENT = "test-component";

    @Mock
    private AuditClient auditClient;

    @Mock
    private Logger logger;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Metadata metadata;

    @InjectMocks
    private DefaultAuditService auditService;

    @BeforeEach
    public void setup() {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);
    }

    @Test
    public void shouldAuditWithDefaultEmptyBlacklist() throws Exception {
        initialisePattern("");
        auditService.audit(jsonEnvelope, COMPONENT);

        verify(auditClient, times(1)).auditEntry(jsonEnvelope, COMPONENT);
    }

    @Test
    public void shouldAuditNonBlacklistedAction() throws Exception {
        initialisePattern(".*\\.action");
        when(metadata.name()).thenReturn("some-action");

        auditService.audit(jsonEnvelope, COMPONENT);

        verify(auditClient, times(1)).auditEntry(jsonEnvelope, COMPONENT);
    }

    @Test
    public void shouldNotAuditBlacklistedAction() {
        initialisePattern(".*\\.action");

        auditService.audit(jsonEnvelope, COMPONENT);

        verify(logger, times(1)).info("Skipping auditing of action test.action due to configured blacklist pattern .*\\.action.");
        verify(auditClient, never()).auditEntry(jsonEnvelope, COMPONENT);
    }

    private void initialisePattern(final String pattern) {
        auditService.auditBlacklist = pattern;
        auditService.initialise();
    }
}
