package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.GenomicBreakend;
import org.monarchinitiative.svart.GenomicBreakendVariant;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

/**
 * Utility class for converting {@link GenomicBreakendVariant} back into their VCF representation.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class VcfBreakendFormatter {

    private VcfBreakendFormatter() {
        // static utility class
    }

    /**
     * Create VCF <em>pos</em> value for the <code>variant</code>.
     *
     * @param variant breakend variant
     * @return int POS value for use in VCF files
     */
    public static int makePosVcfField(GenomicBreakendVariant variant) {
        return variant.left().startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ONE_BASED) - 1;
    }

    /**
     * Create <em>ref</em> allele representation for the <code>variant</code>.
     *
     * @param variant breakend variant
     * @return string with breakend <em>ref</em> allele representation in VCF format
     */
    public static String makeRefVcfField(GenomicBreakendVariant variant) {
        return toPositiveStrand(variant.strand(), variant.ref());
    }

    /**
     * Create <em>alt</em> allele representation for the <code>variant</code>.
     *
     * @param variant breakend variant
     * @return string with breakend <em>alt</em> allele representation in VCF format
     */
    public static String makeAltVcfField(GenomicBreakendVariant variant) {
        String alt = toPositiveStrand(variant.strand(), variant.alt());
        String ref = toPositiveStrand(variant.strand(), variant.ref());
        return makeAltVcfField(variant.left(), variant.right(), ref, alt);
    }

    private static String toPositiveStrand(Strand strand, String allele) {
        return strand.isPositive() ? allele : Seq.reverseComplement(allele);
    }

    /**
     * Create <em>alt</em> allele representation.
     *
     * @param left  left breakend
     * @param right right breakend
     * @param ref   string with <em>ref</em> allele on {@link Strand#POSITIVE}
     * @param ins   string with <em>inserted</em> sequence on {@link Strand#POSITIVE}
     * @return string with breakend <em>alt</em> allele representation in VCF format
     */
    public static String makeAltVcfField(GenomicBreakend left, GenomicBreakend right, String ref, String ins) {
        if (right.isUnresolved()) {
            return left.strand() == Strand.POSITIVE ? ref + '.' : '.' + ref;
        }
        return left.strand() == Strand.POSITIVE ? ref + ins + mateString(right) : mateString(right) + ins + ref;
    }

    /**
     * Builds the mate breakend string e.g. '[6:12345[' for the POSITIVE strand or ']6:12345]' for the NEGATIVE strand
     */
    private static String mateString(GenomicBreakend right) {
        int pos = right.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.ONE_BASED);
        char mateEndStrand = right.strand() == Strand.POSITIVE ? '[' : ']';
        return mateEndStrand + right.contigName() + ':' + pos + mateEndStrand;
    }
}
