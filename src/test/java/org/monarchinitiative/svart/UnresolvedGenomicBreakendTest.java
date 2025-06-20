package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UnresolvedGenomicBreakendTest {

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 1",
            "ZERO_BASED, 0",
    })
    void constructorNoIdentifier(CoordinateSystem coordinateSystem, int start) {
        UnresolvedGenomicBreakend instance = UnresolvedGenomicBreakend.of(coordinateSystem);
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.start(), equalTo(start));
        assertThat(instance.id(), equalTo(""));
        assertThat(instance.mateId(), equalTo(""));
        assertThat(instance.isUnresolved(), equalTo(true));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, bnd1",
            "ZERO_BASED, bnd0",
    })
    void constructorWithIdentifier(CoordinateSystem coordinateSystem, String id) {
        UnresolvedGenomicBreakend instance = UnresolvedGenomicBreakend.of(coordinateSystem, id);
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.id(), equalTo(id));
        assertThat(instance.isUnresolved(), equalTo(true));
    }
}