package org.monarchinitiative.svart;


import java.util.Objects;


/**
 *
 * @param <T>
 */
public abstract class AbstractGenomicVariant<T extends GenomicVariant> implements GenomicVariant {

    protected final GenomicVariant genomicVariant;

    protected AbstractGenomicVariant(GenomicVariant genomicVariant) {
        this.genomicVariant = genomicVariant;
    }

    protected AbstractGenomicVariant(Builder<?> builder) {
        this(builder.genomicVariant);
    }

    protected abstract T newVariantInstance(GenomicVariant genomicVariant);

    public GenomicVariant genomicVariant() {
        return genomicVariant;
    }

    /**
     * @return
     */
    @Override
    public Contig contig() {
        return genomicVariant.contig();
    }

    /**
     * @return
     */
    @Override
    public int contigId() {
        return genomicVariant.contigId();
    }

    /**
     * @return
     */
    @Override
    public String contigName() {
        return genomicVariant.contigName();
    }

    /**
     * @return
     */
    @Override
    public Strand strand() {
        return genomicVariant.strand();
    }

    /**
     * @return
     */
    @Override
    public Coordinates coordinates() {
        return genomicVariant.coordinates();
    }

    /**
     * @return 
     */
    @Override
    public T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    public T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return (T) this;
        }
        return newVariantInstance(genomicVariant.withCoordinateSystem(coordinateSystem));
    }

    /**
     * @return 
     */
    @Override
    public CoordinateSystem coordinateSystem() {
        return genomicVariant.coordinateSystem();
    }

    /**
     * @return 
     */
    @Override
    public int start() {
        return genomicVariant.start();
    }

    /**
     * @return 
     */
    @Override
    public int end() {
        return genomicVariant.end();
    }

    /**
     * @return 
     */
    @Override
    public int length() {
        return genomicVariant.length();
    }

    /**
     * @return 
     */
    @Override
    public boolean isPrecise() {
        return genomicVariant.isPrecise();
    }

    @Override
    public String id() {
        return genomicVariant.id();
    }

    @Override
    public String mateId() {
        return genomicVariant.mateId();
    }

    @Override
    public String eventId() {
        return genomicVariant.eventId();
    }

    @Override
    public int changeLength() {
        return genomicVariant.changeLength();
    }

    @Override
    public String ref() {
        return genomicVariant.ref();
    }

    @Override
    public String alt() {
        return genomicVariant.alt();
    }

    @Override
    public VariantType variantType() {
        return genomicVariant.variantType();
    }

    @Override
    public boolean isBreakend() {
        return genomicVariant.isBreakend();
    }


    /**
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public T toOppositeStrand() {
        return this.isBreakend() ? (T) this : withStrand(strand().opposite());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withStrand(Strand strand) {
        if (strand() == strand || isBreakend()) {
            return (T) this;
        }
        return newVariantInstance(genomicVariant.withStrand(strand));
    }

    /**
     * @return 
     */
    @Override
    public T toPositiveStrand() {
        return withStrand(Strand.POSITIVE);
    }

    /**
     * @return
     */
    @Override
    public T toNegativeStrand() {
        return withStrand(Strand.NEGATIVE);
    }

    /**
     * This method considers any instance to be equal to another if they are the same change on the same {@link Strand}
     * of the same {@link Contig}. Therefore, <b>this implementation will ignore the id</b> such that the variants:
     * <pre>
     *     #CHROM    POS ID  REF ALT
     *     1    20000   .   A   T
     *     1    20000   rs12345678   A   T
     * </pre>
     * will be considered equal. This method will not normalise variants to the same strand before checking equality.
     *
     * @param o the other instance to compare
     * @return true if the two {@link GenomicVariant} instances represent the same change on the same {@link Strand}
     * of the same {@link Contig}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractGenomicVariant<?> that = (AbstractGenomicVariant<?>) o;
        return Objects.equals(genomicVariant, that.genomicVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicVariant);
    }

    @Override
    public String toString() {
        return "GenomicVariant{" +
               "contig=" + contigId() +
               ", id='" + id() + '\'' +
               ", strand=" + strand() +
               ", " + CoordinatesFormat.formatCoordinates(coordinates()) +
               ", ref='" + ref() + '\'' +
               ", alt='" + alt() + '\'' +
               ", variantType=" + variantType() +
               ", length=" + length() +
               ", changeLength=" + changeLength() +
               mateIdStr() +
               eventIdStr() +
               '}';
    }

    private String mateIdStr() {
        String mateId = mateId();
        return mateId.isEmpty() ? "" : ", mateId=" + mateId;
    }

    private String eventIdStr() {
        String eventId = eventId();
        return eventId.isEmpty() ? "" : ", eventId=" + eventId;
    }

    public abstract static class Builder<T extends Builder<T>> {

        protected GenomicVariant genomicVariant;

        public T variant(GenomicVariant genomicVariant) {
            this.genomicVariant = Objects.requireNonNull(genomicVariant, "variant cannot be null");
            return self();
        }

        public T variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
            return variant(contig, "", strand, coordinates, ref, alt);
        }

        public T variant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
            this.genomicVariant = GenomicVariant.of(contig, id, strand, coordinates, ref, alt);
            return self();
        }

        public T variant(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
            return variant(contig, "", strand, coordinateSystem, start, ref, alt);
        }

        public T variant(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
            this.genomicVariant = GenomicVariant.of(contig, id, strand, coordinateSystem, start, ref, alt);
            return self();
        }

        public T variant(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
            return variant(contig, "", strand, coordinates, ref, alt, changeLength);
        }

        public T variant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
            return variant(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
        }

        public T variant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
            this.genomicVariant = GenomicVariant.of(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
            return self();
        }

        public T asZeroBased() {
            return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        }

        public T asOneBased() {
            return withCoordinateSystem(CoordinateSystem.ONE_BASED);
        }

        public T withCoordinateSystem(CoordinateSystem coordinateSystem) {
            this.genomicVariant = genomicVariant.withCoordinateSystem(coordinateSystem);
            return self();
        }

        public T onPositiveStrand() {
            return withStrand(Strand.POSITIVE);
        }

        public T onNegativeStrand() {
            return withStrand(Strand.NEGATIVE);
        }

        public T withStrand(Strand strand) {
            this.genomicVariant = genomicVariant.withStrand(strand);
            return self();
        }

        protected abstract GenomicVariant build();

        protected abstract T self();
    }

}
