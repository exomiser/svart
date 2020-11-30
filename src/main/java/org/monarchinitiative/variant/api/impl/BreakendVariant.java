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

    /**
     * Ref allele from the VCF record
     */
    private final String leadingRef;

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

    public BreakendVariant(String eventId,
                           Breakend left,
                           Breakend right,
                           String ref,
                           String alt) {
        this(eventId, left, right, ref, "", alt);
    }

    private BreakendVariant(String eventId,
                            Breakend left,
                            Breakend right,
                            String leadingRef,
                            String trailingRef,
                            String alt) {
        this.eventId = Objects.requireNonNull(eventId, "Event ID must not be null");
        this.left = Objects.requireNonNull(left, "Left breakend cannot be null");
        this.right = Objects.requireNonNull(right, "Right breakend cannot be null");
        this.leadingRef = Objects.requireNonNull(leadingRef, "Ref sequence cannot be null");
        this.trailingRef = Objects.requireNonNull(trailingRef, "Ref sequence cannot be null");
        this.alt = Objects.requireNonNull(alt, "Alt sequence cannot be null");
    }

    /**
     * @return id of the breakend
     */
    @Override
    public String id() {
        return left.id();
    }

    /**
     * @return event id of the breakend variant
     */
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
        return new BreakendVariant(eventId, left.withCoordinateSystem(coordinateSystem), right.withCoordinateSystem(coordinateSystem), leadingRef, trailingRef, alt);
    }

    /**
     * @return strand of the left breakend
     */
    @Override
    public Strand strand() {
        return left.strand();
    }

    /**
     * Convert the breakend variant into <code>target</code> strand. The conversion is only performed if it is
     * <em>legal</em>. Otherwise, <code>this</code> is returned.
     * <p>
     * See {@link Stranded#withStrand(Strand)} to learn more about when a conversion is <em>legal</em>.
     *
     * @param target strand to convert the variant into
     * @return converted variant or <code>this</code>
     */
    public BreakendVariant withStrand(Strand target) {
        // we cannot convert if e.g. left==UNSTRANDED && target==POSITIVE
        if (left.strand().conversionIsLegal(target) && right.strand().conversionIsLegal(target)) {

            // we do nothing if left==POSITIVE && target==POSITIVE
            if (left.strand().needsConversion(target)) {
                if (target.hasComplement()) {
                    // Convert between POSITIVE and NEGATIVE.
                    return new BreakendVariant(eventId, right.toOppositeStrand(), left.toOppositeStrand(), // reverse order
                            Seq.reverseComplement(trailingRef), Seq.reverseComplement(leadingRef), // here as well
                            Seq.reverseComplement(alt));
                } else {
                    // Convert into UNSTRANDED or UNKNOWN. Here, there is no need to use trailing ref anymore since
                    // the conversion to POSITIVE or NEGATIVE is not possible anymore.
                    return new BreakendVariant(eventId, left.withStrand(target), right.withStrand(target), leadingRef, alt);
                }
            }
        }
        return this;
    }

    @Override
    public BreakendVariant toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    @Override
    public int length() {
        return alt.length();
    }

    @Override
    public int refLength() {
        return leadingRef.length();
    }

    /**
     * @return always <code>0</code>
     */
    @Override
    public int changeLength() {
        return 0;
    }

    /**
     * @return reference allele on {@link Strand#POSITIVE}
     */
    @Override
    public String ref() {
        return leadingRef;
    }

    /**
     * @return string with inserted sequence, as observed in <em>ALT</em> allele. The allele is trimmed to remove the
     * bases shared with the <em>REF</em> allele
     */
    @Override
    public String alt() {
        return alt;
    }

    /**
     * @return always {@link VariantType#BND}
     */
    @Override
    public VariantType variantType() {
        return VariantType.BND;
    }

    /**
     * @return always <code>true</code>
     */
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
                leadingRef.equals(that.leadingRef) &&
                trailingRef.equals(that.trailingRef) &&
                alt.equals(that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, left, right, leadingRef, trailingRef, alt);
    }

    @Override
    public String toString() {
        return "BV(" + eventId + ')' +
                "[" + left +
                ", " + right +
                "] " + alt;
    }
}
