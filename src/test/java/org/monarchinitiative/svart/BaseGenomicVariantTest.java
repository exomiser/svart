package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.monarchinitiative.svart.TestContigs.chr1;
import static org.monarchinitiative.svart.TestContigs.chr2;

public class BaseGenomicVariantTest {

    @Test
    public void buildPreciseInsertion() {
        GenomicVariant instance = GenomicVariant.builder().with(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA").id("rs1234567").build();
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
    public void buildWithVariant() {
        GenomicVariant oneBasedVariant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant instance = GenomicVariant.builder().with(oneBasedVariant).build();
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
    public void buildWithVariantToCoordinateSystemAndStrand() {
        GenomicVariant oneBasedVariant = GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant.Builder instance = GenomicVariant.builder().with(oneBasedVariant);
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
    public void builderAddsMissingEndAndLength(CoordinateSystem coordinateSystem, int start, String ref, String alt, int expectEnd) {
        GenomicVariant instance = GenomicVariant.builder().with(chr1, Strand.POSITIVE, coordinateSystem, start, ref, alt).build();
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
    public void buildIllegalSymbolicInsertion() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().with(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "<INS>").build());
    }

    @Test
    public void buildThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().with(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "A[1:2]").build());
    }

    @Test
    void testSymbolicAllelesWithoutLengthThrowsInformativeException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder()
                .with(chr1, Strand.POSITIVE, Coordinates.oneBased(1, 200), "N", "<DEL>")
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
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(100, 101), "AT", "A", -2)

        );
        assertThat(exception.getMessage(), equalTo("Given changeLength of -2 inconsistent with expected changeLength of -1 for variant 1:100-101 AT>A"));
    }

    @Test
    public void buildSymbolicDeletion() {
        //2    321682 .    T    <DEL>   6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, 321_682, ConfidenceInterval.of(-56, 20), 321_887, ConfidenceInterval.of(-10, 62));
        String ref = "T";
        String alt = "<DEL>";
        int changeLength = -205;

        GenomicVariant instance = GenomicVariant.builder()
                .with(chr1, Strand.POSITIVE, coordinates, ref, alt, changeLength)
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
    public void naturalOrdering() {
        GenomicVariant first = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TA");
        GenomicVariant firstA = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant second = GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, "A", "TAA");
        GenomicVariant third = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant thirdA = GenomicVariant.of(chr2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1, "A", "<INS>", 1000);
        GenomicVariant fourth = GenomicVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 1, "A", "TAA");
        GenomicVariant fifth = GenomicVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1000, "A", "<DEL>", -999);

        List<GenomicVariant> variants = Stream.of(second, fourth, firstA, third, fifth, first, thirdA)
                .parallel().unordered()
                .sorted(GenomicVariant.naturalOrder())
                .collect(Collectors.toList());
        assertThat(variants, equalTo(List.of(first, firstA, second, third, thirdA, fourth, fifth)));
    }

    @Test
    void testBuilderMethods() {
        GenomicVariant sequenceVariantFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T").id("rs123456").build();
        GenomicVariant sequenceVariantFromStaticConstructor = GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T");
        assertEquals(sequenceVariantFromBuilder, sequenceVariantFromStaticConstructor);

        GenomicVariant symbolicFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200).id("rs123456").build();
        GenomicVariant symbolicFromStaticConstructor = GenomicVariant.of(chr1, "rs123456", Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200);
        assertEquals(symbolicFromBuilder, symbolicFromStaticConstructor);

        GenomicVariant bndFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0).id("bnd_U").mateId("bnd_V").eventId("tra2").build();
        GenomicVariant bndFromStaticConstructor = GenomicVariant.of(chr1, "bnd_U", Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0, "bnd_V", "tra2");
        assertEquals(bndFromBuilder, bndFromStaticConstructor);
    }

    @Test
    void testConstructorsWithoutId() {
        GenomicVariant sequenceVariantFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T").build();
        GenomicVariant sequenceVariantFromStaticConstructor = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12346), "CA", "T");
        assertEquals(sequenceVariantFromBuilder, sequenceVariantFromStaticConstructor);
        assertEquals("", sequenceVariantFromBuilder.eventId());
        assertEquals("", sequenceVariantFromBuilder.mateId());

        GenomicVariant symbolicFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200).build();
        GenomicVariant symbolicFromStaticConstructor = GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12544), "C", "<DEL>", -200);
        assertEquals(symbolicFromBuilder, symbolicFromStaticConstructor);
        assertEquals("", symbolicFromBuilder.eventId());
        assertEquals("", symbolicFromBuilder.mateId());

        GenomicVariant bndFromBuilder = GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0).mateId("bnd_V").id("bnd_U").eventId("tra2").build();
        GenomicVariant bndFromStaticConstructor = GenomicVariant.of(chr1, "bnd_U", Strand.POSITIVE, Coordinates.oneBased(12345, 12345), "C", "C[2:321682[", 0, "bnd_V", "tra2");
        assertEquals(bndFromBuilder, bndFromStaticConstructor);
        assertEquals("bnd_V", bndFromStaticConstructor.mateId());
        assertEquals("tra2", bndFromStaticConstructor.eventId());
    }
}