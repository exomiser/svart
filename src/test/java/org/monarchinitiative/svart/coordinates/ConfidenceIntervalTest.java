package org.monarchinitiative.svart.coordinates;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.ConfidenceInterval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ConfidenceIntervalTest {

    @Test
    void checkIllegalLowerBoundInput() {
        assertThrows(IllegalArgumentException.class, () -> ConfidenceInterval.of(10, 0));
    }

    @Test
    void checkIllegalUpperBoundInput() {
        assertThrows(IllegalArgumentException.class, () -> ConfidenceInterval.of(0, -10));
    }

    @Test
    void empty() {
        assertThat(ConfidenceInterval.precise(), equalTo(ConfidenceInterval.of(0, 0)));
    }

    @Test
    void getLowerBound() {
        int lowerBound = -10;
        ConfidenceInterval instance = ConfidenceInterval.of(lowerBound, 0);
        assertThat(instance.lowerBound(), equalTo(lowerBound));
    }

    @Test
    void getUpperBound() {
        int upperBound = 20;
        ConfidenceInterval instance = ConfidenceInterval.of(0, upperBound);
        assertThat(instance.upperBound(), equalTo(upperBound));
    }

    @Test
    void getMin() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.minPos(200), equalTo(190));
    }

    @Test
    void getMinFromPrecise() {
        ConfidenceInterval instance = ConfidenceInterval.precise();
        assertThat(instance.minPos(100), equalTo(100));
    }

    @Test
    void getMax() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.maxPos(200), equalTo(220));
    }

    @Test
    void getMaxFromPrecise() {
        ConfidenceInterval instance = ConfidenceInterval.precise();
        assertThat(instance.maxPos(100), equalTo(100));
    }

    @Test
    void isPrecise() {
        assertThat(ConfidenceInterval.precise().isPrecise(), equalTo(true));
        assertThat(ConfidenceInterval.of(0, 0).isPrecise(), equalTo(true));
        assertThat(ConfidenceInterval.of(-1, 0).isPrecise(), equalTo(false));
        assertThat(ConfidenceInterval.of(-1, 2).isPrecise(), equalTo(false));
        assertThat(ConfidenceInterval.of(0, 2).isPrecise(), equalTo(false));
    }

    @Test
    void toOppositeStrand() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.invert(), equalTo(ConfidenceInterval.of(-20, 10)));
        assertThat(ConfidenceInterval.precise().invert(), equalTo(ConfidenceInterval.precise()));
    }

    @Test
    void lengthOfPreciseIsZero() {
        assertThat(ConfidenceInterval.precise().length(), equalTo(0));
    }

    @Test
    void comparator() {
        ConfidenceInterval zero = ConfidenceInterval.precise();
        ConfidenceInterval one = ConfidenceInterval.of(0,1);
        ConfidenceInterval ten = ConfidenceInterval.of(-5,5);

        List<ConfidenceInterval> intervals = new ArrayList<>();
        intervals.add(one);
        intervals.add(ten);
        intervals.add(zero);
        Collections.sort(intervals);
        assertThat(intervals, equalTo(List.of(zero, one, ten)));
    }

    @Test
    void toStringTest() {
        ConfidenceInterval instance = ConfidenceInterval.of(-10, 20);
        assertThat(instance.toString(), equalTo("(-10, +20)"));

        ConfidenceInterval precise = ConfidenceInterval.precise();
        assertThat(precise.toString(), equalTo(""));
    }
}