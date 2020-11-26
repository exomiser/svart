package org.monarchinitiative.variant.api;

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
            "<DEL>, DEL",
            "<INS:ME:HERV>, INS_ME_HERV",
            "<INS:ME:ALU>, INS_ME_ALU",
            "<ILLEGAL:VALUE>, SYMBOLIC"
    })
    void testParseAngleBracketValue(String input, VariantType expected) {
        assertThat(VariantType.parseType(input), equalTo(expected));
    }

    @Test
    void testParseStrippedValue() {
        assertThat(VariantType.parseType("DEL"), equalTo(VariantType.DEL));
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

    @Test
    void parseTypeSymbolic() {
        assertThat(VariantType.parseType("A", "<INS>"), equalTo(VariantType.INS));
    }

    @Test
    void parseTypeSnv() {
        assertThat(VariantType.parseType("A", "T"), equalTo(VariantType.SNV));
    }

    @Test
    void parseTypeMnv() {
        assertThat(VariantType.parseType("ATG", "TCA"), equalTo(VariantType.MNV));
    }

    @Test
    void parseTypeInsertion() {
        assertThat(VariantType.parseType("A", "TC"), equalTo(VariantType.INS));
    }

    @Test
    void parseTypeDeletion() {
        assertThat(VariantType.parseType("AT", "C"), equalTo(VariantType.DEL));
    }

    @Test
    void parseTypeRearrangement() {
        assertThat(VariantType.parseType("A", "C[2:12345["), equalTo(VariantType.BND));
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
}