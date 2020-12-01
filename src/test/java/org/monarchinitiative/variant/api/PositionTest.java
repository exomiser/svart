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
public class PositionTest {

    @Test
    public void properties() {
        assertThat(Position.of(101).pos(), is(101));
        assertThat(Position.of(101).confidenceInterval(), is(ConfidenceInterval.precise()));

        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).pos(), is(101));
        assertThat(Position.of(101, ConfidenceInterval.of(-20, 10)).confidenceInterval(), is(ConfidenceInterval.of(-20, 10)));
    }

    @Test
    public void withPos() {
        Position one = Position.of(1);
        Position two = Position.of(2);
        assertThat(one.withPos(2), equalTo(two));
    }

    @Test
    public void withPosImprecise() {
        Position aboutOne = Position.of(1, -1, 1);
        Position aboutTwo = Position.of(2, -1, 1);
        assertThat(aboutOne.withPos(2), equalTo(aboutTwo));
    }

    @Test
    public void confidenceInterval() {
        assertThat(Position.of(100).confidenceInterval(), equalTo(ConfidenceInterval.precise()));
        ConfidenceInterval ci = ConfidenceInterval.of(-20, 10);
        assertThat(Position.of(100, ci).confidenceInterval(), equalTo(ci));
    }

    @Test
    public void minPos() {
        assertThat(Position.of(100).minPos(), equalTo(100));
        ConfidenceInterval ci = ConfidenceInterval.of(-20, 10);
        assertThat(Position.of(100, ci).minPos(), equalTo(80));
    }

    @Test
    public void maxPos() {
        assertThat(Position.of(100).maxPos(), equalTo(100));
        ConfidenceInterval ci = ConfidenceInterval.of(-20, 10);
        assertThat(Position.of(100, ci).maxPos(), equalTo(110));
    }

    @Test
    public void isPrecise() {
        assertThat(Position.of(100).isPrecise(), is(true));
        assertThat(Position.of(100, ConfidenceInterval.of(-20, 10)).isPrecise(), is(false));
    }

    @Test
    public void errorThrownWhenInvalidInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Position.of(-1));
        assertThat(exception.getMessage(), is("Cannot create position `-1` with negative value"));
    }

    @Test
    public void toZeroBasedFromOneBased() {
        Position oneBasedStart = Position.of(1);
        Position zeroBasedStart = Position.of(0);
        int delta = CoordinateSystem.ONE_BASED.delta(CoordinateSystem.ZERO_BASED);
        assertThat(delta, equalTo(-1));
        assertThat(oneBasedStart.shift(delta), equalTo(zeroBasedStart));
    }

    @Test
    public void toOneBasedFromZeroBased() {
        Position zeroBased = Position.of(0);
        Position oneBased = Position.of(1);
        assertThat(zeroBased.shift(CoordinateSystem.ZERO_BASED.delta(CoordinateSystem.ONE_BASED)), equalTo(oneBased));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1, -1",
            "1, 0, 1",
            "0, 0, 0",
    })
    public void compare(int x , int y, int expected) {
        assertThat(Position.compare(Position.of(x), Position.of(y)), equalTo(expected));
    }

    @Test
    public void comparePositionsWithDifferentCIs() {
        // precise is higher/better than imprecise
        Position precise = Position.of(100);
        Position imprecise = Position.of(100, ConfidenceInterval.of(-20, 10));
        assertThat(precise.compareTo(imprecise), is(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, -1",
            "2, 2, 0",
            "3, 2, 1",
    })
    public void comparePrecisePositions(int leftPos, int rightPos, int expected) {
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
    public void shiftPosPrecise(int pos, int delta, int expect) {
        assertThat(Position.of(pos).shift(delta), equalTo(Position.of(expect)));
    }

    @Test
    public void shiftPosImprecise() {
        assertThat(Position.of(10, ConfidenceInterval.of(-5, 7)).shift(5), equalTo(Position.of(15, ConfidenceInterval.of(-5, 7))));
    }

    @Test
    public void toPrecise() {
        assertThat(Position.of(1).toPrecise(), equalTo(Position.of(1)));
        assertThat(Position.of(1, ConfidenceInterval.of(-1, 1)).toPrecise(), equalTo(Position.of(1)));
    }

    @Test
    public void string() {
        assertThat(Position.of(1).toString(), equalTo("1"));
        assertThat(Position.of(1, ConfidenceInterval.of(-1, 1)).toString(), equalTo("1 (-1, +1)"));
    }

    @ParameterizedTest
    @CsvSource({
            "1, ONE_BASED, 100",
            "0, ZERO_BASED, 100",
            "100, ZERO_BASED, 0",
            "100, ONE_BASED, 1",
    })
    public void invert(int pos, CoordinateSystem coordinateSystem, int expected) {
        Contig contig = Contig.of(1, "1", SequenceRole.UNKNOWN, 100, "", "", "");
        assertThat(Position.of(pos).invert(contig, coordinateSystem), equalTo(Position.of(expected)));
    }
}