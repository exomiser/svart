package org.monarchinitiative.svart.coordinates;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.ConfidenceInterval;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * This class contains unit tests that verify the functionality of the "formatCoordinates" static method
 * in the CoordinatesFormat class which formats a given Coordinates object into a string representation.
 */
class CoordinatesFormatTest{

    /**
     * Test to verify the format of a precise coordinate system.
     */
    @Test
    void testFormatPreciseCoordinates() {
        Coordinates coordinates = PreciseCoordinates.of(CoordinateSystem.oneBased(), 5, 10);
        String result = CoordinatesFormat.formatCoordinates(coordinates);
        assertThat(result, equalTo("coordinateSystem=ONE_BASED, start=5, end=10"));
    }

    /**
     * Test to verify the format of an imprecise coordinate system without any confidence intervals.
     */
    @Test
    void testFormatImpreciseCoordinatesWithoutConfidenceInterval() {
        Coordinates coordinates = ImpreciseCoordinates.of(CoordinateSystem.oneBased(), 5, ConfidenceInterval.precise(), 10, ConfidenceInterval.precise());
        String result = CoordinatesFormat.formatCoordinates(coordinates);
        assertThat(result, equalTo("coordinateSystem=ONE_BASED, start=5, end=10"));
    }

    /**
     * Test to verify the format of an imprecise coordinate system with confidence intervals.
     */
    @Test
    void testFormatImpreciseCoordinatesWithConfidenceInterval() {
        ConfidenceInterval startCI = ConfidenceInterval.of(-2,3);
        ConfidenceInterval endCI = ConfidenceInterval.of(-8,10);
        Coordinates coordinates = ImpreciseCoordinates.of(CoordinateSystem.oneBased(), 1, startCI, 5, endCI);
        String result = CoordinatesFormat.formatCoordinates(coordinates);
        assertThat(result, equalTo("coordinateSystem=ONE_BASED, start=1 (-2, +3), end=5 (-8, +10)"));
    }

    @Test
    void testFormatMixedPrecisionCoordinatesWithPreciseStart() {
        ConfidenceInterval ci = ConfidenceInterval.of(-8,10);
        Coordinates coordinates = ImpreciseCoordinates.of(CoordinateSystem.oneBased(), 1, ConfidenceInterval.precise(), 5, ci);
        String result = CoordinatesFormat.formatCoordinates(coordinates);
        assertThat(result, equalTo("coordinateSystem=ONE_BASED, start=1, end=5 (-8, +10)"));
    }

    @Test
    void testFormatMixedPrecisionCoordinatesWithPreciseEnd() {
        ConfidenceInterval ci = ConfidenceInterval.of(-8,10);
        Coordinates coordinates = ImpreciseCoordinates.of(CoordinateSystem.oneBased(), 1, ci, 5, ConfidenceInterval.precise());
        String result = CoordinatesFormat.formatCoordinates(coordinates);
        assertThat(result, equalTo("coordinateSystem=ONE_BASED, start=1 (-8, +10), end=5"));
    }

}