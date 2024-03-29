package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ViewStoreJdbcDataSourceProviderTest {

    @Mock
    private InitialContext initialContext;

    @Mock
    private ViewStoreDataSourceNameProvider viewStoreDataSourceNameProvider;

    @InjectMocks
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Test
    public void shouldLookUpTheDataSourceUsingTheCorrectJndiName() throws Exception {

        final String dataSourceName = "java:/DS.framework";
        final DataSource dataSource = mock(DataSource.class);

        when(viewStoreDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenReturn(dataSource);

        assertThat(viewStoreJdbcDataSourceProvider.getDataSource(), is(dataSource));
    }

    @Test
    public void shouldThrowExceptionIfTheLookupOfTheDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException();
        final String dataSourceName = "java:/DS.framework";

        when(viewStoreDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenThrow(namingException);

        try {
            viewStoreJdbcDataSourceProvider.getDataSource();
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup ViewStore DataSource using JNDI name 'java:/DS.framework'"));
        }
    }

    @Test
    public void shouldOnlyLookupTheDataSourceOnce() throws Exception {

        final String dataSourceName = "java:/DS.framework";
        final DataSource dataSource = mock(DataSource.class);

        when(viewStoreDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenReturn(dataSource);

       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();
       viewStoreJdbcDataSourceProvider.getDataSource();

       verify(viewStoreDataSourceNameProvider, times(1)).getDataSourceName();
    }
}
