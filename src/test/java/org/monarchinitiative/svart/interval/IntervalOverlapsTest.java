package org.monarchinitiative.svart.interval;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class IntervalOverlapsTest {

    private final Contig chr1 = GenomicAssemblies.GRCh38p13().contigById(1);
    private final GenomicRegion region1 = GenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.zeroBased(200, 400));
    private final GenomicRegion region2 = GenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.zeroBased(800, 1600));

    @Test
    void overlapping() {
        List<GenomicRegion> overlaps = List.of(this.region1, region2);
        IntervalOverlaps<GenomicRegion> instance = IntervalOverlaps.of(overlaps);
        assertThat(instance.hasOverlaps(), equalTo(true));
        assertThat(instance.overlaps(), equalTo(overlaps));

        assertThat(instance.hasLeft(), equalTo(false));
        assertThat(instance.hasRight(), equalTo(false));
    }

    @Test
    void neighbours() {
        IntervalOverlaps<GenomicRegion> instance = IntervalOverlaps.neighbours(region1, region2);
        assertThat(instance.hasOverlaps(), equalTo(false));
        assertThat(instance.overlaps(), equalTo(List.of()));

        assertThat(instance.hasLeft(), equalTo(true));
        assertThat(instance.left(), equalTo(region1));

        assertThat(instance.hasRight(), equalTo(true));
        assertThat(instance.right(), equalTo(region2));
    }

    @Test
    void neighboursLeftOnly() {
        IntervalOverlaps<GenomicRegion> instance = IntervalOverlaps.neighbours(region1, null);
        assertThat(instance.hasOverlaps(), equalTo(false));
        assertThat(instance.overlaps(), equalTo(List.of()));

        assertThat(instance.hasLeft(), equalTo(true));
        assertThat(instance.left(), equalTo(region1));

        assertThat(instance.hasRight(), equalTo(false));
        assertThat(instance.right(), equalTo(null));
    }

    @Test
    void neighboursRightOnly() {
        IntervalOverlaps<GenomicRegion> instance = IntervalOverlaps.neighbours(null, region2);
        assertThat(instance.hasOverlaps(), equalTo(false));
        assertThat(instance.overlaps(), equalTo(List.of()));

        assertThat(instance.hasLeft(), equalTo(false));
        assertThat(instance.left(), equalTo(null));

        assertThat(instance.hasRight(), equalTo(true));
        assertThat(instance.right(), equalTo(region2));
    }


    @Test
    void empty() {
        IntervalOverlaps<GenomicRegion> instance = IntervalOverlaps.empty();
        assertThat(instance.hasOverlaps(), equalTo(false));
        assertThat(instance.hasLeft(), equalTo(false));
        assertThat(instance.hasRight(), equalTo(false));

        assertThat(IntervalOverlaps.of(List.of()), equalTo(IntervalOverlaps.empty()));
    }
}