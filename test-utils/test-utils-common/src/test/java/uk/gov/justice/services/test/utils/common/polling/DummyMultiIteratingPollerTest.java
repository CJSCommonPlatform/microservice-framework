package uk.gov.justice.services.test.utils.common.polling;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DummyMultiIteratingPollerTest {

    @InjectMocks
    private DummyMultiIteratingPoller dummyMultiIteratingPoller;

    @Test
    public void shouldCallTheSupplierWithoutAnyPollingToAllowForTests() throws Exception {

        final List<String> strings = new ArrayList<>();

        final boolean result = dummyMultiIteratingPoller.pollUntilTrue(() -> {
            strings.add("it works");
            return true;
        });

        assertThat(result, is(true));
        assertThat(strings.size(), is(1));
        assertThat(strings, hasItem("it works"));
    }
}
