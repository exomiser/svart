package org.monarchinitiative.variant.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CoordinateSystemTest {


    @ParameterizedTest
    @CsvSource({"ONE_BASED, true", "ZERO_BASED, false"})
    public void isOneBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isOneBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({"ONE_BASED, false", "ZERO_BASED, true"})
    public void isZeroBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isZeroBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({
            " ONE_BASED,  CLOSED,   0",
            " ONE_BASED,    OPEN,  -1",
            "ZERO_BASED,    OPEN,   0",
            "ZERO_BASED,  CLOSED,   1"
    })
    public void startDelta(CoordinateSystem current, Endpoint desired, int expected) {
        assertThat(current.startDelta(desired), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            " ONE_BASED,  CLOSED,   0",
            " ONE_BASED,    OPEN,   1",
            "ZERO_BASED,    OPEN,   1",
            "ZERO_BASED,  CLOSED,   0"
    })
    public void endDelta(CoordinateSystem current, Endpoint desired, int expected) {
        assertThat(current.endDelta(desired), equalTo(expected));
    }
}