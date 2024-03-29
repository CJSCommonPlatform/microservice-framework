package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestJdbcDataSourceProviderIT {

    @InjectMocks
    private TestJdbcDataSourceProvider testJdbcDataSourceProvider;

    @Test
    public void shouldGetADataSourceToTheViewStore() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetADataSourceToTheEventStore() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getEventStoreDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetADataSourceToSystem() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getSystemDataSource("framework");

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }

    @Test
    public void shouldGetDataSourceToFileService() throws Exception {

        final DataSource dataSource = testJdbcDataSourceProvider.getFileServiceDataSource();

        try(final Connection connection = dataSource.getConnection()) {
            assertThat(connection.isClosed(), is(false));
        }
    }
}
