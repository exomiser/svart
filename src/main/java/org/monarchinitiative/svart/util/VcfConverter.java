package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;

import java.util.Objects;

/**
 * Utility class for converting VCF variants into {@link Variant} objects. This class assumes VCF input coordinates with
 * the positions being provided as FULLY_CLOSED (one-based) on the POSITIVE strand. The class will trim any non-symbolic/breakend
 * alleles.
 */
public class VcfConverter {

    private final GenomicAssembly genomicAssembly;
    private final VariantTrimmer variantTrimmer;
    private final VcfBreakendResolver vcfBreakendResolver;

    public VcfConverter(GenomicAssembly genomicAssembly, VariantTrimmer variantTrimmer) {
        this.genomicAssembly = Objects.requireNonNull(genomicAssembly);
        this.variantTrimmer = Objects.requireNonNull(variantTrimmer);
        this.vcfBreakendResolver = new VcfBreakendResolver(genomicAssembly);
    }

    /**
     * @return The {@link GenomicAssembly} being used by the instance of this class.
     */
    public GenomicAssembly genomicAssembly() {
        return genomicAssembly;
    }

    /**
     * @return The {@link VariantTrimmer} being used by the instance of this class.
     */
    public VariantTrimmer variantTrimmer() {
        return variantTrimmer;
    }

    /**
     * This method will attempt to find a matching {@link Contig} for the identifier provided. Note that for ambiguous
     * identifiers such as '1' or 'chr1' it is possible for the incorrect {@link Contig} to be returned. Using unique
     * identifiers specific for an assembly, such as RefSeq or GenBank, will result in the correct {@link Contig} being
     * returned.
     * <p>
     * Identifiers not recognised by the {@link GenomicAssembly} will return an instance of the unknown {@link Contig}.
     * Clients should decide how to handle this in their system by checking for {@code contig.isUnknown()} before passing
     * the contig into any of the {@code convert} methods in this class as this will almost always result in an unrecoverable
     * {@link CoordinatesOutOfBoundsException} being thrown.
     *
     * @param chr the value found in the CHR field of the VCF file
     * @return the {@link Contig} in the {@link GenomicAssembly} for the {@link VcfConverter} instance or Contig.unknown()
     * if not found.
     */
    public Contig parseContig(String chr) {
        return genomicAssembly.contigByName(chr);
    }

    public Variant convert(Contig contig, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        return Variant.of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(trimmed.start()), trimmed.ref(), trimmed.alt());
    }

    public <T extends BaseVariant.Builder<T>> T convert(T builder, Contig contig, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        return builder.with(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(trimmed.start()), trimmed.ref(), trimmed.alt());
    }

    private VariantPosition checkAndTrimNonSymbolic(int pos, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
    }

    public Variant convertSymbolic(Contig contig, String id, Position pos, Position end, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos.pos(), ref, alt);
        return Variant.of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, pos.withPos(trimmed.start()), end, trimmed.ref(), trimmed.alt(), svlen);
    }

    public <T extends BaseVariant.Builder<T>> T convertSymbolic(T builder, Contig contig, String id, Position pos, Position end, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos.pos(), ref, alt);
        return builder.with(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, pos.withPos(trimmed.start()), end, trimmed.ref(), trimmed.alt(), svlen);
    }

    public BreakendVariant convertBreakend(Contig contig, String id, Position position, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        VariantType.requireBreakend(alt);
        return vcfBreakendResolver.resolve(eventId, id, mateId, contig, position, ciEnd, ref, alt);
    }

    public <T extends BaseBreakendVariant.Builder<T>> T convertBreakend(T builder, Contig contig, String id, Position position, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        BreakendVariant breakendVariant = convertBreakend(contig, id, position, ref, alt, ciEnd, mateId, eventId);
        return builder.with(breakendVariant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VcfConverter that = (VcfConverter) o;
        return genomicAssembly.equals(that.genomicAssembly) && variantTrimmer.equals(that.variantTrimmer) && vcfBreakendResolver.equals(that.vcfBreakendResolver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicAssembly, variantTrimmer, vcfBreakendResolver);
    }

    @Override
    public String toString() {
        return "VcfConverter{" +
                "genomicAssembly=" + genomicAssembly.name() +
                ", variantTrimmer=" + variantTrimmer +
                '}';
    }
}
