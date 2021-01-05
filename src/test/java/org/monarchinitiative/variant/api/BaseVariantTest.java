package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.impl.DefaultVariant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.*;
import static org.monarchinitiative.variant.api.TestContigs.chr2;

public class BaseVariantTest {

    private final Contig chr1 = TestContigs.chr1;

//    @Test
//    public void name() {
//        //2       321682 .         T                <DEL>        6    PASS   SVTYPE=DEL;LEN=206;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62
//        Variant instance = DefaultVariant.oneBased(chr1, "", Position.of(321682, -56, 20), Position.of(321887, -10, 62), "T", "<DEL>", -205);
//        System.out.println(instance);
//
//        //1 212471179 esv3588749 T <CN0> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVLEN=200;SVTYPE=DEL;VT=SV GT 0|1
//        Variant esv3588749 = DefaultVariant.oneBased(chr1, "esv3588749", Position.of(212471179, -471, 0), Position.of(212472619, -0, 444), "T", "<CN0>", -200);
//        System.out.println(esv3588749);
//    }

    @Test
    public void buildPreciseInsertion() {
        Variant instance = TestVariant.builder().with(chr1, "rs1234567", Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(1), "A", "TAA").build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("rs1234567"));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.startPosition(), equalTo(Position.of(1)));
        assertThat(instance.endPosition(), equalTo(Position.of(1)));
        assertThat(instance.changeLength(), equalTo(2));
        assertThat(instance.ref(), equalTo("A"));
        assertThat(instance.alt(), equalTo("TAA"));
    }

    @Test
    public void buildWithVariant() {
        Variant oneBasedVariant = DefaultVariant.oneBased(chr1, 1, "A", "TAA");
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
        Variant oneBasedVariant = DefaultVariant.oneBased(chr1, 1, "A", "TAA");
        TestVariant.Builder instance = TestVariant.builder().with(oneBasedVariant);
        Variant oneBased = instance.build();
        assertThat(instance.asOneBased().build(), equalTo(oneBased));
        assertThat(instance.asZeroBased().build(), equalTo(oneBased.toZeroBased()));
        assertThat(instance.asZeroBased().onNegativeStrand().build(), equalTo(oneBased.toZeroBased().toNegativeStrand()));
        assertThat(instance.asZeroBased().onNegativeStrand().onPositiveStrand().asOneBased().build(), equalTo(oneBased));
    }

    @Test
    public void builderAddsMissingEndAndLength() {
        Variant instance = TestVariant.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(5), "GA", "T").build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo(""));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.startPosition(), equalTo(Position.of(5)));
        assertThat(instance.endPosition(), equalTo(Position.of(6)));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.changeLength(), equalTo(-1));
        assertThat(instance.ref(), equalTo("GA"));
        assertThat(instance.alt(), equalTo("T"));
    }

    @Test
    public void buildIllegalSymbolicInsertion() {
        assertThrows(IllegalArgumentException.class, () -> TestVariant.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(1), "A", "<INS>").build());
    }

    @Test
    public void buildThrowsIllegalArgumentWithBreakendAllele() {
        assertThrows(IllegalArgumentException.class, () -> TestVariant.builder().with(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(1), "A", "A[1:2]").build());
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
                .with(chr1, ".", Strand.POSITIVE, CoordinateSystem.ONE_BASED, startPosition, endPosition, ref, alt, changeLength)
                .build();
        assertThat(instance.contig(), equalTo(chr1));
        assertThat(instance.id(), equalTo("."));
        assertThat(instance.strand(), equalTo(Strand.POSITIVE));
        assertThat(instance.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(instance.startPosition(), equalTo(startPosition));
        assertThat(instance.endPosition(), equalTo(endPosition));
        assertThat(instance.changeLength(), equalTo(changeLength));
        assertThat(instance.ref(), equalTo(ref));
        assertThat(instance.alt(), equalTo(alt));
    }

    @Test
    public void naturalOrdering() {
        Variant first = DefaultVariant.oneBased(chr1, 1, "A", "TA");
        Variant firstA = DefaultVariant.oneBased(chr1, 1, "A", "TAA");
        Variant second = DefaultVariant.oneBased(chr1, 2, "A", "TAA");
        Variant third = DefaultVariant.oneBased(chr2, 1, "A", "TAA");
        Variant thirdA = DefaultVariant.oneBased(chr2,  "", 1, 1, "A", "<INS>", 1000);
        Variant fourth = DefaultVariant.of(chr2, "", Strand.NEGATIVE, CoordinateSystem.ONE_BASED, Position.of(1), "A", "TAA");
        Variant fifth = DefaultVariant.oneBased(chr2,  "", 1, 1000, "A", "<DEL>", -999);

        List<Variant> variants = Stream.of(second, fourth, firstA, third, fifth, first, thirdA)
                .parallel().unordered()
                .sorted(Variant.naturalOrder())
                .collect(Collectors.toList());
        assertThat(variants, equalTo(List.of(first, firstA, second, third, thirdA, fourth, fifth)));
    }
}