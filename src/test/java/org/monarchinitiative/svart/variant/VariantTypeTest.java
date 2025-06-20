package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.VariantType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantTypeTest {

    @Test
    void nullInput() {
        assertThrows(NullPointerException.class, () -> VariantType.parseType(null));
    }

    @Test
    void emptyInput() {
        assertThat(VariantType.parseType(""), equalTo(VariantType.UNKNOWN));
    }

    @Test
    void testParseUnknown() {
        assertThat(VariantType.parseType("WIBBLE"), equalTo(VariantType.UNKNOWN));
    }

    @ParameterizedTest
    @CsvSource({
            "A, T,    SNV",
            "CGAT, CGGT, MNV", // this is actually an untrimmed A>G SNV
            "AT, TC,  MNV", // note this is two adjacent SNVs
            "ATG, TTC,  MNV", // note this is two SNVs (A>T, G>C) separated by a T
            "ATG, TCA,  MNV",
            "AT, AT,  INV",
            "GA, TC,  INV",
            "ATG, CAT,  INV",
            "TCAG, CTGA,  INV",
            "TCAG, ctga,  INV", // technically VCF is case-insensitive. Monsterous.
            "tcag, CTGA,  INV", // technically VCF is case-insensitive. Monsterous.
            "ATG, TC,  DELINS",
            "AT, TCA,  DELINS",
            "GTGTGAT, GTGT,  DEL", // untrimmed DEL
            "ATC, AT,  DEL",  // untrimmed DEL (fwd strand)
            "CTA, TA,  DEL",  // untrimmed DEL (rev strand)
            "TC, T,   DEL", // VCF trimmed DEL
            "AG, A,   DEL", // VCF trimmed DEL (fwd strand)
            "CT, C,   DEL", // VCF trimmed DEL (rev strand)
            "C, '',   DEL", // fully-trimmed DEL
            "CGAT, CGT,  DELINS",  // this is an untrimmed DEL A
            "AT, ATC,    INS",  // untrimmed INS (fwd strand)
            "AT, GAT,    INS",  // untrimmed INS (rev strand)
            "T, TC,   INS",  // VCF trimmed INS
            "A, AG,   INS",  // VCF trimmed INS (fwd strand)
            "T, CT,   INS",  // VCF trimmed INS (rev strand)
            "'', C,   INS", // fully-trimmed INS
            "CGAT, CGTAT, DELINS",  // this is an untrimmed INS T
            "ATC, AG,    DELINS",
            "TC, G,    DELINS",
            "AG, ATC,   DELINS",
            "G, TC,    DELINS",
            "ACG, TC,    DELINS",
            "TC, ACG,    DELINS",
            "N, <DEL>,    DEL",
            "N, <INS>,    INS",
            "N, <INS:ME>,    INS_ME",
            "'', <INS:ME>,    INS_ME",
            "N, <INS:ME:ALU>,    INS_ME_ALU",
            "N, <CNV:GAIN>,    CNV_GAIN",
            "N, C[2:12345[,    BND",
            "N, <CNV:TR>,   CNV_TR",
    })
    void parseTypeRefAlt(String ref, String alt, VariantType baseType) {
        assertThat(VariantType.parseType(ref, alt), equalTo(baseType));
    }

    @ParameterizedTest
    @CsvSource({
            "<DEL>, DEL",
            "<INS:ME:HERV>, INS_ME_HERV",
            "<INS:ME:ALU>, INS_ME_ALU",
            "<ILLEGAL:VALUE>, SYMBOLIC"
    })
    void testParseAngleBracketValue(String input, VariantType expected) {
        assertThat(VariantType.parseType(input), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "DEL, DEL",
            "INS:ME:HERV, INS_ME_HERV",
            "ILLEGAL:VALUE, UNKNOWN"
    })
    void testParseStrippedValue(String input, VariantType expected) {
        assertThat(VariantType.parseType(input), equalTo(expected));
    }

    @Test
    void validateType() {
        assertThat(VariantType.validateType("A", "T", VariantType.SNV), equalTo(VariantType.SNV));
        assertThat(VariantType.validateType("N", "<INS>", VariantType.INS), equalTo(VariantType.INS));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> VariantType.validateType("A", "T", VariantType.MNV));
        assertThat(exception.getMessage(), equalTo("Variant type MNV not consistent with variant A-T"));
    }

    @Test
    void testGetBaseMethodFromBaseType() {
        assertThat(VariantType.DEL.baseType(), equalTo(VariantType.DEL));
    }

    @ParameterizedTest
    @CsvSource({
            "DEL_ME_ALU, DEL",
            "INS_ME, INS",
            "INS_ME_ALU, INS",
            "CNV_GAIN, CNV",
            "CNV_TR, CNV",
            "BND, BND"
    })
    void testGetBaseTypeFromSubType(VariantType subType, VariantType baseType) {
        assertThat(subType.baseType(), equalTo(baseType));
    }

    @Test
    void testGetSubType() {
        assertThat(VariantType.DEL.subType(), equalTo(VariantType.DEL));
        assertThat(VariantType.DEL_ME.subType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.DEL_ME_ALU.subType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.INS_ME.subType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.INS_ME_HERV.subType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.BND.subType(), equalTo(VariantType.BND));
        assertThat(VariantType.CNV_TR.subType(), equalTo(VariantType.CNV_TR));
    }

    @Test
    void testNonCanonicalDelMobileElementSubType() {
        assertThat(VariantType.parseType("DEL:ME:SINE"), equalTo(VariantType.DEL_ME));
    }

    @ParameterizedTest
    @CsvSource({
            "STR",
            "STR27",
            "<STR27>"
    })
    void testStrWithNumRepeats(String input) {
        assertThat(VariantType.parseType(input), equalTo(VariantType.STR));
    }

    @ParameterizedTest
    @CsvSource({
            "CNV_GAIN",
            "CNV_LOSS",
            "CNV_LOH",
            "CNV_COMPLEX",
    })
    void testBaseTypeForCanvasTypes(VariantType variantType) {
        assertThat(variantType.baseType(), equalTo(VariantType.CNV));
    }

    @ParameterizedTest
    @CsvSource({
            "]13:123456]AGTNNNNNCAT, true",
            "]13:123456[AGTNNNNNCAT, true",
            "[13:123456[AGTNNNNNCAT, true",
            "[13:123456]AGTNNNNNCAT, true",
            "[, false",
            "], false",
            "AGTNNNNNCAT, false",
            "., false",
    })
    void isMatedBreakend(String allele, boolean expected) {
        assertThat(VariantType.isMatedBreakend(allele), equalTo(expected));
    }

    @Test
    void isMatedBreakendEmptyStringReturnsFalse() {
        assertThat(VariantType.isMatedBreakend(""), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "]13:123456]AGTNNNNNCAT, false",
            "]13:123456[AGTNNNNNCAT, false",
            "[13:123456[AGTNNNNNCAT, false",
            "[13:123456]AGTNNNNNCAT, false",
            "[, false",
            "], false",
            "AGTNNNNNCAT, false",
            "<INS>, false",
            "., false",
            ".ATC, true",
            "ATC., true",
    })
    void isSingleBreakend(String allele, boolean expected) {
        assertThat(VariantType.isSingleBreakend(allele), equalTo(expected));
    }

    @Test
    void isSingleBreakendEmptyStringReturnsFalse() {
        assertThat(VariantType.isSingleBreakend(""), equalTo(false));
    }


    @ParameterizedTest
    @CsvSource({
            "<INS>, true",
            "]13:123456]AGTNNNNNCAT, true",
            "]13:123456[AGTNNNNNCAT, true",
            "[13:123456[AGTNNNNNCAT, true",
            "[13:123456]AGTNNNNNCAT, true",
            "[, false",
            "], false",
            "AGTNNNNNCAT, false",
            "., false",
            ".ATC, true",
            "ATC., true",
    })
    void isSymbolic(String allele, boolean expected) {
        assertThat(VariantType.isSymbolic(allele), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "<INS>",
            "]13:123456]AGTNNNNNCAT",
            "]13:123456[AGTNNNNNCAT",
            "[13:123456[AGTNNNNNCAT",
            "[13:123456]AGTNNNNNCAT",
            ".ATC",
            "ATC.",
    })
    void requireSymbolicPasses(String allele) {
        assertThat(VariantType.requireSymbolic(allele), equalTo(allele));
    }

    @ParameterizedTest
    @CsvSource({
            "AGTNNNNNCAT",
            "[",
            "]",
            ".",
            "<",
            ">",
    })
    void requireSymbolicThrowsException(String allele) {
        Exception actual = assertThrows(IllegalArgumentException.class, () -> VariantType.requireSymbolic(allele));
        assertThat(actual.getMessage(), equalTo("Illegal non-symbolic alt allele '" + allele + "'"));
    }


    @Test
    void isSymbolicEmptyStringReturnsFalse() {
        assertThat(VariantType.isSymbolic(""), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "<INS>, true",
            "]13:123456]AGTNNNNNCAT, false",
            "]13:123456[AGTNNNNNCAT, false",
            "[13:123456[AGTNNNNNCAT, false",
            "[13:123456]AGTNNNNNCAT, false",
            "[, false",
            "], false",
            "AGTNNNNNCAT, false",
            "., false",
            ".ATC, false",
            "ATC., false",
    })
    void isLargeSymbolic(String allele, boolean expected) {
        assertThat(VariantType.isLargeSymbolic(allele), equalTo(expected));
    }

    @Test
    void isLargeSymbolicEmptyStringReturnsFalse() {
        assertThat(VariantType.isLargeSymbolic(""), equalTo(false));
    }

    @Test
    void isMissing() {
        assertThat(VariantType.isMissing("."), equalTo(true));
    }

    @Test
    void isMissingUpstreamDeletion() {
        assertThat(VariantType.isMissingUpstreamDeletion("*"), equalTo(true));
    }
}