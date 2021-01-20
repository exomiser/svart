package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.parsers.GenomicAssemblyParser;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Variant insertion = Variant.of(contig, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(60), "A", "AC");
        assertThat(insertion.overlapsWith(exon), equalTo(true));
    }

    @Test
    public void symbolicVariantContainsSnv() {
        Contig chr1 = TestContig.of(1, 1000);
        Variant largeIns = Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(1), Position.of(1), "T", "<INS>", 100);
        assertTrue(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(1), "A", "T")));
        assertTrue(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(0), "A", "T")));
        assertFalse(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(2), "C", "A")));
        assertTrue(largeIns.contains(Breakend.of(chr1, "bnd_A", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(1))));
    }

    @Test
    public void trimMultiAllelicSite() {
        // Given the VCF record:
        // chr1    225725424       .       CTT     C,CT    258.06  FreqFilter      AC=1,1;AF=0.500,0.500;AN=2;DP=7;ExcessHet=3.0103;FS=0.000;MAX_FREQ=3.1360424;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=29.21;SOR=0.941    GT:AD:DP:GQ:PL  1/2:0,4,3:7:58:275,76,58,106,0,85
        Contig chr1 = TestContig.of(1, 249_250_621);
        VariantTrimmer leftShiftingTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

        Variant firstAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(225_725_424), "CTT", "C");
        assertThat(firstAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(225_725_424), "CTT", "C")));

        // throws an IllegalArgumentException without being trimmed first
        Variant secondAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(225_725_424), "CTT", "CT");
        assertThat(secondAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(225_725_424), "CT", "C")));
    }

    private Variant trim(VariantTrimmer variantTrimmer, Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        VariantPosition firstTrimmed = variantTrimmer.trim(strand, start.pos(), ref, alt);
        return Variant.of(contig, id, strand, coordinateSystem, start.withPos(firstTrimmed.start()), firstTrimmed.ref(), firstTrimmed.alt());
    }

    @Test
    public void checkGeneContainsVariant() {
        // Load the Human GRCh37.13 assembly from a NCBI assembly report
        GenomicAssembly b37 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // FGFR2 gene is located on chromosome 10 (CM000672.1): 123,237,848-123_357_972 reverse strand. (1-based, positive strand coordinates)
        Contig chr10b37 = b37.contigByName("10");
        GenomicRegion fgfr2Gene = GenomicRegion.of(chr10b37, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, 123_237_848, 123_357_972);
        // 10	123256215	.	T	G  - a pathogenic missense variant (GRCh37 VCF coordinates - 1-based, positive strand)
        Variant snv = Variant.of(chr10b37, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(123_256_215), "T", "G");
        // Because svart knows about coordinate systems and strands it is possible to...
        // keep the gene on the positive strand:
        // GenomicRegion{contig=10, strand=+, coordinateSystem=FULLY_CLOSED, startPosition=123237848, endPosition=123357972}
        assertThat(fgfr2Gene.contains(snv), equalTo(true));
        // or use it on the negative strand:
        // GenomicRegion{contig=10, strand=-, coordinateSystem=FULLY_CLOSED, startPosition=12176776, endPosition=12296900}
        assertThat(fgfr2Gene.toNegativeStrand().contains(snv), equalTo(true));
    }

    @Test
    public void emptyRegionsWithDifferentCoordinateSystems() {
        // these are empty regions and can be used to represent a 'slice' in-between two bases
        GenomicRegion oneBasedEmpty = GenomicRegion.oneBased(Contig.unknown(), 1, 0);
        GenomicRegion zeroBasedEmpty = GenomicRegion.zeroBased(Contig.unknown(), 0, 0);

        assertThat(oneBasedEmpty.contains(zeroBasedEmpty), equalTo(true));
        // convert coordinate systems using convenience methods
        assertThat(oneBasedEmpty.toZeroBased(), equalTo(zeroBasedEmpty));
        // convert coordinate systems using specific systems
        assertThat(oneBasedEmpty.withCoordinateSystem(CoordinateSystem.LEFT_OPEN), equalTo(zeroBasedEmpty));
        System.out.println(oneBasedEmpty);
    }
}
