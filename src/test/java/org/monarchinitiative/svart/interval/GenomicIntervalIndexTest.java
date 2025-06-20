package org.monarchinitiative.svart.interval;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.GenomicVariant;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GenomicIntervalIndexTest {

    private final Contig chr1 = TestContig.of(1, 500);
    private final Contig chr2 = TestContig.of(2, 200);

    private final GenomicRegion region1 = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 20, 50);
    private final GenomicRegion region2 = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 90, 150);

    // these exons overlap, but are on opposite strands:
    //  exon3 (+):              100 |-------------------------| 200
    //  chr2:      |------------------------------------------|
    //  exon4 (-):     150 |-------------------------| 25
    private final GenomicRegion region3 = GenomicRegion.of(chr2, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 100, 200);
    private final GenomicRegion region4 = GenomicRegion.of(chr2, Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 25, 150);

    private final List<GenomicRegion> exons = List.of(region1, region2, region3, region4);

    private final GenomicIntervalIndex<GenomicRegion> instance = GenomicIntervalIndex.of(exons);

    @Test
    void size() {
        assertThat(instance.size(), equalTo(exons.size()));
    }

    @Test
    void empty() {
        assertThat(GenomicIntervalIndex.empty().size(), equalTo(0));
    }

    @Test
    void empty_noOverlap() {
        assertThat(GenomicIntervalIndex.empty().regionsOverlapping(region1), equalTo(IntervalOverlaps.empty()));
    }

    @Test
    void overlappingRegion_noOverlapLeftNeighbour() {
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 250, "A", "T");
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.neighbours(region2, null)));
    }

    @Test
    void overlappingRegion_noOverlapRightNeighbour() {
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 18, "A", "T");
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.neighbours(null, region1)));
    }

    @Test
    void overlappingRegion_oneOverlap() {
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 20, "A", "T");
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region1))));
    }

    @Test
    void overlappingRegion_twoOverlap() {
        GenomicVariant variant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 22, 100, "A", "<DEL>", -88);
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region1, region2))));
    }

    @Test
    void overlappingRegion_chr2() {
        GenomicVariant variant = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 25, 101, "A", "<DEL>", -76);
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region4, region3))));
    }

    @Test
    void overlappingRegion_emptyRegionChr2() {
        GenomicVariant variant = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 101, 101, "A", "<INS>", 100);
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region4, region3))));
    }

    @Test
    void overlappingRegion_emptyOneBasedRegionChr2() {
        GenomicVariant variant = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 102, 101, "A", "<INS>", 100);
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region4, region3))));
    }

    @Test
    void overlappingRegion_chr2NegativeStrandVariantOverlapSinglePositiveStrand() {
        GenomicVariant variant = GenomicVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 24, "A", "ATG");
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region3))));
    }

    @Test
    void overlappingRegion_chr2NegativeStrandVariantOverlapSingleNegativeStrand() {
        GenomicVariant variant = GenomicVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 125, "A", "ATG");
        assertThat(instance.regionsOverlapping(variant), equalTo(IntervalOverlaps.of(List.of(region4))));
    }
}