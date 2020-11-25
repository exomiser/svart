package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

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
            "ONE_BASED, ONE_BASED, 0",
            "ONE_BASED, ZERO_BASED, -1",
            "ZERO_BASED, ZERO_BASED, 0",
            "ZERO_BASED, ONE_BASED, 1"
    })
    public void delta(CoordinateSystem current, CoordinateSystem desired, int expected) {
        assertThat(current.delta(desired), equalTo(expected));
    }

}