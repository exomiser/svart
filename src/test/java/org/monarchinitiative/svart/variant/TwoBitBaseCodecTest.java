package org.monarchinitiative.svart.variant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TwoBitBaseCodecTest {

    @ParameterizedTest
    @CsvSource({
            "'', true",
            "a, true",
            "A, true",
            "c, true",
            "C, true",
            "g, true",
            "G, true",
            "t, true",
            "T, true",
            "*, false",
            "., false",
            "n, false",
            "N, false",
            "ATCGGGTTTATGGGCA, true",
            "ATCGGGTTTATG*GCA, false",
            "ATCGNGTTTATGGCA, false",
    })
    void isJustACGT(String seq, boolean expected) {
        assertEquals(expected, TwoBitBaseCodec.isJustACGT(seq));
    }

    @ParameterizedTest
    @CsvSource({
            "'', 0",
            "a, 0",
            "A, 0",
            "c, 1",
            "C, 1",
            "g, 2",
            "G, 2",
            "t, 3",
            "T, 3",
            "TA, 12",
            "TAC, 49",
            "ATGC, 57",
            "TGGGCTTATCGTATCGTAG, 251779272114"
    })
    void encodeAllele(String seq, long expected) {
        assertEquals(expected, TwoBitBaseCodec.encodeAllele(seq));
    }

    @ParameterizedTest
    @CsvSource({
            "'', 0",
            "A, 0",
            "C, 1",
            "G, 2",
            "T, 3",
            "TA, 12",
            "TAC, 49",
            "ATGC, 57",
            "TGGGCTTATCGTATCGTAG, 251779272114"
    })
    void decodeAllele(String allele, long bits) {
        assertEquals(allele, TwoBitBaseCodec.decodeAllele(allele.length(), (allele.length() - 1) * 2, bits));
    }

    @ParameterizedTest
    @CsvSource({
            "'', 0",
            "A, 0",
            "C, 1",
            "G, 2",
            "T, 3",
            "TA, 12",
            "TAC, 49",
            "ATGC, 57",
            "TGGGCTTATCGTATCGTAG, 251779272114"
    })
    void reverseComplementAllele(String seq) {
        var encoded= TwoBitBaseCodec.encodeAllele(seq);
        var reversed = TwoBitBaseCodec.reverseComplementAllele(seq.length(), 0, encoded);
        var backAgain = TwoBitBaseCodec.reverseComplementAllele(seq.length(), 0, reversed);
        assertEquals(encoded, backAgain);
        assertEquals(seq, TwoBitBaseCodec.decodeAllele(seq.length(), (seq.length() - 1) * 2, backAgain));
    }
}