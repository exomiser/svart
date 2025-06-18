package org.monarchinitiative.svart;

import org.monarchinitiative.svart.variant.DefaultGenomicBreakendVariant;
import org.monarchinitiative.svart.vcf.VcfBreakendFormatter;
import org.monarchinitiative.svart.vcf.VcfBreakendResolver;

/**
 * A {@link GenomicBreakendVariant} is a specialised type of {@link GenomicVariant} for representing pairs of
 * {@link GenomicBreakend}. The VCF specification uses a specialised notation within the ALT allele to represent the
 * orientation and strand of the breakend pairs, with MATEID and EVENTID INFO fields to assist grouping ends with their
 * mates into an 'event', for example:
 * <pre>
 * #CHROM  POS     ID      REF   ALT            QUAL FILTER  INFO
 * 2       321682  bnd_V   T     ]13:123456]T   6    PASS    SVTYPE=BND;MATEID=bnd_U;EVENTID=1
 * 13      123456  bnd_U   C     C[2:321682[    6    PASS    SVTYPE=BND;MATEID=bnd_V;EVENTID=1
 * </pre>
 * Here each record can be represented either as a {@link GenomicVariant} using the exact same values as contained in the
 * VCF record, or using a {@link VcfBreakendResolver} a {@link GenomicBreakendVariant}
 * can be 'resolved'. The resulting strands, positions and sequences can differ significantly from the VCF representation.
 * <p>
 * Given their specialised nature, it is <em>highly</em> recommended to create a standard symbolic {@link GenomicVariant}
 * for most purposes as the REF and ALT values will remain the same as in the VCF file. However, once 'resolved' into a
 * {@link GenomicBreakendVariant} using a {@link VcfBreakendResolver} the REF and ALT
 * alleles will represent the actual sequence changes on that strand. In order to return back to VCF convention,
 * the {@link VcfBreakendFormatter} can be used to re-construct the REF and ALT alleles,
 * although this interface provides a {@link GenomicBreakendVariant#toSymbolicGenomicVariant()} method to do this.
 * <p>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicBreakendVariant extends GenomicVariant {

    @Override
    String eventId();

    @Override
    default String mateId() {
        return right().id();
    }

    GenomicBreakend left();

    default GenomicBreakend right() {
        return GenomicBreakend.unresolved(coordinateSystem());
    }

    @Override
    GenomicBreakendVariant withStrand(Strand other);

    @Override
    GenomicBreakendVariant toOppositeStrand();

    @Override
    GenomicBreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    default GenomicBreakendVariant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    default GenomicBreakendVariant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    /**
     * A {@link GenomicBreakendVariant} of the given values. It is <em>highly</> recommended to create a standard symbolic
     * {@link GenomicVariant} for most purposes as the REF and ALT values will remain the same as in the VCF file. However,
     * once 'resolved' into a {@link GenomicBreakendVariant} using a {@link VcfBreakendResolver}
     * the REF and ALT alleles will represent the actual sequence changes on that strand. In order to return back to VCF convention,
     * the {@link VcfBreakendFormatter} can be used to re-construct the REF and ALT alleles
     *
     * @param eventId The VCF event ID
     * @param left  The left {@link GenomicBreakend}
     * @param right The right {@link GenomicBreakend}
     * @param ref The REF allele <b>AS IT WOULD BE REPRESENTED ON THE LEFT BREAKEND STRAND</b>
     * @param alt Any inserted bases <b>AS IT WOULD BE REPRESENTED ON THE RIGHT BREAKEND STRAND</b> if any, or empty.
     * @return a {@link GenomicBreakendVariant} composed of the input values.
     */
    static GenomicBreakendVariant of(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        return DefaultGenomicBreakendVariant.of(eventId, left, right, ref, alt);
    }

    /**
     * Returns a new symbolic {@link GenomicVariant} instance created from the current {@link GenomicBreakendVariant}.
     * The returned {@link GenomicVariant} coordinates, ref and alt allele values will match the original input VCF
     * values supplied to the {@link VcfBreakendResolver}.
     *
     * @return A {@link GenomicVariant} containing the original VCF values for POS, REF and ALT
     */
    default GenomicVariant toSymbolicGenomicVariant() {
        int pos = VcfBreakendFormatter.makePosVcfField(this);
        Coordinates coordinates = Coordinates.oneBased(pos, this.startConfidenceInterval(), pos, this.endConfidenceInterval());
        String ref = VcfBreakendFormatter.makeRefVcfField(this);
        String alt = VcfBreakendFormatter.makeAltVcfField(this);
        return GenomicVariant.builder()
                .variant(left().contig(), Strand.POSITIVE, coordinates, ref, alt, 0)
                .id(this.id())
                .eventId(this.eventId())
                .mateId(this.mateId())
                .build();
    }
}
