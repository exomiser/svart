package org.monarchinitiative.svart.variant;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.sequence.NucleotideSeq;

import java.util.Objects;

/**
 * Default implementation of a sequence variant. Sequence variants are defined as precise nucleotide sequence changes as
 * opposed to symbolic ({@link DefaultSymbolicVariant}) or breakend ({@link DefaultGenomicBreakendVariant}) variants.
 * This class will not permit symbolic or breakend alleles to be used. But otherwise has no restriction on the allele
 * length or sequence, so long as it is contained within the contig and is a valid nucleotide character [A,C,G,T,N].
 * <p>
 * The static factory constructors of the {@link GenomicVariant} or {@link GenomicBreakendVariant} will automatically
 * create the correct implementation for the given alleles, so unless this specific implementation is desired it is likely
 * that the {@link GenomicVariant} static factory method will be more convenient.
 *
 * @param contig
 * @param id
 * @param strand
 * @param coordinates
 * @param ref
 * @param alt
 * @param variantType
 */
public record DefaultSequenceVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, VariantType variantType) implements GenomicVariant {

    public DefaultSequenceVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, VariantType variantType) {
        Objects.requireNonNull(coordinates);
        Objects.requireNonNull(ref);
        Objects.requireNonNull(alt);
        Objects.requireNonNull(variantType);
        VariantType.requireNonSymbolic(alt);
        if (ref.length() != coordinates.length()) {
            throw new IllegalArgumentException("Ref allele length of " + ref.length() + " inconsistent with " + coordinates + " (length " + coordinates.length() + ") ref=" + ref + ", alt=" + alt);
        }
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
        this.contig = Objects.requireNonNull(contig);
        this.id = GenomicVariant.cacheId(id);
        this.strand = Objects.requireNonNull(strand);
        this.coordinates = coordinates;
        this.ref = GenomicVariant.validateRefAllele(ref);
        this.alt = GenomicVariant.validateAltAllele(alt);
        this.variantType = VariantType.validateType(ref, alt, variantType);
    }

    public static DefaultSequenceVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return of(contig, "", strand, coordinateSystem, start, ref, alt);
    }

    public static DefaultSequenceVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return of(contig, id, strand, Coordinates.ofAllele(coordinateSystem, start, ref), ref, alt);
    }

    public static DefaultSequenceVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
        return new DefaultSequenceVariant(contig, "", strand, coordinates, ref, alt, VariantType.parseType(ref, alt));
    }

    public static DefaultSequenceVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        return new DefaultSequenceVariant(contig, id, strand, coordinates, ref, alt, VariantType.parseType(ref, alt));
    }

    /**
     * @return
     */
    @Override
    public int changeLength() {
        return alt.length() - ref.length();
    }

    /**
     * @param other
     * @return
     */
    @Override
    public DefaultSequenceVariant withStrand(Strand other) {
        if (strand() == other || isBreakend()) {
            return this;
        }
        String refRevComp = NucleotideSeq.reverseComplement(ref);
        String altRevComp = NucleotideSeq.reverseComplement(alt);
        return new DefaultSequenceVariant(contig(), id, other, coordinates().invert(contig()), refRevComp, altRevComp, variantType);
    }

    /**
     * @param coordinateSystem
     * @return
     */
    @Override
    public DefaultSequenceVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return this;
        }
        return new DefaultSequenceVariant(contig(), id, strand(), coordinates().withCoordinateSystem(coordinateSystem), ref, alt, variantType);
    }

    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultSequenceVariant that = (DefaultSequenceVariant) o;
        return Objects.equals(contig, that.contig) && strand == that.strand && Objects.equals(coordinates, that.coordinates) && Objects.equals(ref, that.ref) && Objects.equals(alt, that.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, strand, coordinates, ref, alt);
    }


    @Override
    public String toString() {
        return "SequenceVariant{" +
               "contig=" + contigId() +
               ", id='" + id + '\'' +
               ", strand=" + strand() +
               ", coordinateSystem=" + coordinateSystem() +
               ", start=" + start() +
               ", end=" + end() +
               ", ref='" + ref + '\'' +
               ", alt='" + alt + '\'' +
               ", variantType=" + variantType() +
               ", length=" + length() +
               ", changeLength=" + changeLength() +
               '}';
    }
}
