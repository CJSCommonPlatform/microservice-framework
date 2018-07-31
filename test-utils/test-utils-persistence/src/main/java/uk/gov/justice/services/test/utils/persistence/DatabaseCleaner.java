package uk.gov.justice.services.test.utils.persistence;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

/**
 * Test utility class for easy cleaning of a context's database.
 * Can clean both buffer tables and the event log table.
 * Plus clean a list of other tables
 *
 * To use:
 *
 * <pre>
 *  {@code
 *     {@literal @}Before
 *     public void cleanTheDatabase() {
 *
 *          databaseCleaner.cleanSubscriptionTable(CONTEXT_NAME);
 *          databaseCleaner.cleanStreamBufferTable(CONTEXT_NAME);
 *          databaseCleaner.cleanEventLogTable(CONTEXT_NAME);
 *          databaseCleaner.cleanViewStoreTables(CONTEXT_NAME, "table_1", "table_2");
 *     }
 *  }
 * </pre>
 */
public class DatabaseCleaner {

    private static final String SQL_PATTERN = "DELETE FROM %s";

    private final TestJdbcConnectionProvider testJdbcConnectionProvider;

    public DatabaseCleaner() {
        this(new TestJdbcConnectionProvider());
    }

    @VisibleForTesting
    DatabaseCleaner(final TestJdbcConnectionProvider testJdbcConnectionProvider) {
        this.testJdbcConnectionProvider = testJdbcConnectionProvider;
    }

    /**
     * Deletes all the data in the 'event_buffer' table
     *
     * @param contextName the name of the context who's tables you are cleaning
     */
    public void cleanStreamBufferTable(final String contextName) {
        cleanViewStoreTables(contextName, "event_buffer");
    }

    /**
     * Deletes all the data in the 'stream_status' table
     *
     * @param contextName the name of the context who's tables you are cleaning
     */
    public void cleanSubscriptionTable(final String contextName) {
        cleanViewStoreTables(contextName, "subscription");
    }


    /**
     * Deletes all the data in the Event-Store tables
     *
     * @param contextName the name of the context to clean the tables from
     */
    public void cleanEventStoreTables(final String contextName) {
        try (final Connection connection = testJdbcConnectionProvider.getEventStoreConnection(contextName)) {

            cleanTable("event_log", connection);
            cleanTable("event_stream", connection);
            cleanTable("publish_queue", connection);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit or close database connection", e);
        }
    }

    /**
     * Deprecated from 3.2.0, please use {@link #cleanEventStoreTables(String)} to clean all tables
     * belonging to the event-store.
     *
     * Deletes all the data in the 'event_log' table
     *
     * @param contextName the name of the context who's tables you are cleaning
     */
    @Deprecated
    public void cleanEventLogTable(final String contextName) {
        cleanEventStoreTables(contextName);
    }

    /**
     * Cleans all the tables in the specified list
     *
     * @param contextName          the name of the context who's tables you are cleaning
     * @param tableName            the name of the first table to be cleaned (ensures that there is
     *                             at least one table to be cleaned)
     * @param additionalTableNames the names of any other tables to be cleaned
     */
    public void cleanViewStoreTables(final String contextName, final String tableName, final String... additionalTableNames) {

        final List<String> names = new ArrayList<>();

        names.add(tableName);
        names.addAll(asList(additionalTableNames));

        //noinspection deprecation
        cleanViewStoreTables(contextName, names);
    }

    /**
     * Cleans all the tables in the specified list
     *
     * @param contextName the name of the context who's tables you are cleaning
     * @param tableNames  a list of names of tables to be cleaned
     * @deprecated use {@link #cleanViewStoreTables(String, String, String...)} instead. It's
     * better.
     */
    @Deprecated
    public void cleanViewStoreTables(final String contextName, final List<String> tableNames) {

        try (final Connection connection = testJdbcConnectionProvider.getViewStoreConnection(contextName)) {
            for (String tableName : tableNames) {
                cleanTable(tableName, connection);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit or close database connection", e);
        }
    }

    private void cleanTable(final String tableName, final Connection connection) {

        final String sql = format(SQL_PATTERN, tableName);
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete content from table " + tableName, e);
        }
    }
}
