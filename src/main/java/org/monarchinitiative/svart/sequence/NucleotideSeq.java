package org.monarchinitiative.svart.sequence;

import org.monarchinitiative.svart.VariantType;

import java.util.Arrays;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class NucleotideSeq {

    private static final char[] COMPLEMENT_CHAR_CACHE = new char[128];
    private static final String[] COMPLEMENT_STRING_CACHE = new String[128];

    static {
        Arrays.fill(COMPLEMENT_CHAR_CACHE, 'N');
        // STANDARD
        COMPLEMENT_CHAR_CACHE['A'] = 'T';
        COMPLEMENT_CHAR_CACHE['a'] = 't';
        COMPLEMENT_CHAR_CACHE['C'] = 'G';
        COMPLEMENT_CHAR_CACHE['c'] = 'g';
        COMPLEMENT_CHAR_CACHE['G'] = 'C';
        COMPLEMENT_CHAR_CACHE['g'] = 'c';
        COMPLEMENT_CHAR_CACHE['T'] = 'A';
        COMPLEMENT_CHAR_CACHE['t'] = 'a';
        COMPLEMENT_CHAR_CACHE['U'] = 'A';
        COMPLEMENT_CHAR_CACHE['u'] = 'a';
        // NON-IUPAC VCF bases
        COMPLEMENT_CHAR_CACHE['.'] = '.'; // missing base
        COMPLEMENT_CHAR_CACHE['*'] = '*'; // missing upstream deletion

        // AMBIGUITY BASES - 1st part
        COMPLEMENT_CHAR_CACHE['W'] = 'W'; // weak - A,T
        COMPLEMENT_CHAR_CACHE['w'] = 'w';
        COMPLEMENT_CHAR_CACHE['S'] = 'S'; // strong - C,G
        COMPLEMENT_CHAR_CACHE['s'] = 's';
        COMPLEMENT_CHAR_CACHE['M'] = 'K'; // amino - A,C
        COMPLEMENT_CHAR_CACHE['m'] = 'k';
        COMPLEMENT_CHAR_CACHE['K'] = 'M'; // keto - G,T
        COMPLEMENT_CHAR_CACHE['k'] = 'm';

        // AMBIGUITY BASES - 2nd part
        COMPLEMENT_CHAR_CACHE['R'] = 'Y'; // purine - A,G
        COMPLEMENT_CHAR_CACHE['r'] = 'y'; // purine - A,G
        COMPLEMENT_CHAR_CACHE['Y'] = 'R'; // pyrimidine - C,T
        COMPLEMENT_CHAR_CACHE['y'] = 'r'; // pyrimidine - C,T

        // AMBIGUITY BASES - 3rd part
        COMPLEMENT_CHAR_CACHE['B'] = 'V'; // not A
        COMPLEMENT_CHAR_CACHE['b'] = 'v'; // not A
        COMPLEMENT_CHAR_CACHE['D'] = 'H'; // not C
        COMPLEMENT_CHAR_CACHE['d'] = 'h'; // not C
        COMPLEMENT_CHAR_CACHE['H'] = 'D'; // not G
        COMPLEMENT_CHAR_CACHE['h'] = 'd'; // not G
        COMPLEMENT_CHAR_CACHE['V'] = 'B'; // not T
        COMPLEMENT_CHAR_CACHE['v'] = 'b'; // not T
        COMPLEMENT_CHAR_CACHE['N'] = 'N'; // any one base
        COMPLEMENT_CHAR_CACHE['n'] = 'n'; // any one base
        
        Arrays.fill(COMPLEMENT_STRING_CACHE, "N");
        for (int i = 0; i < COMPLEMENT_CHAR_CACHE.length; i++) {
            COMPLEMENT_STRING_CACHE[i] = Character.toString(COMPLEMENT_CHAR_CACHE[i]);
        }
    }

    private NucleotideSeq() {
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
        int seqLength = seq.length;
        byte[] reversed = new byte[seqLength];
        for (int i = 0, newPos = seqLength - 1; i < seqLength; i++, newPos--) {
            reversed[newPos] = (byte) reverseComplement((char) seq[i]);
        }
        return reversed;
    }

    /**
     * Get reverse complement of a nucleotide sequence <code>seq</code>. The sequence is expected to consist of IUPAC
     * nucleotide symbols. Both upper/lower cases are recognized. Non-IUPAC nucleotide symbols will be replaced with the
     * character 'N'. Unicode
     *
     * @param seq nucleotide sequence to reverse complement
     * @return reverse complemented sequence
     */
    public static String reverseComplement(String seq) {
        int seqLength = seq.length();
        if (seqLength == 0 || VariantType.isSymbolic(seq)) {
            return seq;
        }
        if (seqLength == 1) {
            return revCompSingleCharacterString(seq);
        }
        char[] newSeq = new char[seqLength];
        for (int i = 0, newPos = seqLength - 1; i < seqLength; i++, newPos--) {
            newSeq[newPos] = reverseComplement(seq.charAt(i));
        }
        return new String(newSeq, 0, seqLength);
    }

    private static String revCompSingleCharacterString(String seq) {
        char c = seq.charAt(0);
        return c > 127 ? "N": COMPLEMENT_STRING_CACHE[c];
    }

    public static char reverseComplement(char c) {
        return c > 127 ? 'N': COMPLEMENT_CHAR_CACHE[c];
    }
}
