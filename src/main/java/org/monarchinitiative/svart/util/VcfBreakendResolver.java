package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class resolves a {@link BreakendVariant} from primitive VCF inputs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class VcfBreakendResolver {

    private static final String IUPAC_BASES = "ACGTUWSMKRYBDHVNacgtuwsmkrybdhvn";

    private static final CoordinateSystem VCF_COORDINATE_SYSTEM = CoordinateSystem.FULLY_CLOSED;
    private static final Strand VCF_STRAND = Strand.POSITIVE;

    /**
     * Any BND alt record must match this pattern (e.g. `G]1:123]`, `]1:123]G`, `G[1:123[`, `[1:123[G`).
     * <p>
     * The pattern defines 6 groups: `head`, `left`, `contig`, `pos`, `right`, and `tail`, which capture the following
     * content for alt record: `G]1:123]`:
     * <ul>
     *     <li>`head`   - a single ref base or  - </li>
     *     <li>`left`   - the left bracket</li>
     *     <li>`contig` - contig name of the mate breakend</li>
     *     <li>`pos`    - position of the mate breakend</li>
     *     <li>`right`  - the right bracket</li>
     *     <li>`tail`   - any base sequence that is at the end of the alt record</li>
     * </ul>
     */
    private static final Pattern BND_ALT_PATTERN = Pattern.compile(
            String.format("^(?<head>[%s]*)(?<left>[\\[\\]])(?<contig>[\\w._<>]+):(?<pos>\\d+)(?<right>[\\[\\]])(?<tail>[%s]*)$",
                    IUPAC_BASES, IUPAC_BASES));

    private final GenomicAssembly genomicAssembly;

    public VcfBreakendResolver(GenomicAssembly genomicAssembly) {
        this.genomicAssembly = genomicAssembly;
    }

    public BreakendVariant resolve(String eventId, String id, String mateId, Contig contig, int position, ConfidenceInterval ciPos, ConfidenceInterval ciEnd, String ref, String alt) {
        if (ref.length() > 1) {
            throw new IllegalArgumentException("Invalid breakend! Ref allele '" + ref + "' must be single base");
        }
        // now fiddle about with the ALT allele
        Breakend right;
        Strand leftStrand;
        String insertedSeq;
        Matcher altMatcher = BND_ALT_PATTERN.matcher(alt);
        if (altMatcher.matches()) {
            String head = altMatcher.group("head");
            String tail = altMatcher.group("tail");
            if (!head.isEmpty() && !tail.isEmpty()) {
                throw new IllegalArgumentException("Sequence present both at the beginning (`" + head + "`) and the end (`" + tail + "`) of alt field");
            }
            Strand rightStrand = determineRightStrand(alt, altMatcher.group("left"), altMatcher.group("right"));
            int rightStart = Integer.parseInt(altMatcher.group("pos"));
            // The right breakend position needs to be shifted by -1 because the fully-closed empty region will place the
            // break to the right of the input position but this needs to be to the left because the VCF always includes the
            // reference base but indicates the break is to the left of this. Hence 'left' and 'right' breakends.
            Coordinates coordinates = Coordinates.of(VCF_COORDINATE_SYSTEM, rightStart, ciEnd, rightStart - 1, ciEnd);
            right = parseRightBreakend(mateId, altMatcher.group("contig"), rightStrand, coordinates);
            leftStrand = determineLeftStrand(ref, alt, head, tail);
            insertedSeq = leftStrand.isPositive() ? head.substring(1) : tail.substring(0, tail.length() - 1);
        }
        // maybe this is unresolved? e.g. 'G.' (POS) or '.G' (NEG)
        else if (alt.startsWith(".") || alt.endsWith(".")) {
            right = Breakend.unresolved(VCF_COORDINATE_SYSTEM);
            leftStrand = alt.indexOf('.') == 0 ? Strand.NEGATIVE : Strand.POSITIVE;
            insertedSeq = leftStrand.isPositive() ? alt.substring(1) : alt.substring(0, alt.length() - 1);
        } else {
            throw new IllegalArgumentException("Invalid breakend alt record " + alt);
        }
        // The left breakend position needs to be shifted by +1 because the fully-closed empty region will place the
        // break to the left of the input position but this needs to be to the right because the VCF always includes the
        // reference base but indicates the break is to the right of this. Hence 'left' and 'right' breakends.
        Coordinates leftCoordinates = Coordinates.of(VCF_COORDINATE_SYSTEM, position + 1, ciPos, position, ciPos);
        Breakend left = createBreakend(contig, id, leftStrand, leftCoordinates);

        return BreakendVariant.of(eventId, left, right,
                leftStrand == VCF_STRAND ? ref : Seq.reverseComplement(ref),
                leftStrand == VCF_STRAND ? insertedSeq : Seq.reverseComplement(insertedSeq));
    }

    public Contig parseMateContig(String alt) {
        Matcher altMatcher = BND_ALT_PATTERN.matcher(alt);
        if (altMatcher.matches()) {
            return genomicAssembly.contigByName(altMatcher.group("contig"));
        }
        return Contig.unknown();
    }

    private Strand determineRightStrand(String alt, String leftBracket, String rightBracket) {
        if (!leftBracket.equals(rightBracket)) {
            throw new IllegalArgumentException("Invalid bracket orientation in `" + alt + '`');
        }
        return rightBracket.equals("[") ? Strand.POSITIVE : Strand.NEGATIVE;
    }

    private Breakend parseRightBreakend(String mateId, String mateContigName, Strand rightStrand, Coordinates coordinates) {
        Contig mateContig = genomicAssembly.contigByName(mateContigName);
        if (mateContig.equals(Contig.unknown())) {
            throw new IllegalArgumentException("Unknown mate contig `" + mateContigName + '`');
        }
        return createBreakend(mateContig, mateId, rightStrand, coordinates);
    }

    private static Breakend createBreakend(Contig contig, String id, Strand strand, Coordinates coordinates) {
        // VCF coordinates are always given in 1-based coordinates on the POS strand, so we need to check the position
        // and *then* create the breakend on the to the strand indicated - do not be tempted to inline the strand part prematurely!
//        Position breakStart = strand == VCF_STRAND ? start : end.invert(VCF_COORDINATE_SYSTEM, contig);
//        Position breakEnd = strand == VCF_STRAND ? end : start.invert(VCF_COORDINATE_SYSTEM, contig);
        Coordinates coord = strand == VCF_STRAND ? coordinates : coordinates.invert(contig);
        return Breakend.of(contig, id, strand, coord);
    }

    private Strand determineLeftStrand(String ref, String alt, String head, String tail) {
        char refBase = ref.charAt(0);
        if (!head.isEmpty() && refBase == head.charAt(0)) {
            // e.g C,  CAGT[2:321682[
            return Strand.POSITIVE;
        } else if (!tail.isEmpty() && refBase == tail.charAt(tail.length() - 1)) {
            // e.g C,  [2:321682[TGAC
            return Strand.NEGATIVE;
        }
        throw new IllegalArgumentException("Invalid breakend alt `" + alt + "`. No matching ref allele `"
                + ref + "` at beginning or end of alt sequence");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VcfBreakendResolver that = (VcfBreakendResolver) o;
        return genomicAssembly.equals(that.genomicAssembly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicAssembly);
    }

    @Override
    public String toString() {
        return "VcfBreakendResolver{" +
                "genomicAssembly=" + genomicAssembly.name() +
                '}';
    }
}
