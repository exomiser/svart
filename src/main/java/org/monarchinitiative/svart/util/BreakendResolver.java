package org.monarchinitiative.svart.util;

import org.monarchinitiative.svart.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class resolves a {@link BreakendVariant} from primitive inputs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class BreakendResolver {

    private static final String IUPAC_BASES = "ACGTUWSMKRYBDHVNacgtuwsmkrybdhvn";

    /**
     * Any BND alt record must match this pattern (e.g. `G]1:123]`, `]1:123]G`, `G[1:123[`, `[1:123[G`).
     *
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

    public BreakendResolver(GenomicAssembly genomicAssembly) {
        this.genomicAssembly = genomicAssembly;
    }

    public BreakendVariant resolve(String eventId, String id, String mateId, Contig contig, Position position, ConfidenceInterval ciEnd, String ref, String alt) {
        Matcher altMatcher = BND_ALT_PATTERN.matcher(alt);
        if (!altMatcher.matches()) {
            throw new IllegalArgumentException("Invalid breakend alt record " + alt);
        }

        // parse mate data
        String mateContigName = altMatcher.group("contig");
        Contig mateContig = genomicAssembly.contigByName(mateContigName);
        if (mateContig.equals(Contig.unknown())) {
            throw new IllegalArgumentException("Unknown mate contig `" + mateContigName + '`');
        }

        int pos = Integer.parseInt(altMatcher.group("pos"));
        Position matePos = Position.of(pos, ciEnd);

        // figure out strands and the inserted sequence
        String head = altMatcher.group("head");
        String tail = altMatcher.group("tail");
        if (!head.isEmpty() && !tail.isEmpty()) {
            throw new IllegalArgumentException("Sequence present both at the beginning (`" + head + "`) and the end (`" + tail + "`) of alt field");
        }

        // left breakend strand
        Strand leftStrand;
        char refBase = ref.charAt(0);
        if (!head.isEmpty() && refBase == head.charAt(0)) {
            leftStrand = Strand.POSITIVE;
        } else if (!tail.isEmpty() && refBase == tail.charAt(tail.length()-1)) {
            leftStrand = Strand.NEGATIVE;
        } else {
            throw new IllegalArgumentException("Invalid breakend alt `" + alt + "`. No match for ref allele `"
                    + ref + "` neither at the beginning nor at the end");
        }

        // right breakend strand
        String leftBracket = altMatcher.group("left");
        String rightBracket = altMatcher.group("right");
        if (!leftBracket.equals(rightBracket)) {
            throw new IllegalArgumentException("Invalid bracket orientation in `" + alt + '`');
        }
        Strand rightStrand = (leftBracket.equals("["))
                ? Strand.POSITIVE
                : Strand.NEGATIVE;

        // inserted sequence - alt allele
        String altSeq = leftStrand.isPositive()
                ? head.substring(1)
                : tail.substring(0, tail.length() - 1);


        // assemble breakends and create the final BreakendVariant
        Breakend left = Breakend.of(contig, id, Strand.POSITIVE, CoordinateSystem.oneBased(), position).withStrand(leftStrand);
        Breakend right = Breakend.of(mateContig, mateId, Strand.POSITIVE, CoordinateSystem.oneBased(), matePos).withStrand(rightStrand);


        return BreakendVariant.of(eventId, left, right,
                leftStrand.isPositive() ? ref : Seq.reverseComplement(ref),
                leftStrand.isPositive() ? altSeq : Seq.reverseComplement(altSeq));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BreakendResolver that = (BreakendResolver) o;
        return genomicAssembly.equals(that.genomicAssembly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomicAssembly);
    }

    @Override
    public String toString() {
        return "BreakendResolver{" +
                "genomicAssembly=" + genomicAssembly.name() +
                '}';
    }
}
