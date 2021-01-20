package org.monarchinitiative.svart;

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
    @CsvSource({"FULLY_CLOSED, true", "LEFT_OPEN, false"})
    public void isOneBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isOneBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({"FULLY_CLOSED, false", "LEFT_OPEN, true"})
    public void isZeroBased(CoordinateSystem coordinateSystem, boolean expected) {
        assertThat(coordinateSystem.isZeroBased(), is(expected));
    }

    @ParameterizedTest
    @CsvSource({
            " FULLY_CLOSED,  FULLY_CLOSED,   0",
            " FULLY_CLOSED,  LEFT_OPEN,  -1",
            " FULLY_CLOSED,  FULLY_OPEN,  -1",
            "LEFT_OPEN,  LEFT_OPEN,   0",
            "LEFT_OPEN,  FULLY_CLOSED,   1"
    })
    public void startDelta(CoordinateSystem current, CoordinateSystem target, int expected) {
        assertThat(current.startDelta(target), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            " FULLY_CLOSED,  FULLY_CLOSED,   0",
            " FULLY_CLOSED, LEFT_OPEN,   0",
            " FULLY_CLOSED, FULLY_OPEN,   1",
            " FULLY_CLOSED, RIGHT_OPEN,   1",
            "LEFT_OPEN, LEFT_OPEN,   0",
            "LEFT_OPEN,  FULLY_CLOSED,   0",
            "FULLY_OPEN, FULLY_CLOSED,   -1",
    })
    public void endDelta(CoordinateSystem current, CoordinateSystem target, int expected) {
        assertThat(current.endDelta(target), equalTo(expected));
    }
}