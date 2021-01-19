package org.monarchinitiative.variant.api.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.variant.api.util.VariantTrimmer.*;

class VariantTrimmerTest {

    private enum TrimDirection {
        LEFT, RIGHT
    }

    private enum BaseRetention {
        RETAIN, REMOVE
    }

    @Test
    public void symbolicVariantsAreNotTrimmed() {
        VariantPosition trimmed = leftShift(225_725_424, "C", "<INS>", retainingCommonBase());
        assertThat(trimmed, equalTo(VariantPosition.of(225_725_424, "C", "<INS>")));
    }

    @ParameterizedTest
    @CsvSource({
            "118887583, TCAAAA, TCAAAACAAAA, LEFT,  RETAIN, 118887583, T, TCAAAA",
            "118887583, TCAAAA, TCAAAACAAAA, LEFT,  REMOVE, 118887584, '', CAAAA",
            "118887583, TCAAAA, TCAAAACAAAA, RIGHT, RETAIN, 118887588, A, ACAAAA",
            "118887583, TCAAAA, TCAAAACAAAA, RIGHT, REMOVE, 118887589, '', CAAAA",
    })
    public void trim(int start, String ref, String alt, TrimDirection trimDirection, BaseRetention baseRetention, int expStart, String expRef, String expAlt) {

        BaseRetentionStrategy baseRetentionStrategy = baseRetention == BaseRetention.RETAIN ? retainingCommonBase() : removingCommonBase();

        if (trimDirection == TrimDirection.LEFT) {
            assertThat(leftShift(start, ref, alt, baseRetentionStrategy), equalTo(VariantPosition.of(expStart, expRef, expAlt)));
        } else {
            assertThat(rightShift(start, ref, alt, baseRetentionStrategy), equalTo(VariantPosition.of(expStart, expRef, expAlt)));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "118887583, TCAAAA, TCAAAACAAAA, LEFT,  RETAIN, 118887583, T, TCAAAA",
            // Taken from https://macarthurlab.org/2014/04/28/converting-genetic-variants-to-their-minimal-representation/
            "1000, AGTTC, AGCC, LEFT,  RETAIN, 1002, TT, C",
            "1001, CTCC, C, LEFT,  RETAIN, 1001, CTCC, C",
            "1001, CTCC, CCC, LEFT,  RETAIN, 1001, CT, C",
            "1001, CTCC, CCCC, LEFT,  RETAIN, 1002, T, C",
            "1001, CCT, C, LEFT,  RETAIN, 1001, CCT, C",
            "1001, CCT, CT, LEFT,  RETAIN, 1001 , CC, C",
            // Cases taken from https://github.com/ericminikel/minimal_representation/blob/master/test_minimal_representation.py
            "1001, CTCC, CCCC, LEFT,  RETAIN, 1002, T, C",
            "1001, CTCC, CCCC, RIGHT,  REMOVE, 1002, T, C",
            "1001, CTCC, CCC, LEFT,  RETAIN, 1001, CT, C",
            "1001, CTCC, CCC, LEFT,  REMOVE, 1002, T, ''",
            "1001, CTCC, CCC, RIGHT,  REMOVE, 1002, T, ''",
            "1001, CTCC, CTC, LEFT,  RETAIN, 1002, TC, T",
            "1001, CTAG, CTG, LEFT,  RETAIN, 1002, TA, T",
            "1001, CTCC, CTACC, LEFT,  RETAIN, 1002, T, TA",
            "1001, TCAGCAGCAG, TCAGCAG, LEFT,  RETAIN, 1001, TCAG, T",
            "1001, TCAGCAGCAG, TCAGCAG, LEFT,  REMOVE, 1002, CAG, ''",
            "1001, TCAGCAGCAG, TCAGCAG, RIGHT,  RETAIN, 1007, GCAG, G",
            "1001, TCAGCAGCAG, TCAGCAG, RIGHT,  REMOVE, 1008, CAG, ''",
            "1001, CTT, CTTT, LEFT,  RETAIN, 1001, C, CT",
            "1001, CTT, C, LEFT,  RETAIN, 1001, CTT, C",
            "1001, CTT, CT, LEFT,  RETAIN, 1001, CT, C",
            "1001, AAAATATATATAT, A, LEFT,  RETAIN, 1001, AAAATATATATAT, A",
            "1001, AAAATATATATAT, AATAT, LEFT,  RETAIN, 1001, AAAATATAT, A",
            "1001, ACACACACAC, AACAC, LEFT,  RETAIN, 1001, ACACAC, A",
            "1001, TGACGTAACGATT, TGACGTAACGGTT, LEFT,  RETAIN, 1011, A, G",
            "1001, TGACGTAACGATT, TGACGTAATAC, LEFT,  RETAIN, 1009, CGATT, TAC",
            // symbolic and breakends
            "1001, C, <INS>, LEFT,  RETAIN, 1001, C, <INS>",
            "1001, C, <INS>, LEFT,  REMOVE, 1002, '', <INS>",
            "1001, C, <INS>, RIGHT, REMOVE, 1002, '', <INS>",
            "1001, C, G]17:198982], LEFT,  RETAIN, 1001, C, G]17:198982]",
    })
    public void trimVcf(int start, String ref, String alt, TrimDirection trimDirection, BaseRetention baseRetention, int expStart, String expRef, String expAlt) {

        BaseRetentionStrategy baseRetentionStrategy = baseRetention == BaseRetention.RETAIN ? retainingCommonBase() : removingCommonBase();

        if (trimDirection == TrimDirection.LEFT) {
            assertThat(leftShift(start, ref, alt, baseRetentionStrategy), equalTo(VariantPosition.of(expStart, expRef, expAlt)));
        } else {
            assertThat(rightShift(start, ref, alt, baseRetentionStrategy), equalTo(VariantPosition.of(expStart, expRef, expAlt)));
        }
    }

    @Nested
    public class JannovarTests {

        @Test
        public void singleNucleotide() {
            VariantPosition trimmed = rightShift(100, "C", "T", removingCommonBase());
            assertThat(trimmed, equalTo(VariantPosition.of(100, "C", "T")));
        }

        @Test
        public void deletion() {
            VariantPosition trimmed = rightShift(100, "CGAT", "C", removingCommonBase());
            assertThat(trimmed, equalTo(VariantPosition.of(101, "GAT", "")));
        }

        @Test
        public void substitution() {
            VariantPosition trimmed = rightShift(100, "CCGA", "CGAT", removingCommonBase());
            assertThat(trimmed, equalTo(VariantPosition.of(101, "CGA", "GAT")));
        }

        @Test
        public void insertion() {
            VariantPosition trimmed = rightShift(100, "C", "CGAT", removingCommonBase());
            assertThat(trimmed, equalTo(VariantPosition.of(101, "", "GAT")));
        }

    }

    @Nested
    public class NirvanaTests {

        @Test
        public void singleNucleotide() {
            VariantPosition trimmed = trimNirvana(100, "C", "T");
            assertThat(trimmed, equalTo(VariantPosition.of(100, "C", "T")));
        }

        @Test
        public void deletion() {
            VariantPosition trimmed = trimNirvana(100, "CGAT", "C");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "GAT", "")));
        }

        @Test
        public void substitution() {
            VariantPosition trimmed = trimNirvana(100, "CCGA", "CGAT");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "CGA", "GAT")));
        }

        @Test
        public void insertion() {
            VariantPosition trimmed = trimNirvana(100, "C", "CGAT");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "", "GAT")));
        }

        @ParameterizedTest
        @CsvSource(
                nullValues = {"null"},
                value = {
                "100, A, C, 100, A, C",
                "100, A, A, 100, A, A",
                "100, AT, null, 100, AT, ''",
                "100, null, CG, 100, '', CG",
                "100, ATTT, AT, 102, TT, ''",
                "100, CGGG, TGGG, 100, C, T",
        }
        )
        public void trim(int start, String ref, String alt, int expectedStart, String expectedRef, String expectedAlt) {
            VariantPosition trimmed = trimNirvana(start, ref, alt);
            assertThat(trimmed, equalTo(VariantPosition.of(expectedStart, expectedRef, expectedAlt)));
        }

        @ParameterizedTest
        @CsvSource({
                "100, A, C, 100, A, C",
                "100, A, A, 100, A, A",
                "100, AT, '', 100, AT, ''",
                "100, '', CG, 100, '', CG",
                "100, ATTT, AT, 102, TT, ''",
                "100, CGGG, TGGG, 100, C, T",
        }
        )
        public void trimRightRemoveBase(int start, String ref, String alt, int expectedStart, String expectedRef, String expectedAlt) {
            VariantPosition trimmed = rightShift(start, ref, alt, removingCommonBase());
            assertThat(trimmed, equalTo(VariantPosition.of(expectedStart, expectedRef, expectedAlt)));
        }
    }

    @Nested
    public class JannovarCorrectTests {

        @Test
        public void singleNucleotide() {
            VariantPosition trimmed = correct(100, "C", "T");
            assertThat(trimmed, equalTo(VariantPosition.of(100, "C", "T")));
        }

        @Test
        public void deletion() {
            VariantPosition trimmed = correct(100, "CGAT", "C");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "GAT", "")));
        }

        @Test
        public void substitution() {
            VariantPosition trimmed = correct(100, "CCGA", "CGAT");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "CGA", "GAT")));
        }

        @Test
        public void insertion() {
            VariantPosition trimmed = correct(100, "C", "CGAT");
            assertThat(trimmed, equalTo(VariantPosition.of(101, "", "GAT")));
        }
    }

    // from de.charite.compbio.jannovar.reference.VariantDataCorrector - equivalent to rightShift().removeRedundantBase()
    private static VariantPosition correct(int start, String ref, String alt) {
        int trimStart = 0;
        // beginning
        while (trimStart < ref.length() && trimStart < alt.length() && ref.charAt(trimStart) == alt.charAt(trimStart)) {
            trimStart++;
        }
        if (trimStart == ref.length() && trimStart == alt.length() && trimStart > 0) {
            trimStart -= 1;
        }
        start += trimStart;
        ref = ref.substring(trimStart);
        alt = alt.substring(trimStart);

        // end
        int rightIdx = ref.length();
        int diff = ref.length() - alt.length();
        while (rightIdx > 0 && rightIdx - diff > 0 && ref.charAt(rightIdx - 1) == alt.charAt(rightIdx - 1 - diff)) {
            rightIdx--;
        }
        if (rightIdx == 0 && ref.length() > 0 && alt.length() > 0) {
            rightIdx += 1;
        }
        ref = rightIdx == 0 ? "" : ref.substring(0, rightIdx);
        alt = rightIdx - diff == 0 ? "" : alt.substring(0, rightIdx - diff);

        return VariantPosition.of(start, ref, alt);
    }

    /**
     * Nirvana style trimming:
     * https://github.com/Illumina/Nirvana/blob/master/VariantAnnotation/Algorithms/BiDirectionalTrimmer.cs
     * https://github.com/Illumina/Nirvana/blob/main/Variants/BiDirectionalTrimmer.cs
     * <p>
     * See also GATK 4:
     * https://github.com/broadinstitute/gatk/blob/master/src/main/java/org/broadinstitute/hellbender/utils/variant/GATKVariantContextUtils.java#L1033
     */
    private static VariantPosition trimNirvana(int pos, String ref, String alt) {
        if (ref == null) ref = "";
        if (alt == null) alt = "";

        // do not trim if ref and alt are same
        if (ref.equals(alt)) return VariantPosition.of(pos, ref, alt);

        // trimming at the start
        int i = 0;
        while (i < ref.length() && i < alt.length() && ref.charAt(i) == alt.charAt(i)) {
            i++;
        }
        if (i > 0) {
            pos += i;
            alt = alt.substring(i);
            ref = ref.substring(i);
        }

        int j = 0;
        while (j < ref.length() && j < alt.length() && ref.charAt(ref.length() - j - 1) == alt.charAt(alt.length() - j - 1)) {
            j++;
        }
        if (j > 0) {
            alt = alt.substring(0, alt.length() - j);
            ref = ref.substring(0, ref.length() - j);
        }

        return VariantPosition.of(pos, ref, alt);
    }

}