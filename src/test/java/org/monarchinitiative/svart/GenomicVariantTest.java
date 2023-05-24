package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.util.VariantTrimmer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenomicVariantTest {
    private static final Contig chr1 = GenomicAssemblies.GRCh38p13().contigById(1);

    @ParameterizedTest
    @CsvSource({
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
}