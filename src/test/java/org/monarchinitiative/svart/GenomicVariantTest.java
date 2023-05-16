package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenomicVariantTest {
    private static final Contig chr1 = GenomicAssemblies.GRCh38p13().contigById(1);

    @ParameterizedTest
    @CsvSource ({
        "100, 100, A, T, 0",
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
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().with(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt).build());

            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt));
            assertThrows(IllegalArgumentException.class, () -> GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt).build());
        } else {
            assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt), equalTo(expected));
            assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt), equalTo(expected));
            assertThat(GenomicVariant.builder().with(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, ref, alt).build(), equalTo(expected));

            assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt), equalTo(expected));
            assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt), equalTo(expected));
            assertThat(GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt).build(), equalTo(expected));
        }

        assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength).build(), equalTo(expected));

        assertThat(GenomicVariant.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end, ref, alt, changeLength), equalTo(expected));
        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, start, end, ref, alt, changeLength), equalTo(expected));

        assertThat(GenomicVariant.of(chr1, "", Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength, "", ""), equalTo(expected));
        assertThat(GenomicVariant.builder().with(chr1, Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength)
                .id("")
                .mateId("")
                .eventId("")
                .build(), equalTo(expected));
    }

}