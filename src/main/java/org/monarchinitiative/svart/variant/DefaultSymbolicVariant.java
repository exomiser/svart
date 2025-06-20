package org.monarchinitiative.svart.variant;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.coordinates.CoordinatesFormat;
import org.monarchinitiative.svart.sequence.NucleotideSeq;

import java.util.Objects;

public record DefaultSymbolicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, VariantType variantType, int changeLength, String mateId, String eventId) implements GenomicVariant, Comparable<GenomicVariant> {

    public DefaultSymbolicVariant {
        VariantType.requireSymbolic(alt);
        VariantType.validateType(ref, alt, variantType);
        if (variantType.baseType() == VariantType.DEL && changeLength >= 0) {
            throw new IllegalArgumentException("Illegal DEL changeLength:" + changeLength + ". Should be < 0 given coordinates " + changeCoordinates(contig, coordinates, ref, alt));
        }
        if (variantType.baseType() == VariantType.INS && changeLength <= 0) {
            throw new IllegalArgumentException("Illegal INS changeLength:" + changeLength + ". Should be > 0 given coordinates " + changeCoordinates(contig, coordinates, ref, alt));
        }
        if (variantType.baseType() == VariantType.DUP && changeLength <= 0) {
            throw new IllegalArgumentException("Illegal DUP!changeLength:" + changeLength + ". Should be > 0 given coordinates " + changeCoordinates(contig, coordinates, ref, alt));
        }
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
    }

    private String changeCoordinates(Contig contig, Coordinates coordinates, String ref, String alt) {
        return contig.id() + ":" + coordinates.start() + "-" + coordinates.end() + " " + (ref.isEmpty() ? "-" : ref) + ">" + (alt.isEmpty() ? "-" : alt);
    }

    public static DefaultSymbolicVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        return of(contig, "", strand, Coordinates.of(coordinateSystem, start, end), ref, alt, changeLength, "", "");
    }

    public static DefaultSymbolicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength) {
        return of(contig, id, strand, Coordinates.of(coordinateSystem, start, end), ref, alt, changeLength, "", "");
    }

    public static DefaultSymbolicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end, String ref, String alt, int changeLength, String mateId, String eventId) {
        return of(contig, id, strand, Coordinates.of(coordinateSystem, start, end), ref, alt, changeLength, mateId, eventId);
    }

    public static DefaultSymbolicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return of(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
    }

    public static DefaultSymbolicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        return new DefaultSymbolicVariant(contig, GenomicVariant.cacheId(id), strand, coordinates, GenomicVariant.validateRefAllele(ref), alt, VariantType.parseType(ref, alt), VariantType.isBreakend(alt) ? 0 : changeLength, mateId, eventId);
    }

    /**
     * @param other
     * @return
     */
    @Override
    public DefaultSymbolicVariant withStrand(Strand other) {
        if (strand() == other || isBreakend()) {
            return this;
        }
        String refRevComp = NucleotideSeq.reverseComplement(ref);
        return new DefaultSymbolicVariant(contig(), id, other, coordinates().invert(contig()), refRevComp, alt, variantType, changeLength, mateId, eventId);
    }

    /**
     * @param coordinateSystem
     * @return
     */
    @Override
    public DefaultSymbolicVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return this;
        }
        return new DefaultSymbolicVariant(contig(), id, strand(), coordinates().withCoordinateSystem(coordinateSystem), ref, alt, variantType, changeLength, mateId, eventId);
    }

    /**
     * @return 
     */
    @Override
    public boolean isSymbolic() {
        return true;
    }

    @Override
    public boolean isBreakend() {
        return variantType == VariantType.BND || variantType == VariantType.TRA;
    }

    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultSymbolicVariant that = (DefaultSymbolicVariant) o;
        return changeLength == that.changeLength && Objects.equals(contig, that.contig) && strand == that.strand && Objects.equals(coordinates, that.coordinates) && Objects.equals(ref, that.ref) && Objects.equals(alt, that.alt) && Objects.equals(mateId, that.mateId) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    @Override
    public String toString() {
        return "DefaultSymbolicVariant{" +
               "contig=" + contigId() +
               ", id='" + id + '\'' +
               ", strand=" + strand +
               ", " + CoordinatesFormat.formatCoordinates(coordinates) +
               ", ref='" + ref + '\'' +
               ", alt='" + alt + '\'' +
               ", variantType=" + variantType() +
               ", length=" + length() +
               ", changeLength=" + changeLength +
               mateIdStr() +
               eventIdStr() +
               '}';
    }

    private String mateIdStr() {
        return mateId.isEmpty() ? "" : ", mateId=" + mateId;
    }

    private String eventIdStr() {
        return eventId.isEmpty() ? "" : ", eventId=" + eventId;
    }

}
