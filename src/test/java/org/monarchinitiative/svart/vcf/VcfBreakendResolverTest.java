package org.monarchinitiative.svart.vcf;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
class VcfBreakendResolverTest {

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
    void invalidInput(String ref, String alt, String message) {
        VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> instance.resolve("tra2", "bnd_U", "bnd_V", assembly.contigById(1), 3, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt));
        assertThat(e.getMessage(), equalTo(message));
    }

    @Nested
    class VcfSpecTests {

        private static final GenomicAssembly HG19 = GenomicAssemblies.GRCh37p13();
        private static final VcfBreakendResolver HG19_RESOLVER = new VcfBreakendResolver(HG19);
        // CASE REF  ALT     Meaning                                                    Example
        // 1    s    t[p[    piece extending to the right of p is joined after t        t|p+>
        // 2    s    t]p]    reverse comp piece extending left of p is joined after t   t|<-p
        // 3    s    ]p]t    piece extending to the left of p is joined before t        <+p|t
        // 4    s    [p[t    reverse comp piece extending right of p is joined before t p->|t

        // * #CHROM  POS     ID      REF   ALT            QUAL FILTER  INFO
        // * 2       321681  bnd_W   G     G]17:198982]   6    PASS    SVTYPE=BND;MATEID=bnd_Y;EVENTID=2  (CASE_2)
        // * 2       321682  bnd_V   T     ]13:123456]T   6    PASS    SVTYPE=BND;MATEID=bnd_U;EVENTID=1  (CASE_3)
        // * 13      123456  bnd_U   C     C[2:321682[    6    PASS    SVTYPE=BND;MATEID=bnd_V;EVENTID=1  (CASE_1)
        // * 13      123457  bnd_X   A     [17:198983[A   6    PASS    SVTYPE=BND;MATEID=bnd_Z;EVENTID=3  (CASE_4)
        // * 17      198982  bnd_Y   A     A]2:321681]    6    PASS    SVTYPE=BND;MATEID=bnd_W;EVENTID=2  (CASE_2)
        // * 17      198983  bnd_Z   C     [13:123457[C   6    PASS    SVTYPE=BND;MATEID=bnd_X;EVENTID=3  (CASE_2)

        /**
         * 5.4 Specifying complex rearrangements with breakends
         * <p>
         * An arbitrary rearrangement event can be summarized as a set of novel adjacencies. Each adjacency ties together
         * 2 breakends. The two breakends at either end of a novel adjacency are called mates.
         * <p>
         * There is one line of VCF (i.e. one record) for each of the two breakends in a novel adjacency. A breakend record is
         * identified with the tag "SVTYPE=BND" in the INFO field. The REF field of a breakend record indicates a base or
         * sequence s of bases beginning at position POS, as in all VCF records. The ALT field of a breakend record indicates
         * a replacement for s. This "breakend replacement" has three parts:
         * <ol>
         * <li>The string t that replaces places s. The string t may be an extended version of s if some novel bases are inserted
         * during the formation of the novel adjacency.</li>
         * <li>The position p of the mate breakend, indicated by a string of the form "chr:pos". This is the location of the
         * first mapped base in the piece being joined at this novel adjacency.</li>
         * <li>The direction that the joined sequence continues in, starting from p. This is indicated by the orientation of
         * square brackets surrounding p.</li>
         * </ol>
         * <p>
         * These 3 elements are combined in 4 possible ways to create the ALT. In each of the 4 cases, the assertion is that s
         * is replaced with t, and then some piece starting at position p is joined to t. The cases are:
         * <p>
         * <pre>
         * REF	ALT Meaning
         * s	t[p[	piece extending to the right of p is joined after t
         * s	t]p]	reverse comp piece extending left of p is joined after t
         * s	]p]t	piece extending to the left of p is joined before t
         * s	[p[t	reverse comp piece extending right of p is joined before t
         * </pre>
         * <p>
         * The example in Figure 1 shows a 3-break operation involving 6 breakends. It exemplifies all possible orientations
         * of breakends in adjacencies. Notice how the ALT field expresses the orientation of the breakends.
         * <p>
         * <pre>
         * #CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
         * 2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND
         * 2	321682	bnd_V	T	]13:123456]T	6	PASS	SVTYPE=BND
         * 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND
         * 13	123457	bnd_X	A	[17:198983[A	6	PASS	SVTYPE=BND
         * 17	198982	bnd_Y	A	A]2:321681]	6	PASS	SVTYPE=BND
         * 17	198983	bnd_Z	C	[13:123457[C	6	PASS	SVTYPE=BND
         * </pre>
         */
        @ParameterizedTest
        @CsvSource({
                // CHROM  POS     ID      REF   ALT MATEID EVENTID, LEFT_START, LEFT_END, RIGHT_CHROM, RIGHT_START, RIGHT_END, RIGHT_STRAND
                "2, 321681, bnd_W, G, G]17:198982], bnd_Y, 2, 321682, 321681, 17, 198983, 198982, NEGATIVE",
                "2, 321682, bnd_V, T, ]13:123456]T, bnd_U, 1, 321682, 321681, 13, 123457, 123456, POSITIVE",
                "13, 123456, bnd_U, C, C[2:321682[, bnd_V, 1, 123457, 123456, 2, 321682, 321681, POSITIVE",
                "13, 123457, bnd_X, A, [17:198983[A, bnd_Z, 3, 123457, 123456, 17, 198983, 198982, NEGATIVE",
                "17, 198982, bnd_Y, A, A]2:321681], bnd_W, 2, 198983, 198982, 2, 321682, 321681, NEGATIVE",
                "17, 198983, bnd_Z, C, [13:123457[C, bnd_X, 3, 198983, 198982, 13, 123457, 123456, NEGATIVE",
                // Inserted sequence
                "2, 321682, bnd_V, T, ]13:123456]AGTNNNNNCAT, bnd_U, ins, 321682, 321681, 13, 123457, 123456, POSITIVE",
                "13, 123456, bnd_U, C, CAGTNNNNNCA[2:321682[, bnd_V, ins, 123457, 123456, 2, 321682, 321681, POSITIVE",
        })
            // CASE REF  ALT     Meaning                                                    Example
            // 1    s    t[p[    piece extending to the right of p is joined after t        t|p+>
            // 2    s    t]p]    reverse comp piece extending left of p is joined after t   t|<-p
            // 3    s    ]p]t    piece extending to the left of p is joined before t        <+p|t
            // 4    s    [p[t    reverse comp piece extending right of p is joined before t p->|t
        void parseVcfSpec(String chrom, int pos, String id, String ref, String alt, String mateId, String eventId) {
            GenomicBreakendVariant variant = resolveBreakendVariant(chrom, pos, id, ref, alt, mateId, eventId);
            assertThat(variant.contigName(), equalTo(chrom));
            // pos, strand, ref and alt are debatable!
            assertThat(variant.left().id(), equalTo(id));
            assertThat(variant.right().id(), equalTo(mateId));
            assertThat(variant.eventId(), equalTo(eventId));
        }

        private GenomicBreakendVariant resolveBreakendVariant(String chrom, int pos, String id, String ref, String alt, String mateId, String eventId) {
            return HG19_RESOLVER.resolve(eventId, id, mateId, HG19.contigByName(chrom), pos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
        }

        /**
         * 5.4.1 Inserted Sequence
         * Sometimes, as shown in Figure 2, some bases are inserted between the two breakends, this information is also carried
         * in the ALT column:
         * <pre>
         * #CHROM   POS   ID    REF    ALT    QUAL   FILTER    INFO
         * 2 321682 bndV  T ]13:123456]AGTNNNNNCAT 6  PASS  SVTYPE=BND;MATEID=bnd U
         * 13 123456 bndU C CAGTNNNNNCA[2:321682[  6  PASS  SVTYPE=BND;MATEID=bnd V
         * </pre>
         */
        @ParameterizedTest
        @CsvSource({
                // CHR13-123456-C]AGTNNNNNCA[T-321682-CHR2

                // 3    s    ]p]t    piece extending to the left of p is joined before t    <+p|t
                //  2-321682-T ]13:123456]AGTNNNNNCAT
                "2, 321682, bnd_V, T, ']13:123456]AGTNNNNNCAT', bnd_U, ins, TGNNNNNACT",

                // 1    s    t[p[    piece extending to the right of p is joined after t        t|p+>
                // 13-1234546-C CAGTNNNNNCA[2:321682[
                "13, 123456, bnd_U, C, 'CAGTNNNNNCA[2:321682[', bnd_V, ins, AGTNNNNNCA"
        })
        void testBreakendInsertion(String contig, int position, String id, String ref, String alt, String mateId, String eventId, String expectedAlt) {
            GenomicBreakendVariant variant = resolveBreakendVariant(contig, position, id, ref, alt, mateId, eventId);
            assertThat(variant.alt(), equalTo(expectedAlt));
        }


        /**
         * 5.4.5 Telomeres
         * For a rearrangement involving the telomere end of a reference chromosome, we define a virtual telomeric breakend
         * that serves as a breakend partner for the breakend at the telomere. That way every breakend has a partner. If the
         * chromosome extends from position 1 to N, then the virtual telomeric breakends are at positions 0 and N+1. For
         * example, to describe the reciprocal translocation of the entire chromosome 1 into chromosome 13, as illustrated in
         * Figure 6:
         * <pre>
         *         0      1      chr1
         *         =     T======
         *          X   / Y
         *             /
         *   123456  /   123457  chr13
         *    ====C      A======
         *         U    V
         * </pre>
         * Figure 6: Telomeres
         * <p>
         * the records would look like:
         * <pre>
         * #CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
         * 1	0	bnd_X	N	.[13:123457[	6	PASS	SVTYPE=BND;MATEID=bnd_V
         * 1	1	bnd_Y	T	]13:123456]T	6	PASS	SVTYPE=BND;MATEID=bnd_U
         * 13	123456	bnd_U	C	C[1:1[	6	PASS	SVTYPE=BND;MATEID=bnd_Y
         * 13	123457	bnd_V	A	]1:0]A	6	PASS	SVTYPE=BND;MATEID=bnd_X
         * </pre>
         */
        @ParameterizedTest
        @CsvSource({
                "1, 0, bnd_X, N, .[13:123457[, bnd_V, TELOMERE",
                "1, 1, bnd_Y, T, ]13:123456]T, bnd_U, TELOMERE",
                "13, 123456, bnd_U, C, C[1:1[, bnd_Y, TELOMERE",
                "13, 123457, bnd_V, A, ]1:0]A, bnd_X, TELOMERE"
        })
        void testTelomereBreakend(String chrom, int pos, String id, String ref, String alt, String mateId, String eventId) {
            var breakendVariant = resolveBreakendVariant(chrom, pos, id, ref, alt, mateId, eventId);
            assertThat(breakendVariant.contigName(), equalTo(chrom));
            // pos, strand, ref and alt are debatable!
            assertThat(breakendVariant.left().id(), equalTo(id));
            assertThat(breakendVariant.mateId(), equalTo(mateId));
            assertThat(breakendVariant.eventId(), equalTo(eventId));
        }


        /**
         * 5.4.6 Event modifiers
         * As mentioned previously, a single rearrangement event can be described as a set of novel adjacencies. For example,
         * a reciprocal rearrangement such as in Figure 7
         * <p>
         * <pre>
         *      chr2    321681     1234567       chr13
         *      =============G     A===============
         *                    W - X
         *      chr13   123456     321682        chr2
         *      =============C     T===============
         *                    U - V
         * </pre>
         * Figure 7: Reciprocal rearrangement
         * <p>
         * would be described as:
         * <pre>
         * #CHROM   POS ID  REF ALT QUAL    FILTER  INFO
         * 2    321681  bndW    G   G[13:123457[    6   PASS    SVTYPE=BND;MATEID=bndX;EVENT=RR0
         * 2    321682  bndV    T   ]13:123456]T    6   PASS    SVTYPE=BND;MATEID=bndU;EVENT=RR0
         * 13   123456  bndU    C   C[2:321682[     6   PASS    SVTYPE=BND;MATEID=bndV;EVENT=RR0
         * 13   123457  bndX    A   ]2:321681]A     6   PASS    SVTYPE=BND;MATEID=bndW;EVENT=RR0
         * </pre>
         */
        @ParameterizedTest
        @CsvSource({
                // chrom, pos, id, ref, alt, mateId, eventId, expectedContig, expectedStrand
                "2, 321681, bndW, G, G[13:123457[, bndU, INV0, '2', POSITIVE", // 1 s t[p[ piece extending to the right of p is joined after t t|p+>
                "2, 321682, bndV, T, ]13:123456]T, bndX, INV0, '2', NEGATIVE", // 3 s ]p]t piece extending to the left of p is joined before t <+p|t
                "13, 123456, bndU, C, C[2:321682[, bndW, INV0, '13', POSITIVE", // 1 s t[p[ piece extending to the right of p is joined after t t|p+>
                "13, 123457, bndX, A, ]2:321681]A, bndV, INV0, '13', NEGATIVE"  // 3 s ]p]t piece extending to the left of p is joined before t <+p|t
        })
        void testReciprocalRearrangement(String chrom, int pos, String id, String ref, String alt, String mateId, String eventId, String expectedContig, Strand expectedStrand) {
            // 1    s    t[p[    piece extending to the right of p is joined after t        t|p+>
            // 3    s    ]p]t    piece extending to the left of p is joined before t        <+p|t

            //2    321681  bndW    G   G[13:123457[    6   PASS    SVTYPE=BND;MATEID=bndX;EVENT=RR0
            //     1               s    t[p[    piece extending to the right of p is joined after t        t|p+>

            //2    321682  bndV    T   ]13:123456]T    6   PASS    SVTYPE=BND;MATEID=bndU;EVENT=RR0
            //               3    s    ]p]t    piece extending to the left of p is joined before t        <+p|t

            //13   123456  bndU    C   C[2:321682[     6   PASS    SVTYPE=BND;MATEID=bndV;EVENT=RR0
            //     1               s    t[p[    piece extending to the right of p is joined after t        t|p+>

            //13   123457  bndX    A   ]2:321681]A     6   PASS    SVTYPE=BND;MATEID=bndW;EVENT=RR0
            //               3    s    ]p]t    piece extending to the left of p is joined before t        <+p|t

            var breakend = resolveBreakendVariant(chrom, pos, id, ref, alt, mateId, eventId);
            assertThat(breakend.contigName(), equalTo(expectedContig));
            assertThat(breakend.strand(), equalTo(expectedStrand));
            // pos, strand, ref and alt are debatable!
            assertThat(breakend.left().id(), equalTo(id));
            assertThat(breakend.mateId(), equalTo(mateId));
            assertThat(breakend.eventId(), equalTo(eventId));
        }


        /**
         * 5.4.7 Inversions
         * Similarly an inversion such as in Figure 8:
         * <pre>
         *      chr2    321681     421682         321682     421681       chr2
         *      =============G     T-------------------A     C===============
         *                    W - V       &lt;INV&gt;       U - X
         * </pre>
         * Figure 8: Inversion
         * <p>
         * can be described equivalently in two ways. Either one uses the short hand notation described previously (recom-
         * mended for simple cases):
         * <pre>
         * #CHROM   POS ID  REF ALT QUAL    FILTER  INFO
         * 2    321682  INV0    T  <INV>   6   PASS    SVTYPE=INV;END=421681
         * </pre>
         * or one describes the breakends:
         * <pre>
         * #CHROM POS ID REF ALT QUAL FILTER INFO
         * 2    321681  bnd_W   G   G]2:421681] 6   PASS    SVTYPE=BND;MATEID=bnd_U;EVENT=INV0
         * 2    321682  bnd_V   T   [2:421682[T 6   PASS    SVTYPE=BND;MATEID=bnd_X;EVENT=INV0
         * 2    421681  bnd_U   A   A]2:321681] 6   PASS    SVTYPE=BND;MATEID=bnd_W;EVENT=INV0
         * 2    421682  bnd_X   C   [2:321682[C 6   PASS    SVTYPE=BND;MATEID=bnd_V;EVENT=INV0
         * </pre>
         */
        @ParameterizedTest
        @CsvSource({
                // chrom, pos, id, ref, alt, mateId, eventId, expectedContig, expectedStrand, expectedPos
                "2, 321681, bndW, G, G]2:421681], bndU, INV0, '2', POSITIVE, 321681", // 2 s t]p] reverse comp piece extending left of p is joined after t t|<-p
                "2, 321682, bndV, T, [2:421682[T, bndX, INV0, '2', NEGATIVE, 242877691", // 4 s [p[t reverse comp piece extending right of p is joined before t p->|t
                "2, 421681, bndU, A, A]2:321681], bndW, INV0, '2', POSITIVE, 421681", // 2 s t]p] reverse comp piece extending left of p is joined after t t|<-p
                "2, 421682, bndX, C, [2:321682[C, bndV, INV0, '2', NEGATIVE, 242777691"  // 4 s [p[t reverse comp piece extending right of p is joined before t p->|t
        })
        void testBreakendInversion(String chrom, int pos, String id, String ref, String alt, String mateId, String eventId,
                                   String expectedContig, Strand expectedStrand, int expectedPos) {
            // 2    s    t]p]    reverse comp piece extending left of p is joined after t   t|<-p
            // 4    s    [p[t    reverse comp piece extending right of p is joined before t p->|t

            //#CHROM POS ID REF ALT QUAL FILTER INFO
            //2 321681 bndW G G]2:421681] 6 PASS SVTYPE=BND;MATEID=bndU;EVENT=INV0
            //     2        s t]p]    reverse comp piece extending left of p is joined after t   t|<-p

            //2 321682 bndV T [2:421682[T 6 PASS SVTYPE=BND;MATEID=bndX;EVENT=INV0
            //      4       s [p[t    reverse comp piece extending right of p is joined before t p->|t

            //2 421681 bndU A A]2:321681] 6 PASS SVTYPE=BND;MATEID=bndW;EVENT=INV0
            //     2        s t]p]    reverse comp piece extending left of p is joined after t   t|<-p

            //2 421682 bndX C [2:321682[C 6 PASS SVTYPE=BND;MATEID=bndV;EVENT=INV0
            //      4       s [p[t    reverse comp piece extending right of p is joined before t p->|t

            var breakend = resolveBreakendVariant(chrom, pos, id, ref, alt, mateId, eventId);

            // Basic assertions
            assertThat(breakend.contigName(), equalTo(expectedContig));
            assertThat(breakend.strand(), equalTo(expectedStrand));
            assertThat(breakend.start(), equalTo(expectedPos));

            // Breakend-specific assertions
            assertThat(breakend.isBreakend(), equalTo(true));
            assertThat(breakend.variantType(), equalTo(VariantType.BND));
            assertThat(breakend.eventId(), equalTo(eventId));
            assertThat(breakend.id(), equalTo(id));
//            assertThat(breakend.ref(), equalTo(ref));
            // pos, strand, ref and alt are debatable!

            // Verify mate relationships
            assertThat(breakend.left().id(), equalTo(id));
            assertThat(breakend.right().contigName(), equalTo(expectedContig));
        }
    }

    @Nested
    class AlleleSequenceTests {

        @ParameterizedTest
        @CsvSource({
                "bndA, bndB, chr1, 3, C, CAGTNNNNNCA[chr2:6[,    chr1, POSITIVE, 3, 3, C, AGTNNNNNCA",
                "bndA, bndB, chr1, 3, C, CAGTNNNNNCA]chr2:6],    chr1, POSITIVE, 3, 3, C, AGTNNNNNCA",
                "bndA, bndB, chr1, 3, C, ]chr2:6]AGTNNNNNCAC,    chr1, NEGATIVE, 2, 2, G, TGNNNNNACT",
                "bndA, bndB, chr1, 3, C, [chr2:6[AGTNNNNNCAC,    chr1, NEGATIVE, 2, 2, G, TGNNNNNACT",
                "bndA, '', chr1, 3, C, C.,                       chr1, POSITIVE, 3, 3, C, .",
                "bndA, '', chr1, 3, C, .C,                       chr1, NEGATIVE, 2, 2, G, .",
        })
        void parseAlleles(String id, String mateId, String leftChr, int leftPos, String ref, String alt,
                          String leftContig, Strand leftStrand, int leftStart, int leftEnd,
                          String exptRef, String exptAlt) {

            GenomicAssembly assembly = testAssembly(TestContig.of(1, 5), TestContig.of(2, 10));

            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
            GenomicBreakendVariant variant = instance.resolve("event1", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
            assertThat(variant.eventId(), equalTo("event1"));
            assertThat(variant.mateId(), equalTo(mateId));
            assertThat(variant.id(), equalTo(id));
            assertThat(variant.contig(), equalTo(assembly.contigByName(leftContig)));
            assertThat(variant.strand(), equalTo(leftStrand));
            assertThat(variant.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
            assertThat(variant.start(), equalTo(leftStart));
            assertThat(variant.end(), equalTo(leftEnd));
            assertThat(variant.ref(), equalTo(exptRef));
            assertThat(variant.alt(), equalTo(exptAlt));
        }
    }

    @Nested
    class CoordinateTests {

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
        void leftBreakend(String id, String mateId, String leftChr, int leftPos, String ref, String alt, String leftContig, Strand leftStrand, int leftStart, int leftEnd) {

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
            GenomicBreakendVariant variant = instance.resolve("", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
            GenomicBreakend left = variant.left();
            assertThat(left.id(), equalTo(id));
            assertThat(left.contig(), equalTo(assembly.contigByName(leftContig)));
            assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
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
        void rightBreakend(String id, String mateId, String leftChr, int leftPos, String ref, String alt, String rightContig, Strand rightStrand, int rightStart, int rightEnd) {

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
            GenomicBreakendVariant variant = instance.resolve("", id, mateId, assembly.contigByName(leftChr), leftPos, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);

            GenomicBreakend right = variant.right();
            assertThat(right.id(), equalTo(mateId));
            assertThat(right.contig(), equalTo(assembly.contigByName(rightContig)));
            assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
            assertThat(right.start(), equalTo(rightStart));
            assertThat(right.end(), equalTo(rightEnd));
            assertThat(right.strand(), equalTo(rightStrand));
        }

        @Test
        void resolveVariant() {
            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
            //"event1, bndA, bndB, chr1, 3, C, C[chr2:8[,       chr2, POSITIVE, 8, 7",
            Contig chr1 = assembly.contigByName("chr1");
            Contig chr2 = assembly.contigByName("chr2");
            GenomicVariant genomicVariant = GenomicVariant.of(chr1, "bndA", Strand.POSITIVE, Coordinates.oneBased(3, 3), "C", "C[chr2:8[", 0, "bndB", "event1");
            GenomicBreakendVariant resolvedFromVariant = instance.resolveBreakend(genomicVariant);
            GenomicBreakendVariant expected = instance.resolve("event1", "bndA", "bndB", chr1, 3, ConfidenceInterval.precise(), ConfidenceInterval.precise(), "C", "C[chr2:8[");
            assertThat(resolvedFromVariant, equalTo(expected));
            assertThat(resolvedFromVariant.left(), equalTo(GenomicBreakend.of(chr1, "bndA", Strand.POSITIVE, Coordinates.oneBased(4, 3))));
            assertThat(resolvedFromVariant.right(), equalTo(GenomicBreakend.of(chr2, "bndB", Strand.POSITIVE, Coordinates.oneBased(8, 7))));
        }

        @Test
        void resolveVariantWithoutConfidenceIntervals() {
            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
            //"event1, bndA, bndB, chr1, 3, C, C[chr2:8[,       chr2, POSITIVE, 8, 7",
            GenomicVariant genomicVariant = GenomicVariant.of(assembly.contigByName("chr1"), "bndA", Strand.POSITIVE, Coordinates.oneBased(3, 3), "C", "C[chr2:8[", 0, "bndB", "event1");
            GenomicBreakendVariant resolvedFromVariant = instance.resolveBreakend(genomicVariant);
            GenomicVariant expected = instance.resolve("event1", "bndA", "bndB", assembly.contigByName("chr1"), 3, "C", "C[chr2:8[");
            assertThat(resolvedFromVariant, equalTo(expected));
        }

        @Test
        void resolveVariantOnlyAcceptsBreakends() {
            VcfBreakendResolver instance = new VcfBreakendResolver(assembly);
            GenomicVariant snp = GenomicVariant.of(assembly.contigByName("chr1"), "rs123456", Strand.POSITIVE, Coordinates.oneBased(3, 3), "C", "A", 0, "", "");
            Exception exception = assertThrows(IllegalArgumentException.class, () -> instance.resolveBreakend(snp));
            assertThat(exception.getMessage(), equalTo("Unable to resolve non-breakend variant!"));
        }

    }
}