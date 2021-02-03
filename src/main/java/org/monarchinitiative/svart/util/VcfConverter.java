package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;

import java.util.Objects;

/**
 * Utility class for converting VCF variants into {@link Variant} objects. This class assumes VCF input coordinates with
 * the positions being provided as FULLY_CLOSED (one-based) on the POSITIVE strand. The class will trim any non-symbolic/breakend
 * alleles
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

    public Variant convert(String chr, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        return Variant.of(contig(chr), id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(trimmed.start()), trimmed.ref(), trimmed.alt());
    }

    public <T extends BaseVariant.Builder<T>> T convert(T builder, String chr, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        return builder.with(contig(chr), id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(trimmed.start()), trimmed.ref(), trimmed.alt());
    }

    private VariantPosition checkAndTrimNonSymbolic(int pos, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
    }

    public Variant convertSymbolic(String chr, String id, Position pos, Position end, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos.pos(), ref, alt);
        return Variant.of(contig(chr), id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, pos.withPos(trimmed.start()), end, trimmed.ref(), trimmed.alt(), svlen);
    }

    public <T extends BaseVariant.Builder<T>> T convertSymbolic(T builder, String chr, String id, Position pos, Position end, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos.pos(), ref, alt);
        return builder.with(contig(chr), id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, pos.withPos(trimmed.start()), end, trimmed.ref(), trimmed.alt(), svlen);
    }

    public BreakendVariant convertBreakend(String chr, String id, Position position, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        VariantType.requireBreakend(alt);
        return vcfBreakendResolver.resolve(eventId, id, mateId, contig(chr), position, ciEnd, ref, alt);
    }

    public <T extends BaseBreakendVariant.Builder<T>> T convertBreakend(T builder, String chr, String id, Position position, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        BreakendVariant breakendVariant = convertBreakend(chr, id, position, ref, alt, ciEnd, mateId, eventId);
        return builder.with(breakendVariant);
    }

    public Contig contig(String chr) {
        return genomicAssembly.contigByName(chr);
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
