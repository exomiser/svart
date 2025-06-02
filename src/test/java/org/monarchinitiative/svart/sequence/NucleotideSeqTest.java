package org.monarchinitiative.svart.sequence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class NucleotideSeqTest {

    @Test
    void reverseComplementByteArray() {
        byte[] seq = "ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn.*".getBytes(StandardCharsets.US_ASCII);
        byte[] reversed = NucleotideSeq.reverseComplement(seq);
        String rev = new String(reversed, StandardCharsets.US_ASCII);
        assertThat(rev, is("*.nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }

    @Test
    void reverseComplementString() {
        assertThat(NucleotideSeq.reverseComplement("ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn.*"),
                is("*.nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }

    @Test
    void reverseComplementEmptyString() {
        assertThat(NucleotideSeq.reverseComplement(""), equalTo(""));
    }

    @ParameterizedTest
    @CsvSource({
            "<INS>",
            "N[TCG",
            "n]ATC"
    })
    void reverseComplementSymbolic(String seq) {
        assertThat(NucleotideSeq.reverseComplement(seq), equalTo(seq));
    }

    @Test
    void reverseComplementMissing() {
        assertThat(NucleotideSeq.reverseComplement("."), equalTo("."));
    }

    @Test
    void reverseComplementUpstreamDeletion() {
        assertThat(NucleotideSeq.reverseComplement("*"), equalTo("*"));
    }

    @ParameterizedTest
    @CsvSource({
            "@",
            "!",
            "L",
            "O",
            "Ø",
            "🐱",
    })
    void reverseComplementNonNucleotideCharacterBytes(String seq) {
        assertThat(NucleotideSeq.reverseComplement(seq.getBytes(StandardCharsets.US_ASCII)), equalTo("N".getBytes(StandardCharsets.US_ASCII)));
    }

    @ParameterizedTest
    @CsvSource({
            "@",
            "!",
            "L",
            "O",
            "Ø",
    })
    void reverseComplementNonNucleotideCharacter(String seq) {
        assertThat(NucleotideSeq.reverseComplement(seq), equalTo("N"));
    }

    @Test
    void reverseComplementCatEmoji() {
        assertThat(NucleotideSeq.reverseComplement("🐱"), equalTo("NN"));
    }
}