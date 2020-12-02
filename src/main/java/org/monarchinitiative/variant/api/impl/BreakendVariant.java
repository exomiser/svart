package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

import java.util.Objects;

/**
 * Implementation of a structural variant that involves two different contigs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class BreakendVariant implements Variant, Breakended {

    private final String eventId;
    private final Breakend left;
    private final Breakend right;
    // Ref allele from the VCF record
    private final String ref;

    /**
     * When variant is converted to the opposite strand, then the reverse complemented ref allele is stored here.
     * We need to store the allele in order to reconstruct the original ref allele if variant is converted again to the
     * original strand.
     */
    private final String trailingRef;

    /**
     * String representing the inserted sequence without the <em>ref</em> allele, as described in Section 5.4.1 of the
     * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF v4.2 specification</a>
     */
    private final String alt;

    public BreakendVariant(String eventId, Breakend left, Breakend right, String ref, String alt) {
        this(eventId, left, right, ref, "", alt);
    }

    public BreakendVariant(String eventId, Breakend left, Breakend right, String ref, String trailingRef, String alt) {
        this.eventId = Objects.requireNonNull(eventId, "Event ID must not be null");
        this.left = Objects.requireNonNull(left, "Left breakend cannot be null");
        this.right = Objects.requireNonNull(right, "Right breakend cannot be null");
        this.ref = Objects.requireNonNull(ref, "Ref sequence cannot be null");
        this.trailingRef = Objects.requireNonNull(trailingRef, "Ref sequence cannot be null");
        this.alt = Objects.requireNonNull(alt, "Alt sequence cannot be null");
    }

    @Override
    public String id() {
        return left.id();
    }

    @Override
    public String mateId() {
        return right.id();
    }

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Breakend left() {
        return left;
    }

    @Override
    public Breakend right() {
        return right;
    }

    @Override
    public Contig contig() {
        return left.contig();
    }

    @Override
    public Position startPosition() {
        return left.position();
    }

    @Override
    public Position endPosition() {
        return left.position();
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return left.coordinateSystem();
    }

    @Override
    public BreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (left.coordinateSystem() == coordinateSystem) {
            return this;
        }
        return new BreakendVariant(eventId, left.withCoordinateSystem(coordinateSystem), right.withCoordinateSystem(coordinateSystem), ref, trailingRef, alt);
    }

    @Override
    public int length() {
        return alt.length();
    }

    @Override
    public int refLength() {
        return ref.length();
    }

    @Override
    public int changeLength() {
        return 0;
    }

    @Override
    public String ref() {
        return ref;
    }

    @Override
    public String alt() {
        return alt;
    }

    @Override
    public VariantType variantType() {
        return VariantType.BND;
    }

    @Override
    public Strand strand() {
        return left.strand();
    }

    public BreakendVariant withStrand(Strand strand) {
//        return this;
        //TODO: Neither of these work with the unit test BrakendVariantTest.withStrand
//        if (left.strand().notComplementOf(strand)) {
//            return this;
//        }
//        return new BreakendVariant(eventId, right.toOppositeStrand(), left.toOppositeStrand(), Seq.reverseComplement(trailingRef), Seq.reverseComplement(leadingRef), Seq.reverseComplement(alt));

        // we cannot convert if e.g. left==UNSTRANDED && target==POSITIVE
        if (left.strand().conversionIsLegal(strand) && right.strand().conversionIsLegal(strand)) {

            // we do nothing if left==POSITIVE && target==POSITIVE
            if (left.strand().needsConversion(strand)) {
                if (strand.hasComplement()) {
                    // Convert between POSITIVE and NEGATIVE.
                    return new BreakendVariant(eventId, right.toOppositeStrand(), left.toOppositeStrand(), // reverse order
                            Seq.reverseComplement(trailingRef), Seq.reverseComplement(ref), // here as well
                            Seq.reverseComplement(alt));
                } else {
                    // Convert into UNSTRANDED or UNKNOWN. Here, there is no need to use trailing ref anymore since
                    // the conversion to POSITIVE or NEGATIVE is not possible anymore.
                    return new BreakendVariant(eventId, left.withStrand(strand), right.withStrand(strand), ref, alt);
                }
            }
        }
        return this;
    }

    @Override
    public BreakendVariant toOppositeStrand() {
        return withStrand(strand().opposite());
//
////        Breakend flippedLeft = left.strand().hasComplement() ? left.toOppositeStrand() : left;
////        Breakend flippedRight = right.strand().hasComplement() ? right.toOppositeStrand() : right;
//
//
//
//        if (left.strand().hasComplement() || right.strand().hasComplement()) {
//                // Convert between POSITIVE and NEGATIVE.
//                return new BreakendVariant(eventId, right.toOppositeStrand(), left.toOppositeStrand(), // reverse order
//                        Seq.reverseComplement(trailingRef), Seq.reverseComplement(ref), // here as well
//                        Seq.reverseComplement(alt));
//        }
//        // Convert into UNSTRANDED or UNKNOWN. Here, there is no need to use trailing ref anymore since
//        // the conversion to POSITIVE or NEGATIVE is not possible anymore.
//        return new BreakendVariant(eventId, left.withStrand(strand), right.withStrand(strand), ref, alt);
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BreakendVariant)) return false;
        BreakendVariant that = (BreakendVariant) o;
        return eventId.equals(that.eventId) &&
                left.equals(that.left) &&
                right.equals(that.right) &&
                ref.equals(that.ref) &&
                trailingRef.equals(that.trailingRef) &&
                alt.equals(that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, left, right, ref, trailingRef, alt);
    }

    @Override
    public String toString() {
        return "BreakendVariant{" +
                "eventId='" + eventId + '\'' +
                ", left=" + left +
                ", right=" + right +
                ", alt='" + alt + '\'' +
                '}';
    }
}
