package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;

import java.util.Objects;

/**
 * Utility class for converting VCF variants into {@link Variant} objects. This class assumes VCF input coordinates with
 * the positions being provided as FULLY_CLOSED (one-based) on the POSITIVE strand. The class will trim any non-symbolic/breakend
 * alleles
 */
public class VcfConverter {

    private final GenomicAssembly genomicAssembly;
    private final VariantTrimmer variantTrimmer;
    private final BreakendResolver breakendResolver;

    public VcfConverter(GenomicAssembly genomicAssembly, VariantTrimmer variantTrimmer) {
        this.genomicAssembly = Objects.requireNonNull(genomicAssembly);
        this.variantTrimmer = Objects.requireNonNull(variantTrimmer);
        this.breakendResolver = new BreakendResolver(genomicAssembly);
    }

    public Variant convert(String chr, String id, int pos, String ref, String alt) {
        if (VariantType.isSymbolic(alt)) {
            throw new IllegalArgumentException("Illegal symbolic alt allele " + alt);
        }
        if (alt.contains(",")) {
            throw new IllegalArgumentException("Illegal multi-allelic alt allele " + alt);
        }
        Contig contig = genomicAssembly.contigByName(chr);
        VariantTrimmer.VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
        return Variant.of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(trimmed.start()), trimmed.ref(), trimmed.alt());
    }

    public Variant convertSymbolic(String chr, String id, Position pos, Position end, String ref, String alt, int svlen) {
        if (!VariantType.isLargeSymbolic(alt)) {
            throw new IllegalArgumentException("Illegal non-symbolic or breakend alt allele " + alt);
        }
        Contig contig = genomicAssembly.contigByName(chr);
        return Variant.of(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, pos, end, ref, alt, svlen);
    }

    public BreakendVariant convertBreakend(String chr, String id, Position position, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        if (!VariantType.isBreakend(alt)) {
            throw new IllegalArgumentException("Illegal non-breakend alt allele " + alt);
        }
        Contig contig = genomicAssembly.contigByName(chr);
        return breakendResolver.resolve(eventId, id, mateId, contig, position, ciEnd, ref, alt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VcfConverter that = (VcfConverter) o;
        return genomicAssembly.equals(that.genomicAssembly) && variantTrimmer.equals(that.variantTrimmer) && breakendResolver.equals(that.breakendResolver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicAssembly, variantTrimmer, breakendResolver);
    }

    @Override
    public String toString() {
        return "VcfConverter{" +
                "genomicAssembly=" + genomicAssembly.name() +
                ", variantTrimmer=" + variantTrimmer +
                '}';
    }
}
