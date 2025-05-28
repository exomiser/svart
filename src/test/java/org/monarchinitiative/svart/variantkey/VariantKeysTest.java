package org.monarchinitiative.svart.variantkey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VariantKeysTest {

    @Test
    void encodeRefAlt() {
        System.out.printf("%08x%n", VariantKeys.encodeRefAlt("T", "C"));
        System.out.printf("%08x%n", VariantKeys.encodeRefAlt("ACG", "ACGTACGTAC"));
        System.out.printf("%08x%n", VariantKeys.encodeRefAlt("GCCTCCCCAGCCACGGTGAGGACCCACCCTGGCATGATCCCCCTCATCA", "G"));
        System.out.printf("%08x%n", VariantKeys.encodeRefAlt("AACCATCTGTATTGATGCACTGTCCATGTTT", "A"));
    }

    @Test
    void testEncodeRefAlt() {
        final String[] input =
                {"A", "C", "N", "GT", "ACG", "ACGTa", "ACGTac", "ACGTacg", "ACGTacgt", "ACGTACGTAC", "ACGTacgtACGT"};

        final long[] expected = {
                0x08800000, 0x08800000, 0x08880000, 0x08a00000, 0x2b524725,
                0x13ace339, 0x09160000, 0x10d80000, 0x09830000, 0x188c0000,
                0x0a836000, 0x288d8000, 0x0b036200, 0x308d8800, 0x0b836300,
                0x388d8c00, 0x0c036360, 0x408d8d80, 0x0d036362, 0x508d8d88,
                0x3f0ad81b, 0x519ec623, 0x08a80000, 0x08a80000, 0x51fde969,
                0x5a3ad561, 0x09360000, 0x10da0000, 0x09a30000, 0x188c8000,
                0x0aa36000, 0x288d8800, 0x0b236200, 0x308d8a00, 0x0ba36300,
                0x388d8c80, 0x0c236360, 0x408d8da0, 0x0d236362, 0x508d8d8a,
                0x535d6025, 0x50fd215f, 0x756046af, 0x756046af, 0x39e18639,
                0x699d04d1, 0x2e4f3f0b, 0x3bc2ca01, 0x688d4593, 0x4b35f78d,
                0x3c371093, 0x6ad462d3, 0x56f03e05, 0x709febcd, 0x10529813,
                0x64690b25, 0x62a17681, 0x46f770b7, 0x2ed058cb, 0x77413a59,
                0x115d8000, 0x115d8000, 0x11d8c000, 0x190d6000, 0x12d8d800,
                0x290d9600, 0x1358d880, 0x310d8d80, 0x13d8d8c0, 0x390d8d60,
                0x1458d8d8, 0x410d8dd8, 0x25eff603, 0x69e4072b, 0x650dd623,
                0x5869066f, 0x198c3000, 0x198c3000, 0x1a8c3600, 0x298d8300,
                0x1b0c3620, 0x318d88c0, 0x1b8c3630, 0x398d8c30, 0x1c0c3636,
                0x418d8d8c, 0x0b6df65d, 0x7087bc41, 0x5461bb97, 0x121cc5b3,
                0x2a8d8360, 0x2a8d8360, 0x2b0d8362, 0x328d88d8, 0x2b21970d,
                0x6a552833, 0x1e5367a7, 0x1b5a1cd9, 0x22a4482b, 0x0869e1d1,
                0x554389ed, 0x2578e3b3, 0x490569b1, 0x490569b1, 0x3fda5cad,
                0x48a04ed9, 0x3eb532e3, 0x28a272e3, 0x67768ecf, 0x45f77839,
                0x1f7f9b69, 0x68244db3, 0x333e12a5, 0x333e12a5, 0x5f37dd43,
                0x5231912b, 0x512ee699, 0x57b03177, 0x4b88730f, 0x222ba3a9,
                0x10fa989b, 0x10fa989b, 0x76788595, 0x1694f703, 0x266d0563,
                0x366a23af, 0x56871e01, 0x56871e01, 0x6001429b, 0x7bc28a63,
                0x3a396f37, 0x3a396f37
        };
        int k = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = i; j < input.length; j++) {
                int ri = i;
                int rj = j;
                for (int r = 0; r < 2; r++) {
                    if (input[ri].equals("N") || input[rj].equals("N")) {
//                        System.out.println("Skipping N base input");
                    } else {
                        long h = VariantKeys.encodeRefAlt(input[ri], input[rj]);
//                        System.out.printf("encodeRefAlt(%s, %s) == 0x%8x%n", input[ri], input[rj], h);
                        assertThat(h, equalTo(expected[k]));
                    }
                    k++;
                    int tmp = ri;
                    ri = rj;
                    rj = tmp;
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "chr1, 268435455, C, T, 0fffffff88b80000",
            "1, 0, ACCTCACCAGGCCCAGCTCATGCTTCTTTGCAG, A, 080000003c6f5d8f",
            "1, 268435455, ACCAGGCCCAGCTCATGCTTCTTTGCAGCCTCT, A, 0fffffff8ae2503b",
            "1, 324683, C, G, 08027a2588b00000",
            "1, 324692, N, A, 08027a2a13ace339",
            "1, 324728, T, C, 08027a3c08e80000",
            "1, 324728, T, ., 08027a3c7642c145",
            "1, 324728, T, *, 08027a3c7642c145",
            "1, 324728, T, <DEL>, 08027a3c1ca3baa5",
            "1, 324728, T, [chr3:12345[ATTC, 08027a3c16d07f65",
            "1, 324739, ACGTUWSMKRYBDHVN, A*C*G*T*U*W*S*M*K*R*Y*B*D*H*V*N, 08027a41e6f83195",
            "Y, 445978, A, G, c003670d08900000",
            "Y, 445978, AAAGAAAGAAAGAAAGAAAG, A, c003670d6842610d",
    })
    void encodeVariantKey(String chrom, int pos, String ref, String alt, String variantKey) {
        assertThat(VariantKeys.encodeVariantKey(chrom, pos, ref, alt), equalTo(Long.parseUnsignedLong(variantKey, 16)));
    }

    @ParameterizedTest
    @CsvSource({
            "chr1, 268435455, C, T, 0fffffff88b80000",
            "1, 324683, C, G, 08027a2588b00000",
            "1, 324728, T, C, 08027a3c08e80000",
            "Y, 445978, A, G, c003670d08900000",
    })
    void decodeVariantKeyReversible(String chrom, int pos, String ref, String alt, String variantKey) {
        long key = Long.parseUnsignedLong(variantKey, 16);
        assertThat(VariantKeys.decodeVariantKey(key),
                equalTo(VariantKey.of(chrom.replace("chr", ""), pos, ref, alt)));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0, ACCTCACCAGGCCCAGCTCATGCTTCTTTGCAG, A, 080000003c6f5d8f",
            "1, 268435455, ACCAGGCCCAGCTCATGCTTCTTTGCAGCCTCT, A, 0fffffff8ae2503b",
            "1, 324692, N, A, 08027a2a13ace339",
            "1, 324728, T, ., 08027a3c7642c145",
            "1, 324728, T, *, 08027a3c7642c145",
            "1, 324728, T, <DEL>, 08027a3c1ca3baa5",
            "1, 324728, T, [chr3:12345[ATTC, 08027a3c16d07f65",
            "1, 324739, ACGTUWSMKRYBDHVN, A*C*G*T*U*W*S*M*K*R*Y*B*D*H*V*N, 08027a41e6f83195",
            "Y, 445978, AAAGAAAGAAAGAAAGAAAG, A, c003670d6842610d",
    })
    void decodeVariantKeyNonReversible(String chrom, int pos, String ref, String alt, String variantKey) {
        long key = Long.parseUnsignedLong(variantKey, 16);
        assertThat(VariantKeys.decodeVariantKey(key),
                equalTo(new VariantKey(key, chrom.replace("chr", ""), pos, "", "")));
    }

    @Test
    void orderProperties() {
        List<VariantKey> expected = List.of(
                VariantKey.of(1, 12345, "", "A"),
                VariantKey.of(1, 12345, "", "T"),
                VariantKey.of(1, 12345, "A", "A"),
                VariantKey.of(1, 12345, "A", "C"),
                VariantKey.of(1, 12345, "A", "G"),
                VariantKey.of(1, 12345, "A", "T"),
                VariantKey.of(1, 12345, "C", "A"),
                VariantKey.of(1, 12345, "C", "C"),
                VariantKey.of(1, 12345, "G", "A"),
                VariantKey.of(1, 12345, "A", "AA"),
                VariantKey.of(1, 12345, "A", "CA"),
                VariantKey.of(1, 12345, "A", "TC"),
                VariantKey.of(1, 12345, "AC", "C"),
                VariantKey.of(1, 12346, "A", "A"),
                VariantKey.of(1, 12346, "T", "A"),
                VariantKey.of(1, 12346, "T", "TAC"),
                VariantKey.of(1, 12346, "N", "<DEL>"), // non-reversible
                VariantKey.of(1, 12346, "T", "<DEL>"), // non-reversible
                VariantKey.of(1, 12346, "C", "<DEL>"), // non-reversible
                VariantKey.of(1, 12346, "CATCGGATCCCATAA", "<DEL>"), // non-reversible
                VariantKey.of(1, 12347, "A", "A"),
                VariantKey.of(1, 12347, "C", "A"),
                VariantKey.of(2, 12347, "C", "A")
        );

        var sorted = new ArrayList<>(expected);
        Collections.shuffle(sorted);
        Collections.sort(sorted);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    void variantKeyTests() {
        VariantKey instance = VariantKey.of("1", 12345, "A", "C");
        long encoded = instance.key();
        assertThat(instance, equalTo(VariantKeys.decodeVariantKey(encoded)));
        assertThat(instance.isReversible(), is(true));
    }

    @Test
    void throwsExceptionWithOverLargePos() {
        assertThrows(IllegalArgumentException.class, () -> VariantKey.of("1", VariantKeys.MAX_POS + 1, "A", "C"));
        assertThrows(IllegalArgumentException.class, () -> VariantKeys.encodeVariantKey("1", VariantKeys.MAX_POS + 1, "A", "C"));
    }

    @Test
    void throwsExceptionWithOverLargeChrom() {
        assertThrows(IllegalArgumentException.class, () -> VariantKey.of((short) 26, 12345, "A", "C"));
        assertThrows(IllegalArgumentException.class, () -> VariantKeys.encodeVariantKey((short) 26, 12345, "A", "C"));
        assertThrows(IllegalArgumentException.class, () -> VariantKeys.encodeVariantKey((short) -1, 12345, "A", "C"));
    }

    @ParameterizedTest
    @CsvSource({
            "chr",
            "chrUnk_KI12345.1",
            "chr0",
            "chr25",
            "0",
            "25",
    })
    void encodeChromThrowsExceptionWith(String chrom) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> VariantKeys.encodeChrom(chrom));
        assertThat(exception.getMessage(), equalTo("Invalid chrom: " + chrom + " Must be (chr)?[1-22,X,Y,M,MT]"));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "chr1, 1",
            "CHR1, 1",
            "chrM, 25",
            "MT, 25",
    })
    void encodeChrom(String chrom, long expected) {
        assertThat(VariantKeys.encodeChrom(chrom), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "chr1, 268435455, C, T",
            "2, 268435455, CT, T",
            "2, 268435455, CTT, T",
            "22, 268435455, CTT, T",
            "X, 268435455, CTT, T",
            "Y, 268435455, CTT, T",
            "MT, 268435455, CTT, T",
    })
    void testDecodeRefLength(String chrom, int pos, String ref, String alt) {
        long variantKey = VariantKeys.encodeVariantKey(chrom, pos, ref, alt);
        assertThat(VariantKeys.refLength(variantKey), equalTo(ref.length()));
    }

    @Test
    void testNonReversibleVariantKeys() {
        VariantKey nonReversible = VariantKey.of("1", 268435455, "ACCTCACCAGGCCCAGCTCATGCTTCTTTGCAG", "");
        assertThat(VariantKeys.decodeChrom(nonReversible.key()), equalTo("1"));
        assertThat(VariantKeys.decodePos(nonReversible.key()), equalTo(268435455L));
        assertThat(VariantKeys.decodeRef(nonReversible.key()), equalTo(""));
        assertThat(VariantKeys.decodeAlt(nonReversible.key()), equalTo(""));
        assertThat(VariantKeys.isReversible(nonReversible.key()), equalTo(false));
    }

    @Test
    void roundTripRandomVariants() {
        Random rand = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            VariantKey variantKey = buildRandomVariantKey(rand);
            assertThat(variantKey.isReversible(), equalTo(true));
            assertThat(variantKey, equalTo(VariantKeys.decodeVariantKey(variantKey.key())));
        }
    }

    static final String[] CHROMS = {
            "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20",
            "21", "22", "X", "Y", "MT"};

    private static VariantKey buildRandomVariantKey(Random rand) {
        String chr = CHROMS[rand.nextInt(0, 25)];
        int pos = rand.nextInt(VariantKeys.MAX_POS);
        int refLen = rand.nextInt(0, 11);
        int altLen = rand.nextInt(0, 11 - refLen);
        String ref = allele(refLen, rand);
        String alt = allele(altLen, rand);
        return VariantKey.of(chr, pos, ref, alt);
    }

    private static final byte[] BASES = {'A', 'C', 'G', 'T'};

    private static String allele(int refLen, Random rand) {
        byte[] refBytes = new byte[refLen];
        for (int j = 0; j < refLen; j++) {
            refBytes[j] = BASES[rand.nextInt(0, 4)];
        }
        return new String(refBytes);
    }
}