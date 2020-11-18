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

    @Test
    void properties() {
        assertThat(Position.of(101).pos(), is(101));
        assertThat(Position.of(101).confidenceInterval(), is(ConfidenceInterval.precise()));
        assertThat(Position.of(101).coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));

        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).pos(), is(101));
        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).confidenceInterval(), is(ConfidenceInterval.of(-20, 10)));
    }

    @Test
    void zeroBasedPosition() {
        Position instance = Position.of(2, CoordinateSystem.ZERO_BASED);
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(instance.isOneBased(), is(false));
        assertThat(instance.isZeroBased(), is (true));
    }

    @Test
    void zeroBasedWithPos() {
        Position one = Position.of(1);
        Position two = Position.of(2);
        assertThat(one.withPos(2), equalTo(two));
    }

    @Test
    void oneBasedWithPos() {
        Position one = Position.of(1, CoordinateSystem.ONE_BASED);
        Position two = Position.of(2, CoordinateSystem.ONE_BASED);
        assertThat(one.withPos(2), equalTo(two));
    }

    @Test
    void oneBasedPosition() {
        Position instance = Position.of(2, CoordinateSystem.ONE_BASED);
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.isZeroBased(), is(false));
        assertThat(instance.isOneBased(), is (true));
    }

    @Test
    void isPrecise() {
        assertThat(Position.of(1).isPrecise(), is(true));
        assertThat(Position.of(1, ConfidenceInterval.of(-20, 10)).isPrecise(), is(false));
    }

    @Test
    void errorThrownWhenInvalidInput() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> Position.of(0, CoordinateSystem.ONE_BASED));
        assertThat(ex1.getMessage(), is("Cannot create one-based position `0` with value less than 1"));

        IllegalArgumentException ex0 = assertThrows(IllegalArgumentException.class, () -> Position.of(-1, CoordinateSystem.ZERO_BASED));
        assertThat(ex0.getMessage(), is("Cannot create zero-based position `-1` with negative value"));
    }

    @Test
    void toZeroBasedFromOneBased() {
        Position oneBased = Position.of(1, CoordinateSystem.ONE_BASED);
        Position zeroBased = Position.of(0, CoordinateSystem.ZERO_BASED);
        assertThat(oneBased.toCoordinateSystem(CoordinateSystem.ZERO_BASED), equalTo(zeroBased));
        assertThat(oneBased.zeroBased(), equalTo(zeroBased));
    }

    @Test
    void toOneBasedFromZeroBased() {
        Position zeroBased = Position.of(0, CoordinateSystem.ZERO_BASED);
        Position oneBased = Position.of(1, CoordinateSystem.ONE_BASED);
        assertThat(zeroBased.toCoordinateSystem(CoordinateSystem.ONE_BASED), equalTo(oneBased));
        assertThat(zeroBased.oneBased(), equalTo(oneBased));
    }

    @Test
    void zeroAndOneBasedComparator() {
        Position zero = Position.of(0, CoordinateSystem.ZERO_BASED);
        Position oneBasedOne = Position.of(1, CoordinateSystem.ONE_BASED);
        Position zeroBasedOne = Position.of(1, CoordinateSystem.ZERO_BASED);

        assertThat(zero.compareTo(zeroBasedOne), equalTo(-1));
        assertThat(oneBasedOne.compareTo(zeroBasedOne), equalTo(-1));
        assertThat(zero.compareTo(oneBasedOne), equalTo(0));
        assertThat(zeroBasedOne.compareTo(oneBasedOne), equalTo(1));
    }

    @Test
    void compare() {
        Position zero = Position.of(0, CoordinateSystem.ZERO_BASED);
        Position oneBasedOne = Position.of(1, CoordinateSystem.ONE_BASED);
        Position zeroBasedOne = Position.of(1, CoordinateSystem.ZERO_BASED);

        assertThat(Position.compare(zero, zeroBasedOne), equalTo(-1));
        assertThat(Position.compare(oneBasedOne, zeroBasedOne), equalTo(-1));
        assertThat(Position.compare(zero, oneBasedOne), equalTo(0));
        assertThat(Position.compare(zeroBasedOne, oneBasedOne), equalTo(1));
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
            "1,2,-1",
            "2,2,0",
            "3,2,1",
    })
    void comparePrecisePositions(int leftPos, int rightPos, int expected) {
        final Position left = Position.of(leftPos);
        final Position right = Position.of(rightPos);

        assertThat(left.compareTo(right), is(expected));
    }

}