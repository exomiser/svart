package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class PositionTest {

    @Test
    void properties() {
        assertThat(Position.of(101).pos(), is(101));
        assertThat(Position.of(101).confidenceInterval(), is(ConfidenceInterval.precise()));

        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).pos(), is(101));
        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).confidenceInterval(), is(ConfidenceInterval.of(-20, 10)));
    }

    @Test
    void withPos() {
        Position one = Position.of(1);
        Position two = Position.of(2);
        assertThat(one.withPos(2), equalTo(two));
    }

    @Test
    void isPrecise() {
        assertThat(Position.of(1).isPrecise(), is(true));
        assertThat(Position.of(1, ConfidenceInterval.of(-20, 10)).isPrecise(), is(false));
    }

    @Test
    void errorThrownWhenInvalidInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Position.of(-1));
        assertThat(exception.getMessage(), is("Cannot create position `-1` with negative value"));
    }

    @Test
    void toZeroBasedFromOneBased() {
        Position oneBasedStart = Position.of(1);
        Position zeroBasedStart = Position.of(0);
        int delta = CoordinateSystem.ONE_BASED.delta(CoordinateSystem.ZERO_BASED);
        assertThat(delta, equalTo(-1));
        assertThat(oneBasedStart.shiftPos(delta), equalTo(zeroBasedStart));
    }

    @Test
    void toOneBasedFromZeroBased() {
        Position zeroBased = Position.of(0);
        Position oneBased = Position.of(1);
        assertThat(zeroBased.shiftPos(CoordinateSystem.ZERO_BASED.delta(CoordinateSystem.ONE_BASED)), equalTo(oneBased));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1, -1",
            "1, 0, 1",
            "0, 0, 0",
    })
    void compare(int x , int y, int expected) {
        assertThat(Position.compare(Position.of(x), Position.of(y)), equalTo(expected));
    }

    @Test
    void comparePositionsWithDifferentCIs() {
        // precise is higher/better than imprecise
        Position precise = Position.of(1);
        Position imprecise = Position.of(1, ConfidenceInterval.of(-20, 10));
        assertThat(precise.compareTo(imprecise), is(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, -1",
            "2, 2, 0",
            "3, 2, 1",
    })
    void comparePrecisePositions(int leftPos, int rightPos, int expected) {
        Position left = Position.of(leftPos);
        Position right = Position.of(rightPos);

        assertThat(left.compareTo(right), is(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2, -1, 1",
            "2, 0, 2",
            "2, 1, 3",
    })
    void shiftPosOneBased(int pos, int delta, int expect) {
        assertThat(Position.of(pos).shiftPos(delta), equalTo(Position.of(expect)));
    }
}