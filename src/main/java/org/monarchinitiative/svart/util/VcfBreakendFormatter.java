package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.Breakend;
import org.monarchinitiative.svart.BreakendVariant;
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
        String alt = variant.strand().isPositive() ? variant.alt() : Seq.reverseComplement(variant.alt());
        return makeAltVcfField(variant.left(), variant.right(), variant.ref(), alt);
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
        String contig = right.contigName();
        int pos = right.toOneBased().withStrand(Strand.POSITIVE).start();
        Strand leftStrand = left.strand();
        Strand rightStrand = right.strand();

        if (left.isUnresolved()) {
            return '.' + ref;
        } else if (right.isUnresolved()) {
            return ref + '.';
        }

        String mate = (rightStrand == Strand.POSITIVE)
                ? '[' + contig + ':' + pos + '['
                : ']' + contig + ':' + pos + ']';

        return leftStrand == Strand.POSITIVE
                ? ref + ins + mate
                : mate + ins + ref;
    }
}