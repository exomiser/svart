package org.monarchinitiative.svart.variant;

import org.monarchinitiative.svart.*;

/**
 * This class used to be located in the impl module as the default implementation (hence it's name), however it is a mix
 * of symbolic and sequence variant and didn't allow for alternate implementations. Since version 2.0.0 there are now
 * records for the DefaultSequenceVariant, DefaultSymbolicVariant and a memory-efficient CompactSequenceVariant which
 * all implement {@link GenomicVariant}
 *
 * @deprecated This is retained in the test directory to illustrate how a class might extend the {@link BaseGenomicVariant}.
 */
@Deprecated
public final class DefaultGenomicVariant extends BaseGenomicVariant<DefaultGenomicVariant> implements Comparable<GenomicVariant> {

    private DefaultGenomicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        super(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    public DefaultGenomicVariant(BaseGenomicVariant.Builder<?> builder) {
        super(builder);
    }

    // symbolic variants
    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return of(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return of(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
    }

    // sequence variants
    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        requireLengthIfSymbolic(alt);
        int end = calculateEnd(start, coordinateSystem, ref, alt);
        int changeLength = calculateChangeLength(ref, alt);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        requireLengthIfSymbolic(alt);
        int changeLength = calculateChangeLength(ref, alt);
        return of(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength, String mateId, String eventId) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return of(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    public static DefaultGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    @Override
    protected DefaultGenomicVariant newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        return new DefaultGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
    }

    public static class Builder extends BaseGenomicVariant.Builder<Builder> {

        @Override
        public DefaultGenomicVariant build() {
            return new DefaultGenomicVariant(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
