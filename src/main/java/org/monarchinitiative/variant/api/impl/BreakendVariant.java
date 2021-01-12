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

    private BreakendVariant(String eventId, Breakend left, Breakend right, String ref, String trailingRef, String alt) {
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
        return left;
    }

    @Override
    public Position endPosition() {
        return left;
    }

    /**
     * @return always {@link CoordinateSystem#ZERO_BASED}
     */
    @Override
    public CoordinateSystem coordinateSystem() {
        return CoordinateSystem.ZERO_BASED;
    }

    /**
     * No-op.
     * @param coordinateSystem ignored argument
     * @return this instance
     */
    @Override
    public BreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return this;
    }

    /**
     * @return length of the sequence inserted between breakends
     */
    @Override
    public int length() {
        return alt.length();
    }

    /**
     * @return length of the ref allele
     */
    @Override
    public int refLength() {
        return ref.length();
    }

    /**
     * @return length of the sequence inserted between breakends
     */
    @Override
    public int changeLength() {
        return alt.length();
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

    /**
     * @return strand of the left breakend
     */
    @Override
    public Strand strand() {
        return left.strand();
    }

    /**
     * This method returns the unchanged breakend variant, since <em>left</em> and <em>right</em> breakend might be
     * located on different strands and it may not be possible to convert the breakend variant to required
     * <code>strand</code>.
     * <p>
     * For instance, the VCF record
     * <pre>2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1</pre>
     * describes a breakend <em>bnd_W</em> that is located at <em>2:321,681</em> on {@link Strand#POSITIVE}, and
     * the mate breakend <em>bnd_Y</em> located at <em>17:83,058,460</em> on {@link Strand#NEGATIVE}.
     * <p>
     * This variant cannot be converted to {@link Strand#NEGATIVE}, the {@link #left()} breakend will always be on
     * {@link Strand#POSITIVE}.
     * <p>
     * Note: use {@link #toOppositeStrand()} method to flip the breakend variant to the opposite strand
     * @param other target strand
     * @return this variant with <em>no change</em>
     */
    @Override
    public BreakendVariant withStrand(Strand other) {
        return this;
    }

    @Override
    public BreakendVariant toOppositeStrand() {
        return new BreakendVariant(eventId, right.toOppositeStrand(), left.toOppositeStrand(),
                Seq.reverseComplement(trailingRef), Seq.reverseComplement(ref), Seq.reverseComplement(alt));
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
