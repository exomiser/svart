package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.util.VariantTrimmer.VariantPosition;

import java.util.Objects;

/**
 * Utility class for converting VCF variants into {@link GenomicVariant} objects. This class assumes VCF input coordinates with
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


    /**
     * Creates a {@link GenomicVariant} from the input VCF values. This method will only accept precise sequence variants,
     * i.e. those with known REF and ALT allele sequences.
     * <p>
     * All input values should be provided exactly as seen in the VCF record, apart from multi allelic sites which should
     * be split by alt allele and input separately. This class will handle the trimming of variants using the {@link VariantTrimmer}
     * provided in the class constructor.
     * <p>
     * For symbolic variants it is necessary to use one of the  {@linkplain #convertSymbolic} methods as those variants
     * require more information to be provided, which cannot be computed due to the input sequence being missing.
     *
     * @throws IllegalArgumentException if provided with a symbolic or multi-alleleic alt allele.
     */
    public GenomicVariant convert(Contig contig, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        return GenomicVariant.of(contig, id, Strand.POSITIVE, CoordinateSystem.ONE_BASED, trimmed.start(), trimmed.ref(), trimmed.alt());
    }

    /**
     * Creates a {@link BaseGenomicVariant.Builder} from the input VCF values. This method will only accept precise sequence variants,
     * i.e. those with known REF and ALT allele sequences.
     * <p>
     * All input values should be provided exactly as seen in the VCF record, apart from multi allelic sites which should
     * be split by alt allele and input separately. This class will handle the trimming of variants using the {@link VariantTrimmer}
     * provided in the class constructor.
     * <p>
     * For symbolic variants it is necessary to use one of the  {@linkplain #convertSymbolic} methods as those variants
     * require more information to be provided, which cannot be computed due to the input sequence being missing.
     *
     * @throws IllegalArgumentException if provided with a symbolic or multi-alleleic alt allele.
     */
    public <T extends BaseGenomicVariant.Builder<T>> T convert(T builder, Contig contig, String id, int pos, String ref, String alt) {
        VariantPosition trimmed = checkAndTrimNonSymbolic(pos, ref, alt);
        Coordinates coordinates = Coordinates.ofAllele(CoordinateSystem.ONE_BASED, trimmed.start(), trimmed.ref());
        return builder.variant(contig, Strand.POSITIVE, coordinates, trimmed.ref(), trimmed.alt()).id(id);
    }

    private VariantPosition checkAndTrimNonSymbolic(int pos, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        return variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicVariant} created from the VCF input value arguments for
     * symbolic alleles. This method will accept any type of symbolic or breakend alt (e.g. {@literal <DEL>} or {@literal C[2:12345[}) allele.
     * <p>
     * Alleles will be trimmed according to the {@link VariantTrimmer} supplied to this class.
     * <p>
     * Multi-allelic (e.g. CTT,CCT) ALT alleles are not supported and will throw an {@link IllegalArgumentException}
     *
     * @throws IllegalArgumentException when supplied with a multi-allelic alt allele
     */
    public GenomicVariant convertSymbolic(Contig contig, String id, int pos, int end, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
        return GenomicVariant.of(contig, id, Strand.POSITIVE, CoordinateSystem.ONE_BASED, trimmed.start(), end, trimmed.ref(), trimmed.alt(), svlen);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicVariant} created from the VCF input value arguments for
     * symbolic alleles. This method will accept any type of symbolic or breakend alt (e.g. {@literal <DEL>} or {@literal C[2:12345[}) allele.
     * <p>
     * Alleles will be trimmed according to the {@link VariantTrimmer} supplied to this class.
     * <p>
     * Multi-allelic (e.g. CTT,CCT) ALT alleles are not supported and will throw an {@link IllegalArgumentException}
     *
     * @throws IllegalArgumentException when supplied with a multi-allelic alt allele
     */
    public GenomicVariant convertSymbolic(Contig contig, String id, int pos, int end, String ref, String alt, int svlen, String mateId, String eventId) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, trimmed.start(), end);
        return GenomicVariant.of(contig, id, Strand.POSITIVE, coordinates, trimmed.ref(), trimmed.alt(), svlen, mateId, eventId);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicVariant} created from the VCF input value arguments for
     * symbolic alleles. This method will accept any type of symbolic or breakend alt (e.g. {@literal <DEL>} or {@literal C[2:12345[}) allele.
     * <p>
     * Alleles will be trimmed according to the {@link VariantTrimmer} supplied to this class.
     * <p>
     * Multi-allelic (e.g. CTT,CCT) ALT alleles are not supported and will throw an {@link IllegalArgumentException}
     *
     * @throws IllegalArgumentException when supplied with a multi-allelic alt allele
     */
    public GenomicVariant convertSymbolic(Contig contig, String id, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, start, ref, alt);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, trimmed.start(), startCi, end, endCi);
        return GenomicVariant.of(contig, id, Strand.POSITIVE, coordinates, trimmed.ref(), trimmed.alt(), svlen);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicVariant} created from the VCF input value arguments for
     * symbolic alleles. This method will accept any type of symbolic or breakend alt (e.g. {@literal <DEL>} or {@literal C[2:12345[}) allele.
     * <p>
     * Alleles will be trimmed according to the {@link VariantTrimmer} supplied to this class.
     * <p>
     * Multi-allelic (e.g. CTT,CCT) ALT alleles are not supported and will throw an {@link IllegalArgumentException}
     *
     * @throws IllegalArgumentException when supplied with a multi-allelic alt allele
     */
    public GenomicVariant convertSymbolic(Contig contig, String id, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi, String ref, String alt, int svlen, String mateId, String eventId) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, start, ref, alt);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, trimmed.start(), startCi, end, endCi);
        return GenomicVariant.of(contig, id, Strand.POSITIVE, coordinates, trimmed.ref(), trimmed.alt(), svlen, mateId, eventId);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.BaseGenomicVariant.Builder} created from the VCF input value arguments for
     * symbolic alleles. This method will accept any type of symbolic or breakend alt (e.g. {@literal <DEL>} or {@literal C[2:12345[}) allele.
     * <p>
     * Alleles will be trimmed according to the {@link VariantTrimmer} supplied to this class.
     * <p>
     * Multi-allelic (e.g. CTT,CCT) ALT alleles are not supported and will throw an {@link IllegalArgumentException}
     *
     * @throws IllegalArgumentException when supplied with a multi-allelic alt allele
     */
    public <T extends BaseGenomicVariant.Builder<T>> T convertSymbolic(T builder, Contig contig, String id, int start, ConfidenceInterval startCi, int end, ConfidenceInterval endCi, String ref, String alt, int svlen) {
        VariantType.requireSymbolic(alt);
        VariantPosition trimmed = variantTrimmer.trim(Strand.POSITIVE, start, ref, alt);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.ONE_BASED, trimmed.start(), startCi, end, endCi);
        return builder.variant(contig, Strand.POSITIVE, coordinates, trimmed.ref(), trimmed.alt(), svlen).id(id);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicBreakendVariant} created from the VCF input value arguments.
     * <p>
     * Given the specialised type of the {@link GenomicBreakendVariant} having very different return values for the
     * coordinates, ref and alt alleles to their unresolved symbolic {@link GenomicVariant} representations, it is
     * recommended to prefer the use of a {@linkplain #convertSymbolic} method over the {@linkplain #convertBreakend} methods
     */
    public GenomicBreakendVariant convertBreakend(Contig contig, String id, int position, String ref, String alt, String mateId, String eventId) {
        VariantType.requireBreakend(alt);
        return vcfBreakendResolver.resolve(eventId, id, mateId, contig, position, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.GenomicBreakendVariant} created from the VCF input value arguments.
     * <p>
     * Given the specialised type of the {@link GenomicBreakendVariant} having very different return values for the
     * coordinates, ref and alt alleles to their unresolved symbolic {@link GenomicVariant} representations, it is
     * recommended to prefer the use of a {@linkplain #convertSymbolic} method over the {@linkplain #convertBreakend} methods
     */
    public GenomicBreakendVariant convertBreakend(Contig contig, String id, int position, ConfidenceInterval ciPos, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        VariantType.requireBreakend(alt);
        return vcfBreakendResolver.resolve(eventId, id, mateId, contig, position, ciPos, ciEnd, ref, alt);
    }


    /**
     * Returns a {@link org.monarchinitiative.svart.BaseGenomicBreakendVariant.Builder} created from the VCF input
     * value arguments.
     * <p>
     * Given the specialised type of the {@link GenomicBreakendVariant} having very different return values for the
     * coordinates, ref and alt alleles to their unresolved symbolic {@link GenomicVariant} representations, it is
     * recommended to prefer the use of a {@linkplain #convertSymbolic} method over the {@linkplain #convertBreakend} methods.
     */
    public <T extends BaseGenomicBreakendVariant.Builder<T>> T convertBreakend(T builder, Contig contig, String id, int position, String ref, String alt, String mateId, String eventId) {
        GenomicBreakendVariant breakendVariant = convertBreakend(contig, id, position, ConfidenceInterval.precise(), ref, alt, ConfidenceInterval.precise(), mateId, eventId);
        return builder.breakendVariant(breakendVariant);
    }

    /**
     * Returns a {@link org.monarchinitiative.svart.BaseGenomicBreakendVariant.Builder} created from the VCF input
     * value arguments.
     * <p>
     * Given the specialised type of the {@link GenomicBreakendVariant} having very different return values for the
     * coordinates, ref and alt alleles to their unresolved symbolic {@link GenomicVariant} representations, it is
     * recommended to prefer the use of a {@linkplain #convertSymbolic} method over the {@linkplain #convertBreakend} methods.
     */
    public <T extends BaseGenomicBreakendVariant.Builder<T>> T convertBreakend(T builder, Contig contig, String id, int position, ConfidenceInterval ciPos, String ref, String alt, ConfidenceInterval ciEnd, String mateId, String eventId) {
        GenomicBreakendVariant breakendVariant = convertBreakend(contig, id, position, ciPos, ref, alt, ciEnd, mateId, eventId);
        return builder.breakendVariant(breakendVariant);
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
