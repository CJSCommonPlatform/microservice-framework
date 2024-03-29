package uk.gov.justice.services.metrics.interceptor;


import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;

public class MetricRegistryProducerTest {

    @Test
    public void shouldProduceRegistry() {
        final MetricRegistryProducer producer = new MetricRegistryProducer();
        final MetricRegistry actual = producer.metricRegistry();
        assertThat(actual, not(nullValue()));
        assertThat(actual, is(sameInstance(producer.metricRegistry())));
    }
}