package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.sequence.NucleotideSeq;
import org.openjdk.jol.info.ClassLayout;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CompactSequenceVariantTest {
    private final TestContig contig = TestContig.of(1, 100000);

    @ParameterizedTest
    @CsvSource({
            "N, A",
            "D, 12344",
            "C, <DEL>",
            "C, *",
            "C, ATG]CATGC[",
            "C, ATGCATGCATG",
    })
    void illegalCompactVariantThrowsException(String ref, String alt) {
        Exception result = assertThrows(IllegalArgumentException.class, () ->
                CompactSequenceVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, ref, alt));
        assertThat(result.getMessage(), equalTo("Unable to represent ref=" + ref + ", alt=" + alt + " (" + (ref.length() + alt.length()) + " bases) as compact variant. Length of (ref + alt) must be <= 11 bases and only contain characters [A, C, G, T, a, c, g, t]."));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 12345",
            "ZERO_BASED, 12344"
    })
    void coordinateSystem(CoordinateSystem coordinateSystem, int start) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, start, 12345), "A", "T");
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 12345, false",
            "ZERO_BASED, 12344, true"
    })
    void isZeroBased(CoordinateSystem coordinateSystem, int start, boolean expected) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, start, 12345), "A", "T");
        assertThat(instance.isZeroBased(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 12345, true",
            "ZERO_BASED, 12344, false"
    })
    void isOneBased(CoordinateSystem coordinateSystem, int start, boolean expected) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(coordinateSystem, start, "A"), "A", "T");
        assertThat(instance.isOneBased(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, 1", // test first base
            "ZERO_BASED, 0", // test first base
            "ONE_BASED, 12345",
            "ZERO_BASED, 12344",
            "ONE_BASED, 100000", // test last base
            "ZERO_BASED, 99999", // test last base
    })
    void start(CoordinateSystem coordinateSystem, int start) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(coordinateSystem, start, "A"), "A", "T");
        assertThat(instance.start(), equalTo(start));
    }

    @ParameterizedTest
    @CsvSource({
            //SNP
            "ONE_BASED, 12345, 12345, A, T",
            "ZERO_BASED, 12344, 12345, A, T",
            //DEL
            "ONE_BASED, 12345, 12346, AG, A",
            "ZERO_BASED, 12345, 12347, AG, A",
            "ZERO_BASED, 12346, 12347, G, ''",
            //INS
            "ONE_BASED, 12345, 12345, A, AG",
            "ZERO_BASED, 12344, 12345, A, AG",
            "ZERO_BASED, 12345, 12345, '', G",
            //DELINS
            "ONE_BASED, 12345, 12348, TAGC, TG",
            "ONE_BASED, 12345, 12349, TAGCT, TG",
            "ZERO_BASED, 12344, 12346, TG, TAGC",

            "ZERO_BASED, 12344, 12345, A, T",
            "ZERO_BASED, 12344, 12345, C, G",
            "ZERO_BASED, 12344, 12345, G, C",
            "ZERO_BASED, 12344, 12345, G, G",
            "ZERO_BASED, 12344, 12345, A, GA",
            "ZERO_BASED, 12344, 12345, T, C",
            "ZERO_BASED, 12344, 12346, TG, TAGC",
            "ZERO_BASED, 12344, 12346, TG, TAGTC",
            "ZERO_BASED, 12345, 12345, '', G",
            "ZERO_BASED, 12345, 12347, AG, A",
            "ZERO_BASED, 12346, 12347, G, ''",
            "ZERO_BASED, 12346, 12346, '', TTTTTTTTTTT",
            "ZERO_BASED, 12346, 12357, TTTTTTTTTTT, ''",
            "ONE_BASED,  12347, 12357, TTTTTTTTTTT, ''",
    })
    void end(CoordinateSystem coordinateSystem, int start, int end, String ref, String alt) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, start, end), ref, alt);

        String variant = (coordinateSystem.isOneBased() ? 1 : 0) + "-" + start + "-" + ref + "-" + alt;
//        System.out.println(variant + " " + instance.bits() + " " + Long.toHexString(instance.bits()) + " " + Long.toBinaryString(instance.bits()));

        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.start(), equalTo(start));
        assertThat(instance.end(), equalTo(end));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
        assertThat(instance.length(), equalTo(ref.length()));
        assertThat(instance.changeLength(), equalTo(alt.length() - ref.length()));
    }

    @Disabled("Just checking object sizes - not a test")
    @Test
    void objectSize() {
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345);
        CompactSequenceVariant compact = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, coordinates, "T", "A");
        ClassLayout compactClassLayout = ClassLayout.parseInstance(compact);
        System.out.println(compactClassLayout.toPrintable());
        // org.monarchinitiative.svart.impl.CompactSequenceVariant object internals:
        //OFF  SZ                                      TYPE DESCRIPTION                          VALUE
        //  0   8                                           (object header: mark)                0x0000000000000001 (non-biasable; age: 0)
        //  8   4                                           (object header: class)               0xf8031000
        // 12   4        org.monarchinitiative.svart.Contig CompactSequenceVariant.contig        (object)
        // 16   8                                      long CompactSequenceVariant.bits          53017364660227
        // 24   4                          java.lang.String CompactSequenceVariant.id            (object)
        // 28   4   org.monarchinitiative.svart.VariantType CompactSequenceVariant.variantType   (object)
        //Instance size: 32 bytes
        //Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

        GenomicVariant fat = DefaultSequenceVariant.of(contig, "", Strand.POSITIVE, coordinates, "A", "GTGCTAGTGCC");
        ClassLayout fatClassLayout = ClassLayout.parseInstance(fat);
        System.out.println(fatClassLayout.toPrintable());
//        assertThat(compactClassLayout.instanceSize(), lessThan(fatClassLayout.instanceSize()));
        printClassLayout(coordinates);
        printClassLayout(Strand.POSITIVE);
        printClassLayout("");
        printClassLayout("A");
        printClassLayout(new byte[]{65});
        printClassLayout("GTGCTAGTGCC");
        printClassLayout(new byte[]{71, 84, 71, 67, 84, 65, 71, 84, 71, 67, 67});
        printClassLayout(new ComposedGenomicVariant(compact));
        printClassLayout(new SequenceGenomicVariant(contig, "", Strand.POSITIVE, coordinates, "A", "T"));
    }

    static record ComposedGenomicVariant(GenomicVariant genomicVariant) {
    }

    static class SequenceGenomicVariant {
        private final Contig contig;
        private final String id;
        private final Strand strand;
        private final Coordinates coordinates;
        private final String ref;
        private final String alt;
        private final VariantType variantType;

        public SequenceGenomicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
            this.contig = contig;
            this.id = id;
            this.strand = strand;
            this.coordinates = coordinates;
            this.ref = ref;
            this.alt = alt;
            this.variantType = VariantType.parseType(ref, alt);
        }
    }

    private static void printClassLayout(Object object) {
        ClassLayout classLayout = ClassLayout.parseInstance(object);
        System.out.println(classLayout.toPrintable());
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE",
            "NEGATIVE"
    })
    void strand(Strand strand) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", strand, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345), "A", "T");
        assertThat(instance.strand(), equalTo(strand));
    }

    @Test
    void variantType() {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345), "A", "T");
        assertThat(instance.variantType(), equalTo(VariantType.SNV));
    }

    @ParameterizedTest
    @CsvSource({
            "A, T, true",
            "ATGC, '', true",
            "AAAAAAAAAAA, '', true", // just long enough
            "atgc, '', true", // check lower-case is also legal input
            "AAAAAAAAAAA, 'T', false", // too long
            "N, '', false",
            "A, <INS>, false",
            "'', *, false",
            "'', ., false",
    })
    void testCanBeCompactVariant(String ref, String alt, boolean expected) {
        assertThat(CompactSequenceVariant.canBeCompactVariant(ref, alt), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "A",
            "T",
            "G",
            "C",
            "AC",
            "ATC",
            "TCG",
            "AGTC",
            "CGAT",
            "AAAA",
            "AAAT",
            "CAAA",
            "CAGT",
            "CCCC",
            "GGGG",
            "GCTC",
            "TAAA",
            "TCAG",
            "TTTT",
            "AGTCC",
            "TTTTT",
            "AGTTCC",
            "AGTTCAC",
            "AGTTCACG",
            "AGTTCACGC",
            "AGGTTCACGC",
    })
    void ref(String ref) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, 12345, ref), ref, "T");
        assertThat(instance.ref(), equalTo(ref));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ZERO_BASED",
            "POSITIVE, ONE_BASED",
            "NEGATIVE, ZERO_BASED",
            "NEGATIVE, ONE_BASED",
    })
    void maxIns(Strand strand, CoordinateSystem coordinateSystem) {
        Coordinates coordinates = Coordinates.ofAllele(coordinateSystem, 12345, "");
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", strand, coordinates, "", "TACGTACGTAG");
        assertThat(instance.coordinates(), equalTo(coordinates));
        assertThat(instance.ref(), equalTo(""));
        assertThat(instance.alt(), equalTo("TACGTACGTAG"));
        assertThat(instance.strand(), equalTo(strand));
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ZERO_BASED",
            "POSITIVE, ONE_BASED",
            "NEGATIVE, ZERO_BASED",
            "NEGATIVE, ONE_BASED",
    })
    void maxDel(Strand strand, CoordinateSystem coordinateSystem) {
        Coordinates coordinates = Coordinates.ofAllele(coordinateSystem, 12345, "TACGTACGTAG");
        GenomicVariant instance = CompactSequenceVariant.of(contig, "", strand, coordinates, "TACGTACGTAG", "");
        assertThat(instance.coordinates(), equalTo(coordinates));
        assertThat(instance.ref(), equalTo("TACGTACGTAG"));
        assertThat(instance.alt(), equalTo(""));
        assertThat(instance.strand(), equalTo(strand));
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "A",
            "T",
            "G",
            "C",
            "AC",
            "ATC",
            "AGTC",
            "AGTCC",
            "AGTTCC",
            "AGTTCAC",
            "AGTTCACG",
            "AGTTCACGC",
            "AGGTTCACGC",
    })
    void alt(String alt) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, 12345, "A"), "T", alt);
        assertThat(instance.alt(), equalTo(alt));
    }

    @ParameterizedTest()
    @CsvSource({
            "POSITIVE, ONE_BASED, 12345, ZERO_BASED, 12344",
            "POSITIVE, ZERO_BASED, 12344, ONE_BASED, 12345,",
            "NEGATIVE, ONE_BASED, 12345, ZERO_BASED, 12344",
            "NEGATIVE, ZERO_BASED, 12344, ONE_BASED, 12345,",
    })
    void withCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem, int start, CoordinateSystem targetSystem, int expectedStart) {
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", strand, Coordinates.ofAllele(coordinateSystem, start, "A"), "A", "T");
        CompactSequenceVariant expected = CompactSequenceVariant.of(contig, "", strand, Coordinates.ofAllele(targetSystem, expectedStart, "A"), "A", "T");
        assertThat(instance.withCoordinateSystem(targetSystem), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED, 12345, TAGC, TG",
            "POSITIVE, ZERO_BASED, 12344, AT, T",
            "NEGATIVE, ONE_BASED, 87653, GCTA, CA",
            "NEGATIVE, ZERO_BASED, 87654, TA, A",
            "NEGATIVE, ZERO_BASED, 87654, T, A",
            "NEGATIVE, ZERO_BASED, 87654, T, ''",
            "NEGATIVE, ZERO_BASED, 87654, '', T",
            "POSITIVE, ZERO_BASED, 87654, T, ''",
            "POSITIVE, ZERO_BASED, 87654, '', T",
            "POSITIVE, ONE_BASED, 12345, TAGCTAG, CGGA",
            "POSITIVE, ZERO_BASED, 12344, TAGCTAG, CGGA",
    })
    void withStrand(Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        Coordinates coordinates = Coordinates.ofAllele(coordinateSystem, start, ref);
        CompactSequenceVariant instance = CompactSequenceVariant.of(contig, "", strand, coordinates, ref, alt);
        Strand targetStrand = strand.opposite();
        String refRevComp = NucleotideSeq.reverseComplement(ref);
        String altRevComp = NucleotideSeq.reverseComplement(alt);
        CompactSequenceVariant expected = CompactSequenceVariant.of(contig, "", targetStrand, coordinates.invert(contig), refRevComp, altRevComp);
        assertThat(instance.withStrand(targetStrand), equalTo(expected));
    }

    @Test
    void thereAndBackAgain() {
        var forwardOneBased = CompactSequenceVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "ACG", "TGCCTA");
        var reverseOneBased = forwardOneBased.toOppositeStrand();
        var reverseZeroBased = reverseOneBased.toZeroBased();
        var forwardZeroBased = reverseZeroBased.toOppositeStrand();
        assertThat(forwardZeroBased.toOneBased(), equalTo(forwardOneBased));
    }

    @Test
    void allTheStartsAndEndsSnp() {
        Coordinates coordinates = Coordinates.oneBased(12345, 12345);
        GenomicVariant instance = CompactSequenceVariant.of(contig, Strand.POSITIVE, coordinates, "A", "T");
        assertThat(instance.start(), equalTo(12345));
        assertThat(instance.end(), equalTo(12345));

        assertThat(instance.startOnStrand(Strand.NEGATIVE), equalTo(coordinates.invertStart(contig)));
        assertThat(instance.endOnStrand(Strand.NEGATIVE), equalTo(coordinates.invertEnd(contig)));

        assertThat(instance.startStd(), equalTo(instance.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));
        assertThat(instance.endStd(), equalTo(instance.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));

        assertThat(instance.startZeroBased(), equalTo(12344));
        assertThat(instance.endZeroBased(), equalTo(12345));

        assertThat(instance.startZeroBased(Strand.NEGATIVE), equalTo((contig.length() - instance.end())));
        assertThat(instance.endZeroBased(Strand.NEGATIVE), equalTo((contig.length() - instance.startZeroBased())));

        assertThat(instance.startOneBased(), equalTo(12345));
        assertThat(instance.endOneBased(), equalTo(12345));

        assertThat(instance.startOneBased(Strand.NEGATIVE), equalTo((contig.length() + 1) - instance.end()));
        assertThat(instance.endOneBased(Strand.NEGATIVE), equalTo((contig.length() + 1) - instance.start()));
    }

    @Test
    void allTheStartsAndEndsDel() {
        Coordinates coordinates = Coordinates.ofAllele(CoordinateSystem.ONE_BASED, 12345, "ACG");
        GenomicVariant instance = CompactSequenceVariant.of(contig, Strand.POSITIVE, coordinates, "ACG", "T");
        assertThat(instance.start(), equalTo(coordinates.start()));
        assertThat(instance.end(), equalTo(coordinates.end()));

        assertThat(instance.startOnStrand(Strand.NEGATIVE), equalTo(coordinates.invertEnd(contig)));
        assertThat(instance.endOnStrand(Strand.NEGATIVE), equalTo(coordinates.invertStart(contig)));

        assertThat(instance.startStd(), equalTo(instance.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));
        assertThat(instance.endStd(), equalTo(instance.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ZERO_BASED)));

        assertThat(instance.startZeroBased(), equalTo(coordinates.startZeroBased()));
        assertThat(instance.endZeroBased(), equalTo(coordinates.end()));

        assertThat(instance.startZeroBased(Strand.NEGATIVE), equalTo((contig.length() - instance.end())));
        assertThat(instance.endZeroBased(Strand.NEGATIVE), equalTo((contig.length() - instance.startZeroBased())));

        assertThat(instance.startOneBased(), equalTo(coordinates.startOneBased()));
        assertThat(instance.endOneBased(), equalTo(coordinates.end()));

        assertThat(instance.startOneBased(Strand.NEGATIVE), equalTo((contig.length() + 1) - instance.end()));
        assertThat(instance.endOneBased(Strand.NEGATIVE), equalTo((contig.length() + 1) - instance.start()));
    }

    @Test
    void smokeTestRandomVariants() {
        GenomicAssembly genomicAssembly = GenomicAssemblies.GRCh38p13();
        Random rand = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            SimpleVariant expected = SimpleVariant.nextSimpleVariant(genomicAssembly, rand);
            GenomicVariant variant = expected.toCompactVariant();
            assertThat(variant.contig(), equalTo(expected.contig()));
            assertEquals(variant.strand(), expected.strand());
            CoordinateSystem coordinateSystem = variant.coordinateSystem();
            assertEquals(coordinateSystem, expected.coordinateSystem());
            assertEquals(variant.start(), expected.start());
            assertEquals(variant.end(), expected.end());
            assertThat(variant.ref(), equalTo(expected.ref()));
            assertThat(variant.alt(), equalTo(expected.alt()));
            var otherCoordinateSystem = variant.withCoordinateSystem(coordinateSystem == CoordinateSystem.ONE_BASED ? CoordinateSystem.ZERO_BASED : CoordinateSystem.ONE_BASED);
            var oppositeStrand = otherCoordinateSystem.toOppositeStrand();
            var sameStrand = oppositeStrand.toOppositeStrand();
            assertThat(sameStrand.withCoordinateSystem(coordinateSystem), equalTo(variant));
        }
    }

    record SimpleVariant(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt) {
        private static final byte[] BASES = {'A', 'C', 'G', 'T'};

        GenomicVariant toCompactVariant() {
            return CompactSequenceVariant.of(contig, strand, Coordinates.of(coordinateSystem, start, end), ref, alt);
        }

        GenomicVariant toSequenceVariant() {
            return DefaultSequenceVariant.of(contig, strand, Coordinates.of(coordinateSystem, start, end), ref, alt);
        }

        static SimpleVariant nextSimpleVariant(GenomicAssembly genomicAssembly, Random rand) {
            Contig chr = genomicAssembly.contigById(rand.nextInt(1, 25));
            int refLen = rand.nextInt(0, CompactSequenceVariant.MAX_BASES);
            int altLen = rand.nextInt(0, CompactSequenceVariant.MAX_BASES - refLen);
            String ref = allele(refLen, rand);
            String alt = allele(altLen, rand);
            int start = rand.nextInt(0, chr.length() - refLen);
            Strand strand = rand.nextBoolean() ? Strand.POSITIVE : Strand.NEGATIVE;
            CoordinateSystem coordinateSystem = rand.nextBoolean() ? CoordinateSystem.ZERO_BASED : CoordinateSystem.ONE_BASED;
            int end = GenomicVariant.calculateEnd(start, coordinateSystem, ref, alt);
            return new SimpleVariant(chr, strand, coordinateSystem, start, end, ref, alt);
        }

        private static String allele(int refLen, Random rand) {
            byte[] refBytes = new byte[refLen];
            for (int j = 0; j < refLen; j++) {
                refBytes[j] = BASES[rand.nextInt(0, 4)];
            }
            return new String(refBytes);
        }
    }
}