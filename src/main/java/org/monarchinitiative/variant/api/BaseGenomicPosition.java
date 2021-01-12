package org.monarchinitiative.variant.api;

import java.util.Objects;

public abstract class BaseGenomicPosition<T extends GenomicPosition> implements GenomicPosition {

    private final Contig contig;
    private final Strand strand;
    private final Position position;

    protected BaseGenomicPosition(Contig contig, Strand strand, Position position) {
        this.contig = Objects.requireNonNull(contig, "contig must not be null");
        this.strand = Objects.requireNonNull(strand, "strand must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");

        if ((position.minPos() < 0)) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + " that extends beyond first contig base");
        }
        if (position.maxPos() > contig.length()) {
            throw new IllegalArgumentException("Cannot create genomic position " + position + " that extends beyond contig end " + contig.length());
        }
    }

    protected BaseGenomicPosition(Builder<?> builder) {
        this(builder.contig, builder.strand, builder.position);
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Strand strand() {
        return strand;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withPos(int pos) {
        return pos() == pos ? (T) this : newPositionInstance(contig, strand, position.withPos(pos));
    }

    @Override
    public int pos() {
        return position.pos();
    }

    @Override
    public ConfidenceInterval confidenceInterval() {
        return position.confidenceInterval();
    }

    @Override
    public int minPos() {
        return position.minPos();
    }

    @Override
    public int maxPos() {
        return position.maxPos();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T asPrecise() {
        return isPrecise() ? (T) this : newPositionInstance(contig, strand, position.asPrecise());
    }

    @Override
    public T invert(Contig contig, CoordinateSystem coordinateSystem) {
        return newPositionInstance(contig, strand, position.invert(contig, coordinateSystem));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withStrand(Strand other) {
        if (this.strand == other)
            return (T) this;

        Position inverted = position.invert(contig, CoordinateSystem.ONE_BASED);
        return newPositionInstance(contig, other, inverted);
    }

    protected abstract T newPositionInstance(Contig contig, Strand strand, Position position);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseGenomicPosition<?> that = (BaseGenomicPosition<?>) o;
        return Objects.equals(contig, that.contig) && strand == that.strand && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, position);
    }

    @Override
    public String toString() {
        return "BaseGenomicPosition{" +
                "contig=" + contig +
                ", strand=" + strand +
                ", position=" + position +
                '}';
    }

    protected abstract static class Builder<T extends Builder<T>> {

        protected Contig contig;
        protected Strand strand = Strand.POSITIVE;
        protected Position position = Position.of(1);

        public T with(Contig contig, Strand strand, Position position) {
            this.contig = contig;
            this.strand = strand;
            this.position = position;
            return self();
        }

        public T withStrand(Strand strand) {
            if (this.strand == strand)
                return self();

            this.strand = strand;
            this.position = position.invert(contig, CoordinateSystem.ONE_BASED);
            return self();
        }

        public T onPositiveStrand() {
            return withStrand(Strand.POSITIVE);
        }

        public T onNegativeStrand() {
            return withStrand(Strand.NEGATIVE);
        }

        protected abstract GenomicPosition build();

        protected abstract T self();
    }

}
