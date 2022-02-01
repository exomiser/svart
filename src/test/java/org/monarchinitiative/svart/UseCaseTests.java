package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;
import org.monarchinitiative.svart.util.VcfConverter;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class UseCaseTests {

    @Test
    public void createDonorAcceptorRegionFromExon() {
        TestContig contig = TestContig.of(1, 100);
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.zeroBased(), 50, 70);

        // DONOR
        GenomicRegion donor = GenomicRegion.of(exon.contig(), exon.strand(), exon.coordinateSystem(), exon.end() - 3, exon.end() + 6);
        assertThat(donor.overlapsWith(exon), equalTo(true));
        assertThat(donor.length(), equalTo(9));

        // ACCEPTOR
        GenomicRegion acceptor = GenomicRegion.of(exon.contig(), exon.strand(), exon.coordinateSystem(), exon.start() - 8 , exon.start() + 3);
        assertThat(acceptor.overlapsWith(exon), equalTo(true));
        assertThat(acceptor.length(), equalTo(11));
    }

    @Test
    public void insertionLiesInExon() {
        TestContig contig = TestContig.of(1, 100);
        GenomicRegion exon = GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(), 50, 70);

        GenomicVariant insertion = GenomicVariant.of(contig, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 60, "A", "AC");
        assertThat(insertion.overlapsWith(exon), equalTo(true));
    }

    @Test
    public void symbolicVariantContainsSnv() {
        Contig chr1 = TestContig.of(1, 1000);
        GenomicVariant largeIns = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 1, 1, "T", "<INS>", 100);
        assertTrue(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 1, "A", "T")));
        assertTrue(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.zeroBased(), 0, "A", "T")));
        assertFalse(largeIns.contains(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 2, "C", "A")));
        assertTrue(largeIns.contains(GenomicBreakend.of(chr1, "bnd_A", Strand.POSITIVE, CoordinateSystem.oneBased(), 1, 0)));
    }

    @Test
    public void trimMultiAllelicSite() {
        // Given the VCF record:
        // chr1    225725424       .       CTT     C,CT    258.06  FreqFilter      AC=1,1;AF=0.500,0.500;AN=2;DP=7;ExcessHet=3.0103;FS=0.000;MAX_FREQ=3.1360424;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=29.21;SOR=0.941    GT:AD:DP:GQ:PL  1/2:0,4,3:7:58:275,76,58,106,0,85
        Contig chr1 = TestContig.of(1, 249_250_621);
        VariantTrimmer leftShiftingTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

        GenomicVariant firstAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "C");
        assertThat(firstAllele, equalTo(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "C")));

        GenomicVariant secondAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "CT");
        assertThat(secondAllele, equalTo(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CT", "C")));
    }

    private GenomicVariant trim(VariantTrimmer variantTrimmer, Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        VariantPosition trimmed = variantTrimmer.trim(strand, start, ref, alt);
        return GenomicVariant.of(contig, id, strand, coordinateSystem, trimmed.start(), trimmed.ref(), trimmed.alt());
    }

    @Test
    public void checkGeneContainsVariant() {
        // Load the Human GRCh37.13 assembly from a NCBI assembly report
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // FGFR2 gene is located on chromosome 10 (CM000672.1): 123,237,848-123_357_972 reverse strand. (1-based, positive strand coordinates)
        Contig chr10b37 = b37.contigByName("10");
        GenomicRegion fgfr2Gene = GenomicRegion.of(chr10b37, Strand.POSITIVE, CoordinateSystem.oneBased(), 123_237_848, 123_357_972);
        // 10	123256215	.	T	G  - a pathogenic missense variant (GRCh37 VCF coordinates - 1-based, positive strand)
        GenomicVariant snv = GenomicVariant.of(chr10b37, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 123_256_215, "T", "G");
        // Because svart knows about coordinate systems and strands it is possible to...
        // keep the gene on the positive strand:
        // GenomicRegion{contig=10, strand=+, coordinateSystem=ONE_BASED, startPosition=123237848, endPosition=123357972}
        assertThat(fgfr2Gene.contains(snv), equalTo(true));
        // or use it on the negative strand:
        // GenomicRegion{contig=10, strand=-, coordinateSystem=ONE_BASED, startPosition=12176776, endPosition=12296900}
        assertThat(fgfr2Gene.toNegativeStrand().contains(snv), equalTo(true));
    }

    @Test
    public void emptyRegionsWithDifferentCoordinateSystems() {
        // these are empty regions and can be used to represent a 'slice' in-between two bases
        GenomicRegion oneBasedEmpty = GenomicRegion.of(Contig.unknown(), Strand.POSITIVE, CoordinateSystem.oneBased(), 1, 0);
        GenomicRegion zeroBasedEmpty = GenomicRegion.of(Contig.unknown(), Strand.POSITIVE, CoordinateSystem.zeroBased(), 0, 0);

        assertThat(oneBasedEmpty.contains(zeroBasedEmpty), equalTo(true));
        // convert coordinate systems using convenience methods
        assertThat(oneBasedEmpty.toZeroBased(), equalTo(zeroBasedEmpty));
        // convert coordinate systems using specific systems
        assertThat(oneBasedEmpty.withCoordinateSystem(CoordinateSystem.zeroBased()), equalTo(zeroBasedEmpty));
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
        int start = strand == Strand.POSITIVE ? Integer.parseInt(fields[1]) : Coordinates.invertCoordinate(CoordinateSystem.zeroBased(), contig, Integer.parseInt(fields[2]));
        int end = strand == Strand.POSITIVE ? Integer.parseInt(fields[2]) : Coordinates.invertCoordinate(CoordinateSystem.zeroBased(), contig, Integer.parseInt(fields[1]));
        return GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(), start, end);
    }

    @Test
    public void breakendsAreAlmostLikeNonBreakends() {
        GenomicAssembly b37 = GenomicAssemblies.GRCh37p13();
        VcfConverter vcfConverter = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        // VCF file, parsed with the HTSJDK to get a VariantContext instance...
        // CHR	POS	ID	REF	ALT
        // chr1	12345	rs123456	C	T	6	PASS	.
        GenomicVariant snv = vcfConverter.convert(vcfConverter.parseContig("chr1"), "rs123456", 12345, "CCC", "TCC");
        // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
        GenomicVariant ins = vcfConverter.convertSymbolic(vcfConverter.parseContig("chr1"), "", 12345, 12345, "C", "<INS>", 200);
        // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        GenomicVariant bnd = vcfConverter.convertBreakend(vcfConverter.parseContig("1"), "bnd_U", 12345, ConfidenceInterval.precise(), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

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
        GenomicAssembly b37 = GenomicAssemblies.GRCh37p13();
        // 1	166448783	gnomAD-SV_v2.1_BND_1_4095	N	<BND>	892	UNRESOLVED	END=166448784;SVTYPE=BND;SVLEN=-1;CHR2=6;POS2=166448783;END2=166448784;ALGORITHMS=manta;EVIDENCE=PE;UNRESOLVED_TYPE=SINGLE_ENDER_+-;
        GenomicBreakend left = GenomicBreakend.of(b37.contigById(1), "", Strand.POSITIVE, CoordinateSystem.oneBased(), 166448784, 166448783);
        GenomicBreakend right = GenomicBreakend.unresolved(CoordinateSystem.oneBased());
        GenomicVariant bnd = GenomicVariant.of("gnomAD-SV_v2.1_BND_1_4095", left, right, "N", "");
        assertThat(bnd.ref(), equalTo("N"));
        assertThat(bnd.alt(), equalTo(""));
        assertThat(bnd.start(), equalTo(166448783));
        assertThat(bnd.end(), equalTo(166448783));
        assertThat(bnd.length(), equalTo(1));
        assertThat(bnd.changeLength(), equalTo(0));
    }

    @Test
    public void nonCanonicalBreakend_snifflesTRA() {
        GenomicAssembly b37 = GenomicAssemblies.GRCh37p13();
        // https://github.com/fritzsedlazeck/Sniffles/issues/73  - should run with  "--report_BND true"
        // 1 	797316 	TRA0029399SUR 	N 	<TRA> 	. 	PASS 	SUPP=2;AVGLEN=100000;med_start=797265;med_stop=797265;SVTYPE=TRA;SVMETHOD=SURVIVORv2;CHR2=8;END=245650;STRANDS=++;
        GenomicBreakend left = GenomicBreakend.of(b37.contigById(1), "", Strand.POSITIVE, Coordinates.of(CoordinateSystem.oneBased(), 797317, 797316));
        // CHR2=8;END=245650;STRANDS=++;
        GenomicBreakend right = GenomicBreakend.of(b37.contigById(8), "", Strand.POSITIVE, Coordinates.of(CoordinateSystem.oneBased(), 245651, 245650));
        GenomicVariant bnd = GenomicVariant.of("TRA0029399SUR", left, right, "N", "");
        assertThat(bnd.ref(), equalTo("N"));
        assertThat(bnd.alt(), equalTo(""));
        assertThat(bnd.start(), equalTo(797316));
        assertThat(bnd.end(), equalTo(797316));
        assertThat(bnd.length(), equalTo(1));
        assertThat(bnd.changeLength(), equalTo(0));
    }
}
