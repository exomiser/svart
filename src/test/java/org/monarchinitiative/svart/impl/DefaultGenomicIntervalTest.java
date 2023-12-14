package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

class DefaultGenomicIntervalTest {

    private final Contig contig1 = TestContigs.chr1;

    @Test
    void staticConstructor() {
        DefaultGenomicInterval instance = DefaultGenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345));
        GenomicInterval fromInterface = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345));
        assertThat(instance, equalTo(fromInterface));
        assertThat(fromInterface, instanceOf(DefaultGenomicInterval.class));
    }

    @Test
    void allTheStartsAndEnds() {
        GenomicInterval instance = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345));
        assertThat(instance.start(), equalTo(12345));
        assertThat(instance.end(), equalTo(12345));

        assertThat(instance.startStd(), equalTo(instance.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));
        assertThat(instance.endStd(), equalTo(instance.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));

        assertThat(instance.startZeroBased(), equalTo(12344));
        assertThat(instance.endZeroBased(), equalTo(12345));

        assertThat(instance.startZeroBased(Strand.NEGATIVE), equalTo((contig1.length() - instance.end())));
        assertThat(instance.endZeroBased(Strand.NEGATIVE), equalTo((contig1.length() - instance.startZeroBased())));

        assertThat(instance.startOneBased(), equalTo(12345));
        assertThat(instance.endOneBased(), equalTo(12345));

        assertThat(instance.startOneBased(Strand.NEGATIVE), equalTo((contig1.length() + 1) - instance.end()));
        assertThat(instance.endOneBased(Strand.NEGATIVE), equalTo((contig1.length() + 1) - instance.start()));
    }

    @Test
    void emptyInterval() {
        GenomicInterval oneBased = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12344));
        GenomicInterval zeroBased = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ZERO_BASED, 12344, 12344));
        assertThat(oneBased.length(), equalTo(0));
        assertThat(oneBased.length(), equalTo(zeroBased.length()));
        assertThat(oneBased.startZeroBased(), equalTo(zeroBased.start()));
        assertThat(oneBased.endZeroBased(), equalTo(zeroBased.end()));
    }

    @Test
    void preciseAndImpreciseStrings() {
        GenomicInterval precise = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12344));
        assertThat(precise.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345, end=12344}"));

        GenomicInterval impreciseStart = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, ConfidenceInterval.of(-50, 100), 12344, ConfidenceInterval.precise()));
        assertThat(impreciseStart.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345 (-50, +100), end=12344}"));

        GenomicInterval impreciseEnd = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, ConfidenceInterval.precise(), 12344, ConfidenceInterval.of(-50, 100)));
        assertThat(impreciseEnd.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345, end=12344 (-50, +100)}"));
    }
}