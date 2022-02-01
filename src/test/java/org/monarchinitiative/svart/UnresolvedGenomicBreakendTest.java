package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UnresolvedGenomicBreakendTest {

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED",
            "ZERO_BASED",
    })
    public void instance(CoordinateSystem coordinateSystem) {
        assertThat(UnresolvedGenomicBreakend.instance(coordinateSystem).coordinateSystem(), equalTo(coordinateSystem));
    }
}