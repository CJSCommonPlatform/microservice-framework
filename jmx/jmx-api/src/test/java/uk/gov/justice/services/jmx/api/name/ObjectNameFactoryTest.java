package uk.gov.justice.services.jmx.api.name;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ObjectNameFactoryTest {

    @InjectMocks
    private ObjectNameFactory objectNameFactory;

    @Test
    public void shouldCreateAnObjectName() throws Exception {

        final String domain = "domain";
        final String key = "key";
        final String value = "value";

        final ObjectName objectName = objectNameFactory.create(domain, key, value);

        assertThat(objectName.getDomain(), is(domain));

        assertThat(objectName.getCanonicalName(), is("domain:key=value"));
    }

    @Test
    public void shouldThrowExceptionIfCreatingObjectNameThrowsMalformedObjectNameException() throws Exception {

        try {
            objectNameFactory.create("fred:fred", "type", "value");
            fail();
        } catch (final ObjectNameException expected) {
            assertThat(expected.getMessage(), is("Unable to create ObjectName: domain='fred:fred', key='type', value='value'"));
            assertThat(expected.getCause(), is(CoreMatchers.instanceOf(MalformedObjectNameException.class)));
        }
    }
}
