package org.monarchinitiative.svart.coordinates;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.CoordinateSystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class CoordinateSystemTest {


    @ParameterizedTest
    @CsvSource({"ONE_BASED, true", "ZERO_BASED, false"})
    void isOneBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isOneBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({"ONE_BASED, false", "ZERO_BASED, true"})
    void isZeroBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isZeroBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,     ONE_BASED,   0",
            "ONE_BASED,    ZERO_BASED,  -1",
            "ZERO_BASED,   ZERO_BASED,   0",
            "ZERO_BASED,    ONE_BASED,   1"
    })
    void startDelta(CoordinateSystem current, CoordinateSystem target, int expected) {
        assertThat(current.startDelta(target), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,    ONE_BASED,   0",
            "ONE_BASED,   ZERO_BASED,   0",
            "ZERO_BASED,  ZERO_BASED,   0",
            "ZERO_BASED,   ONE_BASED,   0",
    })
    void endDelta(CoordinateSystem current, CoordinateSystem target, int expected) {
        assertThat(0, equalTo(expected));
    }
}