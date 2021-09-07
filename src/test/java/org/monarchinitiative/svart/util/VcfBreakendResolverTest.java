package org.monarchinitiative.svart.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class VcfBreakendResolverTest {

    private final GenomicAssembly assembly = testAssembly(TestContig.of(1, 5), TestContig.of(2, 10));

    private GenomicAssembly testAssembly(Contig... contigs) {
        return GenomicAssembly.of("TestAssembly", "Wookie", "9607", "Han", "3021-01-15", "GB1", "RS1", Arrays.asList(contigs));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "CT,  CT[2:7[,    Invalid breakend! Ref allele 'CT' must be single base",
            "C,  C(2:7[,      Invalid breakend alt record C(2:7[",
            "C,  C[2:A[,      Invalid breakend alt record C[2:A[",
            "C,  C[X:7[,      Unknown mate contig `X`",
            "C,  C[2:7[G,     Sequence present both at the beginning (`C`) and the end (`G`) of alt field",
            "C,  G[2:7[,      Invalid breakend alt `G[2:7[`. No matching ref allele `C` at beginning or end of alt sequence",
            "C,  [2:7[G,      Invalid breakend alt `[2:7[G`. No matching ref allele `C` at beginning or end of alt sequence",
            "C,  C[2:7],      Invalid bracket orientation in `C[2:7]`",
    })
    public void invalidInput(String ref, String alt, String message) {
        VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> instance.resolve("tra2", "bnd_U", "bnd_V", assembly.contigById(1), 3, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt));
        assertThat(e.getMessage(), equalTo(message));
    }

    @Nested
    public class AlleleSequenceTests {

        @ParameterizedTest
        @CsvSource({
                "bndA, bndB, chr1, 3, C, CAGTNNNNNCA[chr2:6[,    chr1, POSITIVE, 3, 3, C, AGTNNNNNCA",
                "bndA, bndB, chr1, 3, C, CAGTNNNNNCA]chr2:6],    chr1, POSITIVE, 3, 3, C, AGTNNNNNCA",
                "bndA, bndB, chr1, 3, C, ]chr2:6]AGTNNNNNCAC,    chr1, NEGATIVE, 2, 2, G, TGNNNNNACT",
                "bndA, bndB, chr1, 3, C, [chr2:6[AGTNNNNNCAC,    chr1, NEGATIVE, 2, 2, G, TGNNNNNACT",
                "bndA, '', chr1, 3, C, C.,                       chr1, POSITIVE, 3, 3, C, .",
                "bndA, '', chr1, 3, C, .C,                       chr1, NEGATIVE, 2, 2, G, .",
        })
        public void parseAlleles(String id, String mateId, String leftChr, int leftPos, String ref, String alt,
                                 String leftContig, Strand leftStrand, int leftStart, int leftEnd,
                                 String exptRef, String exptAlt) {

            GenomicAssembly assembly = testAssembly(TestContig.of(1, 5), TestContig.of(2, 10));

            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
            BreakendVariant variant = instance.resolve("event1", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
            assertThat(variant.eventId(), equalTo("event1"));
            assertThat(variant.mateId(), equalTo(mateId));
            assertThat(variant.id(), equalTo(id));
            assertThat(variant.contig(), equalTo(assembly.contigByName(leftContig)));
            assertThat(variant.strand(), equalTo(leftStrand));
            assertThat(variant.coordinateSystem(), equalTo(CoordinateSystem.FULLY_CLOSED));
            assertThat(variant.start(), equalTo(leftStart));
            assertThat(variant.end(), equalTo(leftEnd));
            assertThat(variant.ref(), equalTo(exptRef));
            assertThat(variant.alt(), equalTo(exptAlt));
        }
    }

    @Nested
    public class CoordinateTests {

        @ParameterizedTest
        @CsvSource({
                // id, mateId, leftChr, leftPos, ref, alt,  expected left coordinates
                "bndA, bndB, chr1, 3, C, C[chr2:6[,       chr1, POSITIVE, 4, 3",
                "bndA, bndB, chr1, 3, C, C]chr2:6],       chr1, POSITIVE, 4, 3",
                "bndA, bndB, chr1, 3, C, ]chr2:6]C,       chr1, NEGATIVE, 3, 2",
                "bndA, bndB, chr1, 3, C, [chr2:6[C,       chr1, NEGATIVE, 3, 2",
                "bndA, bndB, chr1, 3, C, C.,              chr1, POSITIVE, 4, 3",
                "bndA, bndB, chr1, 3, C, .C,              chr1, NEGATIVE, 3, 2",
        })
        public void leftBreakend(String id, String mateId, String leftChr, int leftPos, String ref, String alt, String leftContig, Strand leftStrand, int leftStart, int leftEnd) {

            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);

            // bndA         |
            // ctg1 +  1 2 3 4 5
            // ctg1 +  A G C T T
            // ctg1 -  T C G A A
            // ctg1 -  5 4 3 2 1

            // bndB             |
            // ctg2 +  1 2 3 4 5 6 7 8 9 10
            // ctg2 +  T G A C C T C G T G
            // ctg2 -  A C T G G A G C A C
            // ctg2 - 10 9 8 7 6 5 4 3 2 1

            // tra2:ctg1:+   1 2 3
            // tra2:ctg1:+   A G C
            // tra2:ctg2:+         T C G T G
            // tra2:ctg2:+         6 7 8 9 10

            // ctg1	3	bndA	C	C[ctg2:5[	.	PASS	SVTYPE=BND;MATEID=bndB;EVENT=tra2
            BreakendVariant variant = instance.resolve("", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
            Breakend left = variant.left();
            assertThat(left.id(), equalTo("bndA"));
            assertThat(left.contig(), equalTo(assembly.contigByName(leftContig)));
            assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.FULLY_CLOSED));
            assertThat(left.start(), equalTo(leftStart));
            assertThat(left.end(), equalTo(leftEnd));
            assertThat(left.strand(), equalTo(leftStrand));
        }


        @ParameterizedTest
        @CsvSource({
                // id, mateId, leftChr, leftPos, ref, alt,  expected right coordinates
                "bndA, bndB, chr1, 3, C, C[chr2:8[,       chr2, POSITIVE, 8, 7",
                "bndA, bndB, chr1, 3, C, C]chr2:8],       chr2, NEGATIVE, 4, 3",
                "bndA, bndB, chr1, 3, C, ]chr2:8]C,       chr2, NEGATIVE, 4, 3",
                "bndA, bndB, chr1, 3, C, [chr2:8[C,       chr2, POSITIVE, 8, 7",
                "bndA, '', chr1, 3, C, C.,                chr0, POSITIVE, 1, 0",
                "bndA, '', chr1, 3, C, .C,                chr0, POSITIVE, 1, 0",
        })
        public void rightBreakend(String id, String mateId, String leftChr, int leftPos, String ref, String alt, String rightContig, Strand rightStrand, int rightStart, int rightEnd) {

            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);

            // bndA         |
            // ctg1 +  1 2 3 4 5
            // ctg1 +  A G C T T
            // ctg1 -  T C G A A
            // ctg1 -  5 4 3 2 1

            // bndB                 |
            // ctg2 +  1 2 3 4 5 6 7 8 9 10
            // ctg2 +  T G A C C T C G T G
            // ctg2 -  A C T G G A G C A C
            // ctg2 - 10 9 8 7 6 5 4 3 2 1

            // tra2:ctg1:+   1 2 3
            // tra2:ctg1:+   A G C
            // tra2:ctg2:+         T C G T G
            // tra2:ctg2:+         6 7 8 9 10

            // ctg1	3	bndA	C	C[ctg2:5[	.	PASS	SVTYPE=BND;MATEID=bndB;EVENT=tra2
            BreakendVariant variant = instance.resolve("", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);

            Breakend right = variant.right();
            assertThat(right.id(), equalTo(mateId));
            assertThat(right.contig(), equalTo(assembly.contigByName(rightContig)));
            assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.FULLY_CLOSED));
            assertThat(right.start(), equalTo(rightStart));
            assertThat(right.end(), equalTo(rightEnd));
            assertThat(right.strand(), equalTo(rightStrand));
        }

    }
}