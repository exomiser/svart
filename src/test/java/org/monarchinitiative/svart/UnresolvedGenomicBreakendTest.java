package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UnresolvedGenomicBreakendTest {

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED",
            "ZERO_BASED",
    })
    void instance(CoordinateSystem coordinateSystem) {
        assertThat(UnresolvedGenomicBreakend.instance(coordinateSystem).coordinateSystem(), equalTo(coordinateSystem));
    }
}