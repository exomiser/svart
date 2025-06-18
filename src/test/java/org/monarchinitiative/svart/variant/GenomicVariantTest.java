package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.ConfidenceInterval;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.svart.TestContigs.chr2;

class GenomicVariantTest {
    private static final Contig chr1 = GenomicAssemblies.GRCh38p13().contigById(1);

    @ParameterizedTest
    @CsvSource({
            "100, 100, A, T, 0",
            "100, 100, T, ., 0",
            "100, 102, AGC, GCT, 0",
            "100, 100, G, '', -1",
            "100, 102, AGC, G, -2",
            "100, 100, A, GCT, 2",
            "100, 99, '', CT, 2",
            "100, 200, A, <DEL>, -100",
            "100, 100, A, 'A[2:321682[', 0",
    })
    void staticConstructorTests(int start, int end, String ref, String alt, int changeLength) {
        // bleurgh! So many static scoped-constructors!
        GenomicVariant expected = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength);

        if (expected.isSymbolic() || expected.isBreakend()) {
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt).build());

            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt).build());
        } else {
            assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt), equalTo(expected));
            assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt), equalTo(expected));
            assertThat(GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt).build(), equalTo(expected));

            assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt), equalTo(expected));
            assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt), equalTo(expected));
            assertThat(GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt).build(), equalTo(expected));
        }

        assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength).build(), equalTo(expected));

        assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end, ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end, ref, alt, changeLength), equalTo(expected));

        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength, "", ""), equalTo(expected));
        assertThat(GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength)
                .id("")
                .mateId("")
                .eventId("")
                .build(), equalTo(expected));
    }

    /**
     * see <href <a href="https://academic.oup.com/bioinformatics/article/36/6/1902/5628222">SPDI: data model for variants and applications at NCBI</a>
     * and supplementary material Table S1
     * <p>
     * 2.5 Alignment projection special cases
     * <p>
     * Two special cases must be considered carefully when projecting through alignments: alignment orientation changes
     * and gaps in the alignment. The forward orientation of mRNA is determined by the direction of transcription (5′–3′)
     * and the associated genes and their transcript products may be reverse to the chromosome. When mapping variants
     * across such alignments, it is important to reverse-complement the inserted sequence, as the SPDI model uses only
     * the positive strand. In addition, extra care must be taken when mapping an insertion variant with zero-length
     * deletion sequence, because alignments use base coordinates, not SPDI’s interbase coordinates. It is usually
     * convenient to convert to an insert-before semantic (which does not alter the numbering system) when computing the
     * mapping. However, because directionality changes when strand orientation changes, insert-before becomes
     * insert-after. In order to return to the insert-before semantic, the position must be increased by one (Fig. 2A).
     */
    @Test
    void testZeroLengthInsertionStrandFlipChangesPosition() {
        Contig chr1 = TestContig.of(1, 9);
        GenomicVariant ins = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.zeroBased(4, 4), "", "T");
        assertThat(ins.startOnStrand(Strand.NEGATIVE), equalTo(5));
    }

    @ParameterizedTest
    @CsvSource({
            "<>, A, ref",
            "A, WIBBLE>, alt",
            "N, <WIBBLE, alt",
            "., AT, ref",
            "*, AT, ref",
            "., ., ref",
            "*, *, ref",
            "<INS>, AT, ref",
            "A, ART, alt",
            "A, 'A TT', alt",
            "'', W, alt",
            "a, R, alt",
            "R, a, ref",
            "A, ?, alt",
            "?, ?, ref", // both illegal, but ref is checked first
    })
    void nonsenseInputTests(String ref, String alt, Allele illegalAllele) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 2, ref, alt, 2));
        assertThat(exception.getMessage(), equalTo("Illegal " + illegalAllele + " allele: " + (illegalAllele == Allele.ref? ref : alt)));
    }

    private enum Allele {
        ref, alt
    }

    @Test
    void emptyVariant() {
        GenomicVariant instance = GenomicVariant.builder().build();
        assertThat(instance, equalTo(GenomicVariant.of(Contig.unknown(), Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 1, 0), "", "")));
    }

    @Test
    void buildPreciseInsertion() {
        GenomicVariant instance = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").id("rs1234567").build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("rs1234567"));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.changeLength(), equalTo(2));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("TAA"));
    }

    @Test
    void buildWithVariant() {
        GenomicVariant oneBasedVariant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant instance = GenomicVariant.builder().variant(oneBasedVariant).build();
        assertThat(instance.contig(), equalTo(oneBasedVariant.contig()));
        assertThat(instance.id(), equalTo(oneBasedVariant.id()));
        assertThat(instance.strand(), equalTo(oneBasedVariant.strand()));
        assertThat(instance.coordinateSystem(), equalTo(oneBasedVariant.coordinateSystem()));
        assertThat(instance.start(), equalTo(oneBasedVariant.start()));
        assertThat(instance.end(), equalTo(oneBasedVariant.end()));
        assertThat(instance.changeLength(), equalTo(oneBasedVariant.changeLength()));
        assertThat(instance.ref(), equalTo(oneBasedVariant.ref()));
        assertThat(instance.alt(), equalTo(oneBasedVariant.alt()));
    }

    @Test
    void buildWithVariantToCoordinateSystemAndStrand() {
        GenomicVariant oneBasedVariant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant.Builder instance = GenomicVariant.builder().variant(oneBasedVariant);
        GenomicVariant oneBased = instance.build();
        assertThat(instance.asOneBased().build(), equalTo(oneBased));
        assertThat(instance.asZeroBased().build(), equalTo(oneBased.toZeroBased()));
        assertThat(instance.asZeroBased().onNegativeStrand().build(), equalTo(oneBased.toZeroBased().toNegativeStrand()));
        assertThat(instance.asZeroBased().onNegativeStrand().onPositiveStrand().asOneBased().build(), equalTo(oneBased));
    }

    @ParameterizedTest
    @CsvSource({
            // SNV/INV/MNV
            "ONE_BASED, 5, G, T,    5",
            "ZERO_BASED,    5, G, T,    6",
            // INS
            "ONE_BASED, 5, GA, T,    6",
            "ZERO_BASED,    5, GA, T,    7",
            // DEL
            "ONE_BASED, 5, G, AT,    5",
            "ZERO_BASED,    5, G, AT,    6",
    })
    void builderAddsMissingEndAndLength(CoordinateSystem coordinateSystem, int start, String ref, String alt, int expectEnd) {
        GenomicVariant instance = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, coordinateSystem, start, ref, alt).build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo(""));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.start(), equalTo(start));
        assertThat(instance.end(), equalTo(expectEnd));
        assertThat(instance.length(), equalTo(ref.length()));
        assertThat(instance.changeLength(), equalTo(alt.length() - ref.length()));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
    }

    @Test
    void buildIllegalSymbolicInsertion() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "<INS>").build());
    }

    @Test
    void buildThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "A[1:2]").build());
    }

    @Test
    void testSymbolicAllelesWithoutLengthThrowsInformativeException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder()
                .variant(chr1, Strand.POSITIVE, Coordinates.oneBased(1, 200), "N", "<DEL>")
                .build()
        );
        assertThat(exception.getMessage(), equalTo("Missing changeLength for symbolic alt allele <DEL>"));
    }

    @Test
    void testIncorrectCoordinatesLengthThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(1, 200), "AT", "A")
        );
        assertThat(exception.getMessage(), equalTo("Ref allele length of 2 inconsistent with Coordinates{coordinateSystem=ONE_BASED, start=1, end=200} (length 200) ref=AT, alt=A"));
    }

    @Test
    void testAlleleChangeLengthMismatchThrowsException() {
        GenomicVariant instance = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(100, 101), "AT", "A", -2);
        GenomicVariant expected = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(100, 101), "AT", "A");
        assertThat(instance, equalTo(expected));
    }

    @Test
    void buildSymbolicDeletion() {
        //2    321682 .    T    <DEL>   6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 321_682, ConfidenceInterval.of(-56, 20), 321_887, ConfidenceInterval.of(-10, 62));
        String ref = "T";
        String alt = "<DEL>";
        int changeLength = -205;

        GenomicVariant instance = GenomicVariant.builder()
                .variant(chr1, Strand.POSITIVE, coordinates, ref, alt, changeLength)
                .id(".")
                .build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("."));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.start(), equalTo(321_682));
        assertThat(instance.startConfidenceInterval(), equalTo(ConfidenceInterval.of(-56, 20)));
        assertThat(instance.end(), equalTo(321_887));
        assertThat(instance.endConfidenceInterval(), equalTo(ConfidenceInterval.of(-10, 62)));
        assertThat(instance.changeLength(), equalTo(changeLength));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
        assertThat(instance.variantType(), equalTo(VariantType.DEL));
    }

    @Test
    void naturalOrdering() {
        GenomicVariant first = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TA");
        GenomicVariant firstB = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, "A", "TAA");
        GenomicVariant firstA = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant secondA = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 1, "A", "TAA");
        GenomicVariant second = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "A", "TAA");
        GenomicVariant third = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant thirdA = GenomicVariant.of(chr2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 1000);
        GenomicVariant fourth = GenomicVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant fifth = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1000, "A", "<DEL>", -999);

        List<GenomicVariant> variants = Stream.of(second, fourth, secondA, firstA, third, firstB, fifth, first, thirdA)
                .parallel().unordered()
                .sorted(GenomicVariant.naturalOrder())
                .toList();
        assertThat(variants, equalTo(List.of(first, firstB, firstA, secondA, second, third, thirdA, fourth, fifth)));
    }

    @Test
    void testBuilderMethods() {
        GenomicVariant sequenceVariantFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T").id("rs123456").build();
        GenomicVariant sequenceVariantFromStaticConstructor = GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T");
        assertEquals(sequenceVariantFromBuilder, sequenceVariantFromStaticConstructor);

        GenomicVariant symbolicFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200).id("rs123456").build();
        GenomicVariant symbolicFromStaticConstructor = GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200);
        assertEquals(symbolicFromBuilder, symbolicFromStaticConstructor);

        GenomicVariant bndFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0).id("bnd_U").mateId("bnd_V").eventId("tra2").build();
        GenomicVariant bndFromStaticConstructor = GenomicVariant.of(chr1, "bnd_U", Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0, "bnd_V", "tra2");
        assertEquals(bndFromBuilder, bndFromStaticConstructor);
    }

    @Test
    void testConstructorsWithoutId() {
        GenomicVariant sequenceVariantFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T").build();
        GenomicVariant sequenceVariantFromStaticConstructor = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T");
        assertEquals(sequenceVariantFromBuilder, sequenceVariantFromStaticConstructor);
        assertEquals("", sequenceVariantFromBuilder.eventId());
        assertEquals("", sequenceVariantFromBuilder.mateId());

        GenomicVariant symbolicFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200).build();
        GenomicVariant symbolicFromStaticConstructor = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200);
        assertEquals(symbolicFromBuilder, symbolicFromStaticConstructor);
        assertEquals("", symbolicFromBuilder.eventId());
        assertEquals("", symbolicFromBuilder.mateId());

        GenomicVariant bndFromBuilder = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0).mateId("bnd_V").id("bnd_U").eventId("tra2").build();
        GenomicVariant bndFromStaticConstructor = GenomicVariant.of(chr1, "bnd_U", Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0, "bnd_V", "tra2");
        assertEquals(bndFromBuilder, bndFromStaticConstructor);
        assertEquals("bnd_V", bndFromStaticConstructor.mateId());
        assertEquals("tra2", bndFromStaticConstructor.eventId());
    }

    @Test
    void testChangeLengthInBuilderCannotBeIncorrectlySet() {
        Exception exception = assertThrows(IllegalArgumentException.class , () -> GenomicVariant.builder()
                .variant(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T")
                .changeLength(200)
                .build());
        assertThat(exception.getMessage(), equalTo("Given changeLength of 200 inconsistent with expected changeLength of -1 for variant 1:12345-12346 CA>T"));
    }

    @Test
    void idIsNotConsideredForEquality() {
        GenomicVariant instance = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").id("rs1234567").build();
        GenomicVariant other = GenomicVariant.builder().variant(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").build();
        assertThat(instance.id(), equalTo("rs1234567"));
        assertThat(other.id(), equalTo(""));
        assertThat(instance, equalTo(other));
    }

    @Disabled("telomeric")
    @Test
    void telomericVariantsOutOfContigBoundsTest() {
        // chrUn_KI270435v1	92984	667974	N	CGGAGTGGGGTGGAATGGAATCAACGCGAGTGCAGGGGAATGGAA	.	PASS	SVMETHOD=DYSGUv1.8.3;SVTYPE=INS;END=92984;CHR2=chrUn_KI270435v1;GRP=667974;NGRP=1;CT=3to5;CIPOS95=0;CIEND95=0;SVLEN=45;KIND=extra-regional;GC=46.85;NEXP=10;STRIDE=5;EXPSEQ=ggaatggaaT;RPOLY=90;OL=0;SU=54;WR=0;PE=0;SR=0;SC=54;BND=10;LPREC=0;RT=pe
        Contig chrUnKI270435v1 = GenomicAssemblies.GRCh38p13().contigByName("chrUn_KI270435v1");
        System.out.println(chrUnKI270435v1);
        GenomicVariant instance = GenomicVariant.of(chrUnKI270435v1, Strand.POSITIVE, Coordinates.oneBased(92984, 92984),"N", "CGGAGTGGGGTGGAATGGAATCAACGCGAGTGCAGGGGAATGGAA", 44);
        System.out.println(instance);
    }
}