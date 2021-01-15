package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UseCaseTests {

    @Test
    public void createDonorAcceptorRegionFromExon() {
        TestContig contig = TestContig.of(1, 100);
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, 50, 70);

        // DONOR
        Position donorStart = exon.endPosition().shift(-3);
        Position donorEnd = exon.endPosition().shift(+6);
        GenomicRegion donor = GenomicRegion.of(exon.contig(), exon.strand(), exon.coordinateSystem(), donorStart, donorEnd);
        assertThat(donor.overlapsWith(exon), equalTo(true));
        assertThat(donor.length(), equalTo(9));

        // ACCEPTOR
        Position acceptorStart = exon.startPosition().shift(-8);
        Position acceptorEnd = exon.startPosition().shift(+3); // (region of length 11)
        GenomicRegion acceptor = GenomicRegion.of(exon.contig(), exon.strand(), exon.coordinateSystem(), acceptorStart, acceptorEnd);
        assertThat(acceptor.overlapsWith(exon), equalTo(true));
        assertThat(acceptor.length(), equalTo(11));
    }

    @Test
    public void insertionLiesinExon() {
        TestContig contig = TestContig.of(1, 100);
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, 50, 70);

        Variant insertion = Variant.nonSymbolic(contig, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(60), "A", "AC");
        assertThat(insertion.overlapsWith(exon), equalTo(true));
    }
}
