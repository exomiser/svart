package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.Breakend;
import org.monarchinitiative.svart.BreakendVariant;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class VcfBreakendFormatter {

    private VcfBreakendFormatter() {
        // static utility class
    }

    /**
     * Create <em>alt</em> allele representation for <code>variant</code>.
     *
     * @param variant breakend variant
     * @return string with breakend <em>alt</em> allele representation in VCF format
     */
    public static String makeAltVcfField(BreakendVariant variant) {
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
    public static String makeAltVcfField(Breakend left, Breakend right, String ref, String ins) {
        if (right.isUnresolved()) {
            return left.strand() == Strand.POSITIVE ? ref + '.' : '.' + ref;
        }
        return left.strand() == Strand.POSITIVE ? ref + ins + mateString(right) : mateString(right) + ins + ref;
    }

    /**
     * Builds the mate breakend string e.g. '[6:12345[' for the POSITIVE strand or ']6:12345]' for the NEGATIVE strand
     */
    private static String mateString(Breakend right) {
        int pos = right.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED);
        char mateEndStrand = right.strand() == Strand.POSITIVE ? '[' : ']';
        return mateEndStrand + right.contigName() + ':' + pos + mateEndStrand;
    }
}
