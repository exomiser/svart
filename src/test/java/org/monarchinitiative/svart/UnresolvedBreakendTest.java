package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UnresolvedBreakendTest {

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED",
            "LEFT_OPEN",
    })
    public void instance(CoordinateSystem coordinateSystem) {
        assertThat(UnresolvedBreakend.instance(coordinateSystem).coordinateSystem(), equalTo(coordinateSystem));
    }
}