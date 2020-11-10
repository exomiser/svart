package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PositionTest {

    private final Position precise = Position.of(101);
    private final Position imprecise = Position.of(101, ConfidenceInterval.of(-20, 10));

    @Test
    void properties() {
        assertThat(precise.getPos(), is(101));
        assertThat(precise.getConfidenceInterval(), is(ConfidenceInterval.precise()));
        assertThat(precise.getCoordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        assertThat(imprecise.getPos(), is(101));
        assertThat(imprecise.getConfidenceInterval(), is(ConfidenceInterval.of(-20, 10)));
    }

    @Test
    void zeroBasedPosition() {
        assertThat(Position.of(2, CoordinateSystem.ZERO_BASED).getCoordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @Test
    void comparePositionsWithDifferentCIs() {
        // precise is higher/better than imprecise
        assertThat(precise.compareTo(imprecise), is(1));
    }

    @ParameterizedTest
    @CsvSource({
            "1,2,-1",
            "2,2,0",
            "3,2,1",
    })
    void comparePrecisePositions(int leftPos, int rightPos, int expected) {
        final Position left = Position.of(leftPos);
        final Position right = Position.of(rightPos);

        assertThat(left.compareTo(right), is(expected));
    }

    @Test
    void isPrecise() {
        assertThat(precise.isPrecise(), is(true));
        assertThat(imprecise.isPrecise(), is(false));
    }

    @Test
    void errorThrownWhenInvalidInput() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> Position.of(0, CoordinateSystem.ONE_BASED));
        assertThat(ex1.getMessage(), is("ONE_BASED position `0` cannot be non-positive"));

        IllegalArgumentException ex0 = assertThrows(IllegalArgumentException.class, () -> Position.of(-1, CoordinateSystem.ZERO_BASED));
        assertThat(ex0.getMessage(), is("ZERO_BASED position `-1` cannot be negative"));
    }

    @Test
    void toZeroBasedFromOneBased() {
        Position oneBased = Position.of(1, CoordinateSystem.ONE_BASED);
        assertThat(oneBased.toCoordinateSystem(CoordinateSystem.ZERO_BASED), equalTo(Position.of(0, CoordinateSystem.ZERO_BASED)));
    }

    @Test
    void toOneBasedFromZeroBased() {
        Position zeroBased = Position.of(0, CoordinateSystem.ZERO_BASED);
        assertThat(zeroBased.toCoordinateSystem(CoordinateSystem.ONE_BASED), equalTo(Position.of(1, CoordinateSystem.ONE_BASED)));
    }
}