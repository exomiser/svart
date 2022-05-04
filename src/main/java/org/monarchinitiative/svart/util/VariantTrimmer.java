package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.VariantType;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class VariantTrimmer {

    private static final BaseRetentionStrategy RETAINING = new RetainingCommonBase();
    private static final BaseRetentionStrategy REMOVING = new RemovingCommonBase();

    private static final String REF_STRING_CANNOT_BE_NULL = "REF string cannot be null";
    private static final String ALT_STRING_CANNOT_BE_NULL = "ALT string cannot be null";

    private VariantTrimmer() {
    }

    public abstract boolean canTrim(String ref, String alt);

    public abstract VariantPosition trim(Strand strand, int start, String ref, String alt);

    public static VariantTrimmer leftShiftingTrimmer(BaseRetentionStrategy baseRetentionStrategy) {
        return new LeftShiftingTrimmer(baseRetentionStrategy);
    }

    private static class LeftShiftingTrimmer extends VariantTrimmer {

        private final BaseRetentionStrategy baseRetentionStrategy;

        private LeftShiftingTrimmer(BaseRetentionStrategy baseRetentionStrategy) {
            this.baseRetentionStrategy = baseRetentionStrategy;
        }

        @Override
        public VariantPosition trim(Strand strand, int start, String ref, String alt) {
            return strand == Strand.POSITIVE ? leftShift(start, ref, alt, baseRetentionStrategy) : rightShift(start, ref, alt, baseRetentionStrategy);
        }

        @Override
        public boolean canTrim(String ref, String alt) {
            Objects.requireNonNull(ref, REF_STRING_CANNOT_BE_NULL);
            Objects.requireNonNull(alt, ALT_STRING_CANNOT_BE_NULL);
            return !baseRetentionStrategy.cantTrim(ref, alt) && (canRightTrim(ref, alt) || canLeftTrim(ref, alt));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LeftShiftingTrimmer that = (LeftShiftingTrimmer) o;
            return baseRetentionStrategy.equals(that.baseRetentionStrategy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(baseRetentionStrategy);
        }

        @Override
        public String toString() {
            return "LeftShiftingTrimmer{" +
                    "baseRetentionStrategy=" + baseRetentionStrategy +
                    '}';
        }
    }

    public static VariantTrimmer rightShiftingTrimmer(BaseRetentionStrategy baseRetentionStrategy) {
        return new RightShiftingTrimmer(baseRetentionStrategy);
    }

    private static class RightShiftingTrimmer extends VariantTrimmer {

        private final BaseRetentionStrategy baseRetentionStrategy;

        private RightShiftingTrimmer(BaseRetentionStrategy baseRetentionStrategy) {
            this.baseRetentionStrategy = baseRetentionStrategy;
        }

        @Override
        public VariantPosition trim(Strand strand, int start, String ref, String alt) {
            return strand == Strand.POSITIVE ? rightShift(start, ref, alt, baseRetentionStrategy) : leftShift(start, ref, alt, baseRetentionStrategy);
        }

        @Override
        public boolean canTrim(String ref, String alt) {
            Objects.requireNonNull(ref, REF_STRING_CANNOT_BE_NULL);
            Objects.requireNonNull(alt, ALT_STRING_CANNOT_BE_NULL);
            return !baseRetentionStrategy.cantTrim(ref, alt) && (canLeftTrim(ref, alt) || canRightTrim(ref, alt));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RightShiftingTrimmer that = (RightShiftingTrimmer) o;
            return baseRetentionStrategy.equals(that.baseRetentionStrategy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(baseRetentionStrategy);
        }

        @Override
        public String toString() {
            return "RightShiftingTrimmer{" +
                    "baseRetentionStrategy=" + baseRetentionStrategy +
                    '}';
        }
    }

    static VariantPosition leftShift(int start, String ref, String alt, BaseRetentionStrategy baseRetentionStrategy) {
        // copy these here in order not to change input params
        int trimStart = start;
        String trimRef = Objects.requireNonNull(ref, REF_STRING_CANNOT_BE_NULL);
        String trimAlt = Objects.requireNonNull(alt, ALT_STRING_CANNOT_BE_NULL);

        if (VariantType.isLargeSymbolic(alt)) {
            return baseRetentionStrategy.trimLargeSymbolic(start, ref, alt);
        }

        if (baseRetentionStrategy.cantTrim(ref, alt)) {
            return new VariantPosition(start, ref, alt);
        }

        // Can't do left alignment as have no reference seq and are assuming this has happened already.
        // Therefore check the sequence is first right trimmed, then left trimmed as per the wiki link above.

        // trim right side
        if (canRightTrim(trimRef, trimAlt)) {
            int rightIdx = baseRetentionStrategy.findRightIndex(ref, alt);
            if (rightIdx > 0) {
                trimRef = ref.substring(0, ref.length() - rightIdx);
                trimAlt = alt.substring(0, alt.length() - rightIdx);
            }
        }

        // trim left side
        if (canLeftTrim(trimRef, trimAlt)) {
            int leftIdx = baseRetentionStrategy.findleftIndex(trimRef, trimAlt);
            if (leftIdx > 0) {
                trimStart += leftIdx;
                trimRef = trimRef.substring(leftIdx);
                trimAlt = trimAlt.substring(leftIdx);
            }
        }

        return new VariantPosition(trimStart, trimRef, trimAlt);
    }

    static VariantPosition rightShift(int start, String ref, String alt, BaseRetentionStrategy baseRetentionStrategy) {
        // copy these here in order not to change input params
        int trimStart = start;
        String trimRef = Objects.requireNonNull(ref, REF_STRING_CANNOT_BE_NULL);
        String trimAlt = Objects.requireNonNull(alt, ALT_STRING_CANNOT_BE_NULL);

        if (VariantType.isLargeSymbolic(alt)) {
            return baseRetentionStrategy.trimLargeSymbolic(start, ref, alt);
        }

        if (baseRetentionStrategy.cantTrim(ref, alt)) {
            return new VariantPosition(start, ref, alt);
        }

        // trim left side
        if (canLeftTrim(trimRef, trimAlt)) {
            int leftIdx = baseRetentionStrategy.findleftIndex(trimRef, trimAlt);
            if (leftIdx > 0) {
                trimStart += leftIdx;
                trimRef = trimRef.substring(leftIdx);
                trimAlt = trimAlt.substring(leftIdx);
            }
        }

        // trim right side
        if (canRightTrim(trimRef, trimAlt)) {
            int rightIdx = baseRetentionStrategy.findRightIndex(trimRef, trimAlt);
            if (rightIdx > 0) {
                trimRef = trimRef.substring(0, trimRef.length() - rightIdx);
                trimAlt = trimAlt.substring(0, trimAlt.length() - rightIdx);
            }
        }

        return new VariantPosition(trimStart, trimRef, trimAlt);
    }

    private static boolean canRightTrim(String ref, String alt) {
        int refLength = ref.length();
        int altLength = alt.length();
        // alter 0 to 1 in order to change fully trimmed homomorphic sites from '' '' to 'A' 'A'
        return refLength > 0 && altLength > 0 && ref.charAt(refLength - 1) == alt.charAt(altLength - 1);
    }

    private static boolean canLeftTrim(String ref, String alt) {
        // alter 0 to 1 in order to change fully trimmed homomorphic sites from '' '' to 'A' 'A'
        return ref.length() > 0 && alt.length() > 0 && ref.charAt(0) == alt.charAt(0);
    }

    public static BaseRetentionStrategy retainingCommonBase() {
        return RETAINING;
    }

    public static BaseRetentionStrategy removingCommonBase() {
        return REMOVING;
    }

    public interface BaseRetentionStrategy {

        boolean cantTrim(String ref, String alt);

        int findleftIndex(String ref, String alt);

        int findRightIndex(String ref, String alt);

        VariantPosition trimLargeSymbolic(int start, String ref, String alt);

    }

    private static class RetainingCommonBase implements BaseRetentionStrategy {

        private RetainingCommonBase() {
        }

        public boolean cantTrim(String ref, String alt) {
            return (ref.length() == 1 || alt.length() == 1) || VariantType.isSymbolic(alt);
        }

        @Override
        public VariantPosition trimLargeSymbolic(int start, String ref, String alt) {
            return new VariantPosition(start, ref, alt);
        }

        @Override
        public int findleftIndex(String ref, String alt) {
            int leftIdx = 0;
            // scan from left to right
            while (leftIdx < ref.length() && leftIdx < alt.length() && ref.charAt(leftIdx) == alt.charAt(leftIdx)) {
                leftIdx++;
            }
            return leftIdx > 0 && leftIdx == ref.length() || leftIdx == alt.length() ? leftIdx - 1 : leftIdx;
        }

        @Override
        public int findRightIndex(String ref, String alt) {
            int rightIdx = 1;
            while (rightIdx < ref.length() && rightIdx < alt.length() && ref.charAt(ref.length() - rightIdx) == alt.charAt(alt.length() - rightIdx)) {
                rightIdx++;
            }
            return rightIdx - 1;
        }

        @Override
        public String toString() {
            return "RetainingCommonBase";
        }
    }

    private static class RemovingCommonBase implements BaseRetentionStrategy {

        private RemovingCommonBase() {
        }

        @Override
        public boolean cantTrim(String ref, String alt) {
            return (ref.length() == 0 || alt.length() == 0) || VariantType.isSymbolic(alt);
        }

        @Override
        public VariantPosition trimLargeSymbolic(int start, String ref, String alt) {
            return new VariantPosition(start + 1, "", alt);
        }

        @Override
        public int findleftIndex(String ref, String alt) {
            int leftIdx = 0;
            // scan from left to right
            while (leftIdx < ref.length() && leftIdx < alt.length() && ref.charAt(leftIdx) == alt.charAt(leftIdx)) {
                leftIdx++;
            }
            return leftIdx > 0 && leftIdx == ref.length() && leftIdx == alt.length() ? leftIdx - 1 : leftIdx;
        }

        @Override
        public int findRightIndex(String ref, String alt) {
            int rightIdx = 0;
            while (rightIdx < ref.length() && rightIdx < alt.length() && ref.charAt(ref.length() - rightIdx - 1) == alt.charAt(alt.length() - rightIdx - 1)) {
                rightIdx++;
            }
            return rightIdx;
        }

        @Override
        public String toString() {
            return "RemovingCommonBase";
        }
    }


    /**
     * Simple data class to hold start, ref and alt values for a genomic variant. Should become an inline type in the
     * future.
     *
     * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
     */
    public static class VariantPosition {

        private final int start;
        private final String ref;
        private final String alt;

        private VariantPosition(int start, String ref, String alt) {
            this.start = start;
            this.ref = ref;
            this.alt = alt;
        }

        /**
         * Returns a simple triple containing the input values.
         *
         * @param start start position of the ref allele
         * @param ref   reference allele sequence
         * @param alt   alternate allele sequence
         * @return an exact representation of the input coordinates and sequence.
         */
        public static VariantPosition of(int start, String ref, String alt) {
            Objects.requireNonNull(ref, REF_STRING_CANNOT_BE_NULL);
            Objects.requireNonNull(alt, ALT_STRING_CANNOT_BE_NULL);
            return new VariantPosition(start, ref, alt);
        }

        public int start() {
            return start;
        }

        public String ref() {
            return ref;
        }

        public String alt() {
            return alt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VariantPosition)) return false;
            VariantPosition that = (VariantPosition) o;
            return start == that.start &&
                    ref.equals(that.ref) &&
                    alt.equals(that.alt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, ref, alt);
        }

        @Override
        public String toString() {
            return "VariantPosition{" +
                    "start=" + start +
                    ", ref='" + ref + '\'' +
                    ", alt='" + alt + '\'' +
                    '}';
        }
    }
}
