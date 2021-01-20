package org.monarchinitiative.variant.api.util;

import org.monarchinitiative.variant.api.VariantType;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class Seq {

    private static final Charset ASCII = StandardCharsets.US_ASCII;

    private static final Map<Character, Character> IUPAC = makeIupacMap();

    private static final Map<Byte, Byte> IUPAC_COMPLEMENT_MAP = makeIupacByteMap(IUPAC);

    private Seq() {
        // static utility class
    }

    /**
     * Get reverse complement of a sequence represented by <code>seq</code> array. The input array is expected to contain
     * bases encoded using US_ASCII charset.
     *
     * @param seq array with input sequence bases
     * @return a new array with length of <code>seq.length</code> containing reverse complement of the input sequence
     */
    static byte[] reverseComplement(byte[] seq) {
        byte[] reversed = new byte[seq.length];
        for (int i = 0; i < seq.length; i++) {
            reversed[seq.length - i - 1] = IUPAC_COMPLEMENT_MAP.get(seq[i]);
        }
        return reversed;
    }

    /**
     * Get reverse complement of a nucleotide sequence <code>seq</code>. The sequence is expected to consist of IUPAC
     * nucleotide symbols. Both upper/lower cases are recognized.
     *
     * @param seq nucleotide sequence to reverse complement
     * @return reverse complemented sequence
     */
    public static String reverseComplement(String seq) {
        if (VariantType.isSymbolic(seq) || VariantType.isMissingUpstreamDeletion(seq) || VariantType.isMissing(seq)) {
            return seq;
        }
        char[] oldSeq = seq.toCharArray();
        char[] newSeq = new char[oldSeq.length];
        for (int i = 0; i < oldSeq.length; i++) {
            newSeq[oldSeq.length - i - 1] = IUPAC.get(oldSeq[i]);
        }
        return new String(newSeq);
    }

    private static Map<Byte, Byte> makeIupacByteMap(Map<Character, Character> iupac) {
        return iupac.entrySet().stream()
                .collect(Collectors.toMap(
                        // awful, I know..
                        e -> ASCII.encode(CharBuffer.wrap(new char[]{e.getKey()})).get(0),
                        e -> ASCII.encode(CharBuffer.wrap(new char[]{e.getValue()})).get(0)));
    }

    private static Map<Character, Character> makeIupacMap() {
        Map<Character, Character> temporary = new HashMap<>();
        temporary.putAll(
                Map.of(
                        // STANDARD
                        'A', 'T',
                        'a', 't',
                        'C', 'G',
                        'c', 'g',
                        'G', 'C',
                        'g', 'c',
                        'T', 'A',
                        't', 'a',
                        'U', 'A',
                        'u', 'a'));
        temporary.putAll(
                Map.of(
                        // AMBIGUITY BASES - 1st part
                        'W', 'W', // weak - A,T
                        'w', 'w',
                        'S', 'S', // strong - C,G
                        's', 's',
                        'M', 'K', // amino - A,C
                        'm', 'k',
                        'K', 'M', // keto - G,T
                        'k', 'm'));
        temporary.putAll(
                Map.of(
                        // AMBIGUITY BASES - 2nd part
                        'R', 'Y', // purine - A,G
                        'r', 'y', // purine - A,G
                        'Y', 'R', // pyrimidine - C,T
                        'y', 'r')); // pyrimidine - C,T

        temporary.putAll(
                Map.of(
                        // AMBIGUITY BASES - 3rd part
                        'B', 'V', // not A
                        'b', 'v', // not A
                        'D', 'H', // not C
                        'd', 'h', // not C
                        'H', 'D', // not G
                        'h', 'd', // not G
                        'V', 'B', // not T
                        'v', 'b', // not T
                        'N', 'N', // any one base
                        'n', 'n' // any one base
                )
        );
        return temporary;
    }

}
