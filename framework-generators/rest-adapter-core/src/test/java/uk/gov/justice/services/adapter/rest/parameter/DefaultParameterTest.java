package uk.gov.justice.services.adapter.rest.parameter;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class DefaultParameterTest {

    @Test
    public void shouldCreateStringParameter() {
        Parameter param = DefaultParameter.valueOf("paramName", "someStringValue", ParameterType.STRING);
        assertThat(param.getType(), is(ParameterType.STRING));
        assertThat(param.getName(), is("paramName"));
        assertThat(param.getStringValue(), is("someStringValue"));

    }

    @Test
    public void shouldCreateNumericParameterFromInt() {
        Parameter param = DefaultParameter.valueOf("paramName2", "123", ParameterType.NUMERIC);
        assertThat(param.getType(), is(ParameterType.NUMERIC));
        assertThat(param.getName(), is("paramName2"));
        assertThat(param.getNumericValue(), is(BigDecimal.valueOf(123)));

    }

    @Test
    public void shouldCreateNumericParameterFromFloat() {
        Parameter param = DefaultParameter.valueOf("paramName3", "123.02", ParameterType.NUMERIC);
        assertThat(param.getType(), is(ParameterType.NUMERIC));
        assertThat(param.getName(), is("paramName3"));
        assertThat(param.getNumericValue(), is(BigDecimal.valueOf(123.02)));
    }


    @Test
    public void shouldCreateBooleanParameter() {
        Parameter param = DefaultParameter.valueOf("paramName4", "true", ParameterType.BOOLEAN);
        assertThat(param.getType(), is(ParameterType.BOOLEAN));
        assertThat(param.getName(), is("paramName4"));
        assertThat(param.getBooleanValue(), is(true));

    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfInvalidNumericParamValuePassed() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> DefaultParameter.valueOf("paramName3", "aaa", ParameterType.NUMERIC));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfInvalidBooleanParamValuePassed() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> DefaultParameter.valueOf("paramName3", "aaa", ParameterType.BOOLEAN));
    }

}