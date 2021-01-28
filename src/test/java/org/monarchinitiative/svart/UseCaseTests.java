package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;
import org.monarchinitiative.svart.util.VcfConverter;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.monarchinitiative.svart.CoordinateSystem.*;

public class UseCaseTests {

    @Test
    public void createDonorAcceptorRegionFromExon() {
        TestContig contig = TestContig.of(1, 100);
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, LEFT_OPEN, 50, 70);

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
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, FULLY_CLOSED, 50, 70);

        Variant insertion = Variant.of(contig, "", Strand.POSITIVE, FULLY_CLOSED, Position.of(60), "A", "AC");
        assertThat(insertion.overlapsWith(exon), equalTo(true));
    }

    @Test
    public void symbolicVariantContainsSnv() {
        Contig chr1 = TestContig.of(1, 1000);
        Variant largeIns = Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(1), Position.of(1), "T", "<INS>", 100);
        assertTrue(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(1), "A", "T")));
        assertTrue(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, zeroBased(), Position.of(0), "A", "T")));
        assertFalse(largeIns.contains(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(2), "C", "A")));
        assertTrue(largeIns.contains(Breakend.of(chr1, "bnd_A", Strand.POSITIVE, oneBased(), Position.of(1))));
    }

    @Test
    public void trimMultiAllelicSite() {
        // Given the VCF record:
        // chr1    225725424       .       CTT     C,CT    258.06  FreqFilter      AC=1,1;AF=0.500,0.500;AN=2;DP=7;ExcessHet=3.0103;FS=0.000;MAX_FREQ=3.1360424;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=29.21;SOR=0.941    GT:AD:DP:GQ:PL  1/2:0,4,3:7:58:275,76,58,106,0,85
        Contig chr1 = TestContig.of(1, 249_250_621);
        VariantTrimmer leftShiftingTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

        Variant firstAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "C");
        assertThat(firstAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "C")));

        // throws an IllegalArgumentException without being trimmed first
        Variant secondAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "CT");
        assertThat(secondAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CT", "C")));
    }

    private Variant trim(VariantTrimmer variantTrimmer, Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
        VariantPosition firstTrimmed = variantTrimmer.trim(strand, start.pos(), ref, alt);
        return Variant.of(contig, id, strand, coordinateSystem, start.withPos(firstTrimmed.start()), firstTrimmed.ref(), firstTrimmed.alt());
    }

    @Test
    public void checkGeneContainsVariant() {
        // Load the Human GRCh37.13 assembly from a NCBI assembly report
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // FGFR2 gene is located on chromosome 10 (CM000672.1): 123,237,848-123_357_972 reverse strand. (1-based, positive strand coordinates)
        Contig chr10b37 = b37.contigByName("10");
        GenomicRegion fgfr2Gene = GenomicRegion.of(chr10b37, Strand.POSITIVE, FULLY_CLOSED, 123_237_848, 123_357_972);
        // 10	123256215	.	T	G  - a pathogenic missense variant (GRCh37 VCF coordinates - 1-based, positive strand)
        Variant snv = Variant.of(chr10b37, "", Strand.POSITIVE, oneBased(), Position.of(123_256_215), "T", "G");
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
        assertThat(oneBasedEmpty.withCoordinateSystem(LEFT_OPEN), equalTo(zeroBasedEmpty));
    }

    @Test
    public void parseGenomicRegionFromBedFile() {
        // Load the Human GRCh37.13 assembly from a NCBI assembly report
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // BED uses left-open coordinates, with positions in standard genomic coordinates (i.e. positive strand), with
        // the 6th column indicating the strand. Using the example from - https://grch37.ensembl.org/info/website/upload/bed.html
        GenomicRegion pos1 = parseBedRecord(b37, "chr7\t127471196\t127472363\tPos1\t0\t+");
        GenomicRegion neg1 = parseBedRecord(b37, "chr7\t127475864\t127477031\tNeg1\t0\t-");

        assertThat(pos1.contigName(), equalTo("7"));
        assertThat(pos1.startOnStrand(Strand.POSITIVE), equalTo(127471196));
        assertThat(pos1.endOnStrand(Strand.POSITIVE), equalTo(127472363));
        assertThat(pos1.startOnStrand(Strand.POSITIVE), equalTo(127471196));
        assertThat(pos1.strand(), equalTo(Strand.POSITIVE));

        assertThat(neg1.contigName(), equalTo("7"));
        // a new instance on the positive strand can be created using neg1.toPositiveStrand(), however this will create
        // a new object which may not be wanted in the long term, so to avoid this you can use the start/endOnStrand method
        // which will efficiently calculate the result of neg1.withStrand(POSITIVE).start()/.end()
        assertThat(neg1.startOnStrand(Strand.POSITIVE), equalTo(127475864));
        assertThat(neg1.endOnStrand(Strand.POSITIVE), equalTo(127477031));
        assertThat(neg1.strand(), equalTo(Strand.NEGATIVE));
    }

    private GenomicRegion parseBedRecord(GenomicAssembly genomicAssembly, String bedRecord) {
        String[] fields = bedRecord.split("\t");
        Contig contig = genomicAssembly.contigByName(fields[0]);
        Strand strand = Strand.parseStrand(fields[5]);
        Position start = strand == Strand.POSITIVE ? Position.of(Integer.parseInt(fields[1])) : Position.of(Coordinates.invertPosition(LEFT_OPEN, Integer.parseInt(fields[2]), contig));
        Position end = strand == Strand.POSITIVE ? Position.of(Integer.parseInt(fields[2])) : Position.of(Coordinates.invertPosition(LEFT_OPEN, Integer.parseInt(fields[1]), contig));
        return GenomicRegion.of(contig, strand, LEFT_OPEN, start, end);
    }

    @Test
    public void breakendsAreAlmostLikeNonBreakends() {
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        VcfConverter vcfConverter = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        // VCF file, parsed with the HTSJDK to get a VariantContext instance...
        // CHR	POS	ID	REF	ALT
        // chr1	12345	rs123456	C	T	6	PASS	.
        Variant snv = vcfConverter.convert("chr1", "rs123456", 12345, "CCC", "TCC");
        // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
        Variant ins = vcfConverter.convertSymbolic("chr1", "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
        // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        Variant bnd = vcfConverter.convertBreakend("1", "bnd_U", Position.of(12345), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

        assertThat(snv.ref(), equalTo(ins.ref()));
        assertThat(snv.ref(), equalTo(bnd.ref()));
        assertThat(snv.overlapsWith(ins), equalTo(true));
        assertThat(snv.overlapsWith(bnd), equalTo(true));
        assertThat(snv.isSymbolic(), equalTo(false));
        assertThat(ins.isSymbolic(), equalTo(true));
        assertThat(bnd.isSymbolic(), equalTo(true));
        assertThat(bnd.isBreakend(), equalTo(true));
    }

    @Test
    public void nonCanonicalBreakend_mantaUnresolved() {
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // 1	166448783	gnomAD-SV_v2.1_BND_1_4095	N	<BND>	892	UNRESOLVED	END=166448784;SVTYPE=BND;SVLEN=-1;CHR2=6;POS2=166448783;END2=166448784;ALGORITHMS=manta;EVIDENCE=PE;UNRESOLVED_TYPE=SINGLE_ENDER_+-;
        Breakend left = Breakend.of(b37.contigById(1), "", Strand.POSITIVE, FULLY_CLOSED, Position.of(166448783));
        Breakend right = Breakend.unresolved();
        Variant bnd = Variant.of("gnomAD-SV_v2.1_BND_1_4095", left, right, "N", "");
        assertThat(bnd.ref(), equalTo("N"));
        assertThat(bnd.alt(), equalTo(""));
        assertThat(bnd.start(), equalTo(166448783));
        assertThat(bnd.end(), equalTo(166448783));
        assertThat(bnd.length(), equalTo(0));
        assertThat(bnd.changeLength(), equalTo(0));
    }

    @Test
    public void nonCanonicalBreakend_snifflesTRA() {
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // https://github.com/fritzsedlazeck/Sniffles/issues/73  - should run with  "--report_BND true"
        // 1 	797316 	TRA0029399SUR 	N 	<TRA> 	. 	PASS 	SUPP=2;AVGLEN=100000;med_start=797265;med_stop=797265;SVTYPE=TRA;SVMETHOD=SURVIVORv2;CHR2=8;END=245650;STRANDS=++;
        Breakend left = Breakend.of(b37.contigById(1), "", Strand.POSITIVE, FULLY_CLOSED, Position.of(797316));
        // CHR2=8;END=245650;STRANDS=++;
        Breakend right = Breakend.of(b37.contigById(8), "", Strand.POSITIVE, FULLY_CLOSED, Position.of(245650));
        Variant bnd = Variant.of("TRA0029399SUR", left, right, "N", "");
        assertThat(bnd.ref(), equalTo("N"));
        assertThat(bnd.alt(), equalTo(""));
        assertThat(bnd.start(), equalTo(797316));
        assertThat(bnd.end(), equalTo(797316));
        assertThat(bnd.length(), equalTo(0));
        assertThat(bnd.changeLength(), equalTo(0));
    }
}
