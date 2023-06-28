package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class resolves a {@link GenomicBreakendVariant} from primitive VCF inputs based on the VCF 4.2 specifications:
 * <p>
 * 5.4 Specifying complex rearrangements with breakends
 * <p>
 * An arbitrary rearrangement event can be summarized as a set of novel adjacencies. Each adjacency ties together
 * 2 breakends. The two breakends at either end of a novel adjacency are called mates.
 * There is one line of VCF (i.e. one record) for each of the two breakends in a novel adjacency. A breakend record is
 * identified with the tag “SVTYPE=BND” in the INFO field. The REF field of a breakend record indicates a base or
 * sequence s of bases beginning at position POS, as in all VCF records. The ALT field of a breakend record indicates
 * a replacement for s. This “breakend replacement” has three parts:
 * <p>
 * 1. The string t that replaces places s. The string t may be an extended version of s if some novel bases are inserted
 * during the formation of the novel adjacency.
 * <p>
 * 2. The position p of the mate breakend, indicated by a string of the form “chr:pos”. This is the location of the
 * first mapped base in the piece being joined at this novel adjacency.
 * <p>
 * 3. The direction that the joined sequence continues in, starting from p. This is indicated by the orientation of
 * square brackets surrounding p.
 * <p>
 * These 3 elements are combined in 4 possible ways to create the ALT. In each of the 4 cases, the assertion is that s
 * is replaced with t, and then some piece starting at position p is joined to t. The cases are:
 * <p>
 * <pre>
 *  REF  ALT     Meaning
 *  s    t[p[    piece extending to the right of p is joined after t
 *  s    t]p]    reverse comp piece extending left of p is joined after t
 *  s    ]p]t    piece extending to the left of p is joined before t
 *  s    [p[t    reverse comp piece extending right of p is joined before t
 * </pre>
 * The example in Figure 1 shows a 3-break operation involving 6 breakends. It exemplifies all possible orientations
 * of breakends in adjacencies. Notice how the ALT field expresses the orientation of the breakends.
 * <p>
 * <pre>
 * #CHROM  POS     ID      REF   ALT            QUAL FILTER  INFO
 * 2       321681  bnd_W   G     G]17:198982]   6    PASS    SVTYPE=BND;MATEID=bnd_Y;EVENTID=2
 * 2       321682  bnd_V   T     ]13:123456]T   6    PASS    SVTYPE=BND;MATEID=bnd_U;EVENTID=1
 * 13      123456  bnd_U   C     C[2:321682[    6    PASS    SVTYPE=BND;MATEID=bnd_V;EVENTID=1
 * 13      123457  bnd_X   A     [17:198983[A   6    PASS    SVTYPE=BND;MATEID=bnd_Z;EVENTID=3
 * 17      198982  bnd_Y   A     A]2:321681]    6    PASS    SVTYPE=BND;MATEID=bnd_W;EVENTID=2
 * 17      198983  bnd_Z   C     [13:123457[C   6    PASS    SVTYPE=BND;MATEID=bnd_X;EVENTID=3
 * </pre>
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class VcfBreakendResolver {

    private static final String IUPAC_BASES = "ACGTUWSMKRYBDHVNacgtuwsmkrybdhvn";

    private static final CoordinateSystem VCF_COORDINATE_SYSTEM = CoordinateSystem.ONE_BASED;
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

    public GenomicBreakendVariant resolveBreakend(GenomicVariant variant) {
        if (!VariantType.isBreakend(variant.alt())) {
            throw new IllegalArgumentException("Unable to resolve non-breakend variant!");
        }
        return resolve(variant.eventId(), variant.id(), variant.mateId(),
                variant.contig(),
                variant.startOnStrandWithCoordinateSystem(VCF_STRAND, VCF_COORDINATE_SYSTEM),
                variant.startConfidenceInterval(),
                variant.startConfidenceInterval(),
                variant.ref(),
                variant.alt()
        );
    }

    public GenomicBreakendVariant resolve(String eventId, String id, String mateId, Contig contig, int position, String ref, String alt) {
        return resolve(eventId, id, mateId, contig, position, ConfidenceInterval.precise(), ConfidenceInterval.precise(), ref, alt);
    }

    public GenomicBreakendVariant resolve(String eventId, String id, String mateId, Contig contig, int position, ConfidenceInterval ciPos, ConfidenceInterval ciEnd, String ref, String alt) {
        if (ref.length() > 1) {
            throw new IllegalArgumentException("Invalid breakend! Ref allele '" + ref + "' must be single base");
        }
        // now fiddle about with the ALT allele
        GenomicBreakend right;
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
            // reference base but indicates the break is to the left of this. Hence, 'left' and 'right' breakends.
            Coordinates coordinates = Coordinates.of(VCF_COORDINATE_SYSTEM, rightStart, ciEnd, rightStart - 1, ciEnd);
            right = parseRightBreakend(mateId, altMatcher.group("contig"), rightStrand, coordinates);
            leftStrand = determineLeftStrand(ref, alt, head, tail);
            insertedSeq = leftStrand.isPositive() ? head.substring(1) : tail.substring(0, tail.length() - 1);
        }
        // maybe this is unresolved? e.g. 'G.' (POS) or '.G' (NEG)
        else if (alt.startsWith(".") || alt.endsWith(".")) {
            right = GenomicBreakend.unresolved(VCF_COORDINATE_SYSTEM);
            leftStrand = alt.indexOf('.') == 0 ? Strand.NEGATIVE : Strand.POSITIVE;
            insertedSeq = leftStrand.isPositive() ? alt.substring(1) : alt.substring(0, alt.length() - 1);
        } else {
            throw new IllegalArgumentException("Invalid breakend alt record " + alt);
        }
        // The left breakend position needs to be shifted by +1 because the fully-closed empty region will place the
        // break to the left of the input position but this needs to be to the right because the VCF always includes the
        // reference base but indicates the break is to the right of this. Hence 'left' and 'right' breakends.
        Coordinates leftCoordinates = Coordinates.of(VCF_COORDINATE_SYSTEM, position + 1, ciPos, position, ciPos);
        GenomicBreakend left = createBreakend(contig, id, leftStrand, leftCoordinates);

        return GenomicBreakendVariant.of(eventId, left, right,
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

    private GenomicBreakend parseRightBreakend(String mateId, String mateContigName, Strand rightStrand, Coordinates coordinates) {
        Contig mateContig = genomicAssembly.contigByName(mateContigName);
        if (mateContig.equals(Contig.unknown())) {
            throw new IllegalArgumentException("Unknown mate contig `" + mateContigName + '`');
        }
        return createBreakend(mateContig, mateId, rightStrand, coordinates);
    }

    private static GenomicBreakend createBreakend(Contig contig, String id, Strand strand, Coordinates coordinates) {
        // VCF coordinates are always given in 1-based coordinates on the POS strand, so we need to check the position
        // and *then* create the breakend on the to the strand indicated - do not be tempted to inline the strand part prematurely!
        Coordinates coord = strand == VCF_STRAND ? coordinates : coordinates.invert(contig);
        return GenomicBreakend.of(contig, id, strand, coord);
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
