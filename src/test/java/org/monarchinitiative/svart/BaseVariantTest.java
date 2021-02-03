package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.impl.DefaultVariant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.monarchinitiative.svart.TestContigs.chr1;
import static org.monarchinitiative.svart.TestContigs.chr2;

public class BaseVariantTest {

    @Test
    public void buildPreciseInsertion() {
        Variant instance = TestVariant.builder().with(chr1, "rs1234567", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA").build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("rs1234567"));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.FULLY_CLOSED));
        assertThat(instance.startPosition(), equalTo(Position.of(1)));
        assertThat(instance.endPosition(), equalTo(Position.of(1)));
        assertThat(instance.changeLength(), equalTo(2));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("TAA"));
    }

    @Test
    public void buildWithVariant() {
        Variant oneBasedVariant = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA");
        Variant instance = TestVariant.builder().with(oneBasedVariant).build();
        assertThat(instance.contig(), equalTo(oneBasedVariant.contig()));
        assertThat(instance.id(), equalTo(oneBasedVariant.id()));
        assertThat(instance.strand(), equalTo(oneBasedVariant.strand()));
        assertThat(instance.coordinateSystem(), equalTo(oneBasedVariant.coordinateSystem()));
        assertThat(instance.startPosition(), equalTo(oneBasedVariant.startPosition()));
        assertThat(instance.endPosition(), equalTo(oneBasedVariant.endPosition()));
        assertThat(instance.changeLength(), equalTo(oneBasedVariant.changeLength()));
        assertThat(instance.ref(), equalTo(oneBasedVariant.ref()));
        assertThat(instance.alt(), equalTo(oneBasedVariant.alt()));
    }

    @Test
    public void buildWithVariantToCoordinateSystemAndStrand() {
        Variant oneBasedVariant = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA");
        TestVariant.Builder instance = TestVariant.builder().with(oneBasedVariant);
        Variant oneBased = instance.build();
        assertThat(instance.asOneBased().build(), equalTo(oneBased));
        assertThat(instance.asZeroBased().build(), equalTo(oneBased.toZeroBased()));
        assertThat(instance.asZeroBased().onNegativeStrand().build(), equalTo(oneBased.toZeroBased().toNegativeStrand()));
        assertThat(instance.asZeroBased().onNegativeStrand().onPositiveStrand().asOneBased().build(), equalTo(oneBased));
    }

    @ParameterizedTest
    @CsvSource({
            // SNV/INV/MNV
            "FULLY_CLOSED, 5, G, T,    5",
            "LEFT_OPEN,    5, G, T,    6",
            "RIGHT_OPEN,   5, G, T,    6",
            "FULLY_OPEN,   5, G, T,    7",
            // INS
            "FULLY_CLOSED, 5, GA, T,    6",
            "LEFT_OPEN,    5, GA, T,    7",
            "RIGHT_OPEN,   5, GA, T,    7",
            "FULLY_OPEN,   5, GA, T,    8",
            // DEL
            "FULLY_CLOSED, 5, G, AT,    5",
            "LEFT_OPEN,    5, G, AT,    6",
            "RIGHT_OPEN,   5, G, AT,    6",
            "FULLY_OPEN,   5, G, AT,    7",
    })
    public void builderAddsMissingEndAndLength(CoordinateSystem coordinateSystem, int start, String ref, String alt, int expectEnd) {
        Variant instance = TestVariant.builder().with(chr1, "", Strand.POSITIVE, coordinateSystem, Position.of(start), ref, alt).build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo(""));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(coordinateSystem));
        assertThat(instance.startPosition(), equalTo(Position.of(start)));
        assertThat(instance.endPosition(), equalTo(Position.of(expectEnd)));
        assertThat(instance.length(), equalTo(ref.length()));
        assertThat(instance.changeLength(), equalTo(alt.length() - ref.length()));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
    }

    @Test
    public void buildIllegalSymbolicInsertion() {
        assertThrows(IllegalArgumentException.class, () -> TestVariant.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "<INS>").build());
    }

    @Test
    public void buildThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> TestVariant.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "A[1:2]").build());
    }

    @Test
    public void buildSymbolicDeletion() {
        //2    321682 .    T    <DEL>   6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
        Position startPosition = Position.of(321682, -56, 20);
        Position endPosition = Position.of(321887, -10, 62);
        String ref = "T";
        String alt = "<DEL>";
        int changeLength = -205;

        Variant instance = TestVariant.builder()
                .with(chr1, ".", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, startPosition, endPosition, ref, alt, changeLength)
                .build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("."));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.FULLY_CLOSED));
        assertThat(instance.startPosition(), equalTo(startPosition));
        assertThat(instance.endPosition(), equalTo(endPosition));
        assertThat(instance.changeLength(), equalTo(changeLength));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
    }

    @Test
    public void naturalOrdering() {
        Variant first = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TA");
        Variant firstA = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA");
        Variant second = DefaultVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(2), "A", "TAA");
        Variant third = DefaultVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA");
        Variant thirdA = DefaultVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(1), "A", "<INS>", 1000);
        Variant fourth = DefaultVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), "A", "TAA");
        Variant fifth = DefaultVariant.of(chr2, "", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(1000), "A", "<DEL>", -999);

        List<Variant> variants = Stream.of(second, fourth, firstA, third, fifth, first, thirdA)
                .parallel().unordered()
                .sorted(Variant.naturalOrder())
                .collect(Collectors.toList());
        assertThat(variants, equalTo(List.of(first, firstA, second, third, thirdA, fourth, fifth)));
    }
}