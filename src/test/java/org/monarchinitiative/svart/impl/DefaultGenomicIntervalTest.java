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
    void preciseAndImpreciseStrings() {
        GenomicInterval precise = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12344));
        assertThat(precise.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345, end=12344}"));

        GenomicInterval impreciseStart = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, ConfidenceInterval.of(-50, 100), 12344, ConfidenceInterval.precise()));
        assertThat(impreciseStart.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345 (-50, +100), end=12344}"));

        GenomicInterval impreciseEnd = GenomicInterval.of(contig1, Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, ConfidenceInterval.precise(), 12344, ConfidenceInterval.of(-50, 100)));
        assertThat(impreciseEnd.toString(), equalTo("GenomicInterval{contig=1, strand=+, coordinateSystem=ONE_BASED, start=12345, end=12344 (-50, +100)}"));
    }
}