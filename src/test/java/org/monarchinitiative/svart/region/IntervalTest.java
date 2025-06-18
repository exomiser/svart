package org.monarchinitiative.svart.region;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Interval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class IntervalTest {

    record BasicInterval(Coordinates coordinates) implements Interval {

        static BasicInterval of(CoordinateSystem coordinateSystem, int start, int end) {
            return new BasicInterval(Coordinates.of(coordinateSystem, start, end));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 3, 3,    ZERO_BASED, 1, 2,  false",
            "ONE_BASED, 3, 3,    ONE_BASED, 2, 2,  false",

            "ONE_BASED, 3, 3,    ZERO_BASED, 2, 3,  true",
            "ONE_BASED, 3, 3,    ONE_BASED, 3, 3,  true",

            "ONE_BASED, 3, 3,    ZERO_BASED, 3, 4,  false",
            "ONE_BASED, 3, 3,    ONE_BASED, 4, 4,  false",

            "ONE_BASED, 3, 4,    ONE_BASED, 2, 3,  false",
            "ONE_BASED, 3, 4,    ONE_BASED, 3, 4,  true",
            "ONE_BASED, 3, 4,    ONE_BASED, 4, 5,  false",
    })
    void contains_region(CoordinateSystem coordinateSystem, int start, int end,
                                CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                                boolean expected) {
        Interval region = BasicInterval.of(coordinateSystem, start, end);
        Interval query = BasicInterval.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.contains(query), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED,  2, 3,   2, false",
            "ZERO_BASED,  2, 3,   3, true",
            "ZERO_BASED,  2, 3,   4, false",

            "ONE_BASED, 3, 3,   2, false",
            "ONE_BASED, 3, 3,   3, true",
            "ONE_BASED, 3, 3,   4, false",
    })
    void contains_position(CoordinateSystem coordinateSystem, int start, int end, int pos, boolean expected) {
        Interval region = BasicInterval.of(coordinateSystem, start, end);
        assertThat(region.contains(pos), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 3, 4,   ONE_BASED, 1, 2,  false",
            "ONE_BASED, 3, 4,   ONE_BASED, 2, 3,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 3, 4,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 4, 5,  true",
            "ONE_BASED, 3, 4,   ONE_BASED, 5, 5,  false",
    })
    void overlapsWith(CoordinateSystem coordinateSystem, int start, int end,
                             CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                             boolean expected) {
        Interval region = BasicInterval.of(coordinateSystem, start, end);
        Interval query = BasicInterval.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.overlapsWith(query), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 4, 5,   ONE_BASED, 1, 2,  -1",
            "ONE_BASED, 4, 5,   ONE_BASED, 2, 3,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 3, 4,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 4, 5,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 5, 6,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 6, 7,  0",
            "ONE_BASED, 4, 5,   ONE_BASED, 7, 8,  1",
    })
    void distanceTo(CoordinateSystem coordinateSystem, int start, int end,
                           CoordinateSystem queryCoordinateSystem, int queryStart, int queryEnd,
                           int expected) {
        Interval region = BasicInterval.of(coordinateSystem, start, end);
        Interval query = BasicInterval.of(queryCoordinateSystem, queryStart, queryEnd);

        assertThat(region.distanceTo(query), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 2, 2,   0",
            "ONE_BASED,  2, 1,   0",

            "ZERO_BASED, 1, 2,   1",
            "ONE_BASED,  2, 2,   1",

            "ONE_BASED,  2, 3,   2",
            "ONE_BASED,  2, 4,   3",
    })
    void length(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        assertThat(BasicInterval.of(coordinateSystem,start, end).length(), equalTo(expected));
    }

    @Test
    void testStartTypes() {
        Interval oneBasedCoords = BasicInterval.of(CoordinateSystem.ONE_BASED, 1, 10);
        Interval zeroBasedCoords = BasicInterval.of(CoordinateSystem.ZERO_BASED,0, 10);
        assertThat(oneBasedCoords.start(), not(equalTo(zeroBasedCoords.start())));
        assertThat(oneBasedCoords.startZeroBased(), equalTo(zeroBasedCoords.start()));
        assertThat(oneBasedCoords.start(), equalTo(zeroBasedCoords.startOneBased()));
    }

    @Test
    void testContainsPosition() {
        Interval oneBasedCoords = BasicInterval.of(CoordinateSystem.ONE_BASED, 1, 10);
        assertThat(oneBasedCoords.contains(0), equalTo(false));
        assertThat(oneBasedCoords.contains(1), equalTo(true));
        assertThat(oneBasedCoords.contains(10), equalTo(true));
        assertThat(oneBasedCoords.contains(11), equalTo(false));

        Interval zeroBasedCoords = BasicInterval.of(CoordinateSystem.ZERO_BASED,0, 10);
        assertThat(zeroBasedCoords.contains(0), equalTo(false));
        assertThat(zeroBasedCoords.contains(1), equalTo(true));
        assertThat(zeroBasedCoords.contains(10), equalTo(true));
        assertThat(zeroBasedCoords.contains(11), equalTo(false));
    }

}
