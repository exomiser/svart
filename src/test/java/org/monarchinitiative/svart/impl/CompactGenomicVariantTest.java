package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.Seq;
import org.openjdk.jol.info.ClassLayout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


class CompactGenomicVariantTest {
    private final TestContig contig = TestContig.of(1, 100000);

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED",
            "ZERO_BASED"
    })
    void coordinateSystem(CoordinateSystem coordinateSystem) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, 12345, 12345), "A", "T");
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, false",
            "ZERO_BASED, true"
    })
    void isZeroBased(CoordinateSystem coordinateSystem, boolean expected) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, 12345, 12345), "A", "T");
        assertThat(instance.isZeroBased(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED, true",
            "ZERO_BASED, false"
    })
    void isOneBased(CoordinateSystem coordinateSystem, boolean expected) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, 12345, 12345), "A", "T");
        assertThat(instance.isOneBased(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED",
            "ZERO_BASED"
    })
    void start(CoordinateSystem coordinateSystem) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, 12345, 12345), "A", "T");
        assertThat(instance.start(), equalTo(12345));
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
            "ZERO_BASED, 12344, 12346, TG, TAGC",
    })
    void end(CoordinateSystem coordinateSystem, int start, int end, String ref, String alt) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(coordinateSystem, start, end), ref, alt);
        assertThat(instance.end(), equalTo(end));
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.start(), equalTo(start));
        assertThat(instance.end(), equalTo(end));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
    }

    @Disabled("Just checking object sizes - not a test")
    @Test
    void objectSize() {
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345);
        CompactGenomicVariant compact = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, coordinates, "A", "T");
        ClassLayout compactClassLayout = ClassLayout.parseInstance(compact);
        System.out.println(compactClassLayout.toPrintable());
        GenomicVariant fat = GenomicVariant.of(contig, "", Strand.POSITIVE, coordinates, "A", "T");
        ClassLayout fatClassLayout = ClassLayout.parseInstance(fat);
        System.out.println(fatClassLayout.toPrintable());
        assertThat(compactClassLayout.instanceSize(), lessThan(fatClassLayout.instanceSize()));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE",
            "NEGATIVE"
    })
    void strand(Strand strand) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", strand, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345), "A", "T");
        assertThat(instance.strand(), equalTo(strand));
    }

    @Test
    void variantType() {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345), "A", "T");
        assertThat(instance.variantType(), equalTo(VariantType.SNV));
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
            "AGGTTCAC",
    })
    void ref(String ref) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, 12345, ref), ref, "T");
        assertThat(instance.ref(), equalTo(ref));
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
            "AGGTTCAC",
    })
    void alt(String alt) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, 12345, "A"), "A", alt);
        assertThat(instance.alt(), equalTo(alt));
    }

    @ParameterizedTest()
    @CsvSource({
            "ONE_BASED, 12345, ZERO_BASED, 12344",
            "ZERO_BASED, 12344, ONE_BASED, 12345,",
    })
    void withCoordinateSystem(CoordinateSystem coordinateSystem, int start, CoordinateSystem targetSystem, int expectedStart) {
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(coordinateSystem, start, "A"), "A", "T");
        CompactGenomicVariant expected = CompactGenomicVariant.of(contig, "", Strand.POSITIVE, Coordinates.ofAllele(targetSystem, expectedStart, "A"), "A", "T");
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
    })
    void withStrand(Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        Coordinates coordinates = Coordinates.ofAllele(coordinateSystem, start, ref);
        CompactGenomicVariant instance = CompactGenomicVariant.of(contig, "", strand, coordinates, ref, alt);
        System.out.println(instance);

        Strand targetStrand = strand.opposite();
        String refRevComp = Seq.reverseComplement(ref);
        String altRevComp = Seq.reverseComplement(alt);
        CompactGenomicVariant expected = CompactGenomicVariant.of(contig, "", targetStrand, coordinates.invert(contig), refRevComp, altRevComp);
        assertThat(instance.withStrand(targetStrand), equalTo(expected));
    }
}