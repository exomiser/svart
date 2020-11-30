package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Breakend;
import org.monarchinitiative.variant.api.CoordinateSystem;
import org.monarchinitiative.variant.api.Strand;

import java.util.Optional;

class Utils {

    private Utils() {
        // static utility class
    }

    /**
     * Create <em>alt</em> allele representation for <code>variant</code>.
     *
     * @param variant breakend variant
     * @return optional with breakend <em>alt</em> allele representation in VCF format or empty optional if either of
     * the breakends is not on positive or on negative strand
     */
    static Optional<String> makeAltVcfField(BreakendVariant variant) {
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
     * @return optional with breakend <em>alt</em> allele representation in VCF format or empty optional if either of
     * the breakends is not on positive or on negative strand
     */
    static Optional<String> makeAltVcfField(Breakend left, Breakend right, String ref, String ins) {
        String contig = right.contigName();
        int pos = right.withCoordinateSystem(CoordinateSystem.ONE_BASED).withStrand(Strand.POSITIVE).pos();
        Strand leftStrand = left.strand();
        Strand rightStrand = right.strand();

        if ((leftStrand == Strand.POSITIVE || leftStrand == Strand.NEGATIVE)
                && (rightStrand == Strand.POSITIVE || rightStrand == Strand.NEGATIVE)) {
            String mate = rightStrand == Strand.POSITIVE
                    ? '[' + contig + ':' + pos + '['
                    : ']' + contig + ':' + pos + ']';
            String full = leftStrand == Strand.POSITIVE
                    ? ref + ins + mate
                    : mate + ins + ref;

            return Optional.of(full);
        } else if (left.isUnresolved()) {
            return Optional.of('.' + ref);
        } else if (right.isUnresolved()) {
            return Optional.of(ref + '.');
        }
        return Optional.empty();
    }
}
