package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantTypeTest {

    @Test
    public void nullInput() {
        assertThrows(NullPointerException.class, () -> VariantType.parseType(null));
    }

    @Test
    public void emptyInput() {
        assertThat(VariantType.parseType(""), equalTo(VariantType.UNKNOWN));
    }

    @Test
    public void testParseUnknown() {
        assertThat(VariantType.parseType("WIBBLE"), equalTo(VariantType.UNKNOWN));
    }

    @ParameterizedTest
    @CsvSource({
            "A, T,    SNV",
            "ATG, TCA,    MNV",
            "TC, A,    DEL",
            "A, TC,    INS",
            "N, <DEL>,    DEL",
            "N, <INS>,    INS",
            "N, <INS:ME>,    INS_ME",
            "'', <INS:ME>,    INS_ME",
            "N, <INS:ME:ALU>,    INS_ME_ALU",
            "N, <CNV:GAIN>,    CNV_GAIN",
            "N, C[2:12345[,    BND",
    })
    public void parseTypeRefAlt(String ref, String alt, VariantType baseType) {
        assertThat(VariantType.parseType(ref, alt), equalTo(baseType));
    }

    @ParameterizedTest
    @CsvSource({
            "<DEL>, DEL",
            "<INS:ME:HERV>, INS_ME_HERV",
            "<INS:ME:ALU>, INS_ME_ALU",
            "<ILLEGAL:VALUE>, SYMBOLIC"
    })
    public void testParseAngleBracketValue(String input, VariantType expected) {
        assertThat(VariantType.parseType(input), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "DEL, DEL",
            "INS:ME:HERV, INS_ME_HERV",
            "ILLEGAL:VALUE, UNKNOWN"
    })
    public void testParseStrippedValue(String input, VariantType expected) {
        assertThat(VariantType.parseType(input), equalTo(expected));
    }

    @Test
    public void testGetBaseMethodFromBaseType() {
        assertThat(VariantType.DEL.baseType(), equalTo(VariantType.DEL));
    }

    @ParameterizedTest
    @CsvSource({
            "DEL_ME_ALU, DEL",
            "INS_ME, INS",
            "INS_ME_ALU, INS",
            "CNV_GAIN, CNV",
            "BND, BND"
    })
    public void testGetBaseTypeFromSubType(VariantType subType, VariantType baseType) {
        assertThat(subType.baseType(), equalTo(baseType));
    }

    @Test
    public void testGetSubType() {
        assertThat(VariantType.DEL.subType(), equalTo(VariantType.DEL));
        assertThat(VariantType.DEL_ME.subType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.DEL_ME_ALU.subType(), equalTo(VariantType.DEL_ME));
        assertThat(VariantType.INS_ME.subType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.INS_ME_HERV.subType(), equalTo(VariantType.INS_ME));
        assertThat(VariantType.BND.subType(), equalTo(VariantType.BND));
    }

    @Test
    public void testNonCanonicalDelMobileElementSubType() {
        assertThat(VariantType.parseType("DEL:ME:SINE"), equalTo(VariantType.DEL_ME));
    }

    @ParameterizedTest
    @CsvSource({
            "STR",
            "STR27",
            "<STR27>"
    })
    public void testStrWithNumRepeats(String input) {
        assertThat(VariantType.parseType(input), equalTo(VariantType.STR));
    }

    @ParameterizedTest
    @CsvSource({
            "CNV_GAIN",
            "CNV_LOSS",
            "CNV_LOH",
            "CNV_COMPLEX",
    })
    public void testBaseTypeForCanvasTypes(VariantType variantType) {
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
    public void isMatedBreakend(String allele, boolean expected) {
        assertThat(VariantType.isMatedBreakend(allele), equalTo(expected));
    }

    @Test
    public void isMatedBreakendEmptyStringReturnsFalse() {
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
    public void isSingleBreakend(String allele, boolean expected) {
        assertThat(VariantType.isSingleBreakend(allele), equalTo(expected));
    }

    @Test
    public void isSingleBreakendEmptyStringReturnsFalse() {
        assertThat(VariantType.isSingleBreakend(""), equalTo(false));
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
            "<INS>, true",
            "., false",
            ".ATC, true",
            "ATC., true",
    })
    public void isSymbolic(String allele, boolean expected) {
        assertThat(VariantType.isSymbolic(allele), equalTo(expected));
    }

    @Test
    public void isSymbolicEmptyStringReturnsFalse() {
        assertThat(VariantType.isSymbolic(""), equalTo(false));
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
            "<INS>, true",
            "., false",
            ".ATC, false",
            "ATC., false",
    })
    public void isLargeSymbolic(String allele, boolean expected) {
        assertThat(VariantType.isLargeSymbolic(allele), equalTo(expected));
    }

    @Test
    public void isLargeSymbolicEmptyStringReturnsFalse() {
        assertThat(VariantType.isLargeSymbolic(""), equalTo(false));
    }

    @Test
    public void isMissing() {
        assertThat(VariantType.isMissing("."), equalTo(true));
    }

    @Test
    public void isMissingUpstreamDeletion() {
        assertThat(VariantType.isMissingUpstreamDeletion("*"), equalTo(true));
    }
}