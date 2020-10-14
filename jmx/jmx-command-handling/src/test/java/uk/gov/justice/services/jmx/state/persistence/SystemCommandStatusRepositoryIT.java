package uk.gov.justice.services.jmx.state.persistence;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.jmx.command.TestCommand.TEST_COMMAND;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.SystemJdbcDataSourceProvider;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.command.TestCommand;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SystemCommandStatusRepositoryIT {

    private final DataSource systemDataSource = new TestJdbcDataSourceProvider().getSystemDataSource("framework");

    @Mock
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @InjectMocks
    private SystemCommandStatusRepository systemCommandStatusRepository;

    @Before
    public void stubGetDataSource() {
        when(systemJdbcDataSourceProvider.getDataSource()).thenReturn(systemDataSource);
    }

    @Before
    public void cleanTable() throws Exception {

        try (final Connection connection = systemDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement("TRUNCATE system_command_status")) {
            preparedStatement.executeUpdate();
        }
    }

    @Test
    public void shouldSaveAndFindAll() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final UUID catchupCommandId = randomUUID();
        final SystemCommandStatus catchupCommandStatus_1 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(10),
                "Catchup started successfully"
        );
        final SystemCommandStatus indexCommandStatus = new SystemCommandStatus(
                randomUUID(),
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(5),
                "Indexing of events started successfully"
        );
        final SystemCommandStatus catchupCommandStatus_2 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_COMPLETE,
                now,
                "Catchup complete"
        );

        systemCommandStatusRepository.add(catchupCommandStatus_1);
        systemCommandStatusRepository.add(indexCommandStatus);
        systemCommandStatusRepository.add(catchupCommandStatus_2);

        final List<SystemCommandStatus> systemCommandStatuses = systemCommandStatusRepository.findAll();

        assertThat(systemCommandStatuses.size(), is(3));

        assertThat(systemCommandStatuses.get(0), is(catchupCommandStatus_1));
        assertThat(systemCommandStatuses.get(1), is(indexCommandStatus));
        assertThat(systemCommandStatuses.get(2), is(catchupCommandStatus_2));
    }

    @Test
    public void shouldFindAllStatusesOfACommand() throws Exception {
        final ZonedDateTime now = new UtcClock().now();
        final UUID catchupCommandId = randomUUID();
        final SystemCommandStatus catchupCommandStatus_1 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(10),
                "Catchup started successfully"
        );
        final SystemCommandStatus indexCommandStatus = new SystemCommandStatus(
                randomUUID(),
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(5),
                "Indexing of events started successfully"
        );
        final SystemCommandStatus catchupCommandStatus_2 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_COMPLETE,
                now,
                "Catchup complete"
        );

        systemCommandStatusRepository.add(catchupCommandStatus_1);
        systemCommandStatusRepository.add(indexCommandStatus);
        systemCommandStatusRepository.add(catchupCommandStatus_2);

        final List<SystemCommandStatus> systemCommandStatuses = systemCommandStatusRepository.findAllByCommandId(catchupCommandId);

        assertThat(systemCommandStatuses.size(), is(2));

        assertThat(systemCommandStatuses.get(0), is(catchupCommandStatus_1));
        assertThat(systemCommandStatuses.get(1), is(catchupCommandStatus_2));
    }

    @Test
    public void shouldGetTheLatestStatusOfACommandById() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final UUID catchupCommandId = randomUUID();
        final SystemCommandStatus catchupCommandStatus_1 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(10),
                "Catchup started successfully"
        );
        final SystemCommandStatus indexCommandStatus = new SystemCommandStatus(
                randomUUID(),
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(5),
                "Indexing of events started successfully"
        );
        final SystemCommandStatus catchupCommandStatus_2 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_COMPLETE,
                now,
                "Catchup complete"
        );

        systemCommandStatusRepository.add(catchupCommandStatus_1);
        systemCommandStatusRepository.add(indexCommandStatus);
        systemCommandStatusRepository.add(catchupCommandStatus_2);

        final Optional<SystemCommandStatus> latestStatus = systemCommandStatusRepository.findLatestStatusById(catchupCommandId);

        if (latestStatus.isPresent()) {
            assertThat(latestStatus.get(), is(catchupCommandStatus_2));
        } else {
            fail();
        }
    }

    @Test
    public void shouldReturnEmptyIfNoLatestStatusFound() throws Exception {

        assertThat(systemCommandStatusRepository.findLatestStatusById(randomUUID()), is(empty()));
    }

    @Test
    public void shouldGetTheLatestStatusOfACommand() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final UUID catchupCommandId = randomUUID();
        final SystemCommandStatus indexerCommandStatus_1 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now.minusMinutes(2),
                "Catchup started successfully"
        );
        final SystemCommandStatus latestCommandStatus = new SystemCommandStatus(
                randomUUID(),
                TEST_COMMAND,
                COMMAND_IN_PROGRESS,
                now,
                "Indexing of events started successfully"
        );
        final SystemCommandStatus catchupCommandStatus_2 = new SystemCommandStatus(
                catchupCommandId,
                TEST_COMMAND,
                COMMAND_COMPLETE,
                now.minusMinutes(5),
                "Catchup complete"
        );

        systemCommandStatusRepository.add(indexerCommandStatus_1);
        systemCommandStatusRepository.add(latestCommandStatus);
        systemCommandStatusRepository.add(catchupCommandStatus_2);

        final Optional<SystemCommandStatus> latestStatus = systemCommandStatusRepository.findLatestStatusByType(new TestCommand());

        assertThat(latestStatus, is(of(latestCommandStatus)));
    }


    @Test
    public void shouldReturnEmptyIfNoLatestStatusFoundByType() throws Exception {

        assertThat(systemCommandStatusRepository.findLatestStatusByType(new TestCommand()), is(empty()));
    }
}
