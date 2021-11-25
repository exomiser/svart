package org.monarchinitiative.svart;

import java.util.Objects;

public abstract class BaseBreakendVariant<T extends BreakendVariant> extends BaseGenomicRegion<T> implements BreakendVariant {

    private final String eventId;
    private final Breakend left;
    private final Breakend right;
    // Ref allele from the VCF record
    private final String ref;
    /**
     * String representing the inserted sequence without the <em>ref</em> allele, as described in Section 5.4.1 of the
     * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF v4.2 specification</a>
     */
    private final String alt;

    protected BaseBreakendVariant(String eventId, Breakend left, Breakend right, String ref, String alt) {
        super(left.contig(), left.strand(), left.coordinates());
        this.eventId = Objects.requireNonNull(eventId, "Event ID must not be null");
        this.left = Objects.requireNonNull(left, "Left breakend cannot be null");
        this.right = Objects.requireNonNull(right, "Right breakend cannot be null");
        this.ref = Objects.requireNonNull(ref, "Ref sequence cannot be null");
        this.alt = Objects.requireNonNull(alt, "Alt sequence cannot be null");
        //check alt is either empty or contains [ATGC]+ / IUPAC bases in VcfBreakendResolver
        if (left.coordinateSystem() != right.coordinateSystem()) {
            throw new IllegalStateException("Breakend variant left and right breakends must have same coordinate system!");
        }
        if (left.isUnresolved()) {
            throw new IllegalArgumentException("Left breakend cannot be unresolved.");
        }
    }

    protected BaseBreakendVariant(Builder<?> builder) {
        this(builder.eventId, builder.left, builder.right, builder.ref, builder.alt);
    }

    protected abstract T newBreakendVariantInstance(String eventId, Breakend left, Breakend right, String ref, String alt);

    @Override
    protected T newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
        // no-op Not required as the newBreakendVariantInstance returns the same type and this is only required for
        // the BaseGenomicRegion.withCoordinateSystem and withStrand methods which are overridden in this class
        return null;
    }

    @Override
    public String id() {
        return left.id();
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
    public int start() {
        return left.start() - ref.length();
    }

    @Override
    public int end() {
        return left.end();
    }

    /**
     * @return the coordinateSystem of the Left side.
     */
    @Override
    public CoordinateSystem coordinateSystem() {
        return left.coordinateSystem();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (left.coordinateSystem() == coordinateSystem) {
            return (T) this;
        }
        Breakend leftAltered = left.withCoordinateSystem(coordinateSystem);
        Breakend rightAltered = right.withCoordinateSystem(coordinateSystem);
        return newBreakendVariantInstance(eventId, leftAltered, rightAltered, ref, alt);
    }

    @Override
    public int length() {
        return Coordinates.length(coordinateSystem(), start(), end());
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
    @SuppressWarnings("unchecked")
    public T withStrand(Strand other) {
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T toOppositeStrand() {
        return (T) this;
    }

    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean isBreakend() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseBreakendVariant<?> that = (BaseBreakendVariant<?>) o;
        return eventId.equals(that.eventId) &&
                left.equals(that.left) &&
                right.equals(that.right) &&
                ref.equals(that.ref) &&
                alt.equals(that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), eventId, left, right, ref, alt);
    }

    @Override
    public String toString() {
        return "BaseBreakendVariant{" +
                "eventId='" + eventId + '\'' +
                ", left=" + left +
                ", right=" + right +
                ", ref=" + ref +
                ", alt='" + alt + '\'' +
                '}';
    }

    public abstract static class Builder<T extends Builder<T>> extends BaseGenomicRegion.Builder<T> {

        protected Breakend left;
        protected Breakend right;
        protected String ref = "";
        protected String alt = "";
        protected String eventId = "";

        // n.b. this class does not offer the usual plethora of Builder options for each and every variable as they are
        // inherently linked to one-another and to allow this will more than likely ensure that objects are built in an
        // improper state. These methods are intended to allow subclasses to easily pass in the correct parameters so as
        // to maintain the correct state when finally built.

        public T with(BreakendVariant breakendVariant) {
            Objects.requireNonNull(breakendVariant, "breakendVariant cannot be null");
            return with(breakendVariant.eventId(), breakendVariant.left(), breakendVariant.right(), breakendVariant.ref(), breakendVariant.alt());
        }

        public T with(String eventId, Breakend left, Breakend right, String ref, String alt) {
            Objects.requireNonNull(left, "left breakend cannot be null");
            super.with(left);
            this.eventId = Objects.requireNonNull(eventId);
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
            this.ref = Objects.requireNonNull(ref);
            this.alt = Objects.requireNonNull(alt);
            return self();
        }

        /**
         * A no-op override. Breakend variants, with breakends possibly being on different strands are not able to
         * change their strand.
         * @param strand the target strand on which to place this variant
         * @return the same instance
         */
        @Override
        public T withStrand(Strand strand) {
            return self();
        }

        protected abstract BaseBreakendVariant<?> build();

        protected abstract T self();
    }
}
