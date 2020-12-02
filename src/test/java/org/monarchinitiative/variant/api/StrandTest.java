package org.monarchinitiative.variant.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class StrandTest {

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, NEGATIVE",
            "NEGATIVE, POSITIVE",
            "UNKNOWN, UNKNOWN",
            "UNSTRANDED, UNSTRANDED",
    })
    public void opposite(Strand strand, Strand expected) {
        assertThat(strand.opposite(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, true",
            "NEGATIVE, false",
            "UNKNOWN, false",
            "UNSTRANDED, false",
    })
    public void isPositive(Strand strand, boolean expected) {
        assertThat(strand.isPositive(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, false",
            "NEGATIVE, true",
            "UNKNOWN, false",
            "UNSTRANDED, false",
    })
    public void isNegative(Strand strand, boolean expected) {
        assertThat(strand.isNegative(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "+, POSITIVE",
            "-, NEGATIVE",
            "., UNSTRANDED",
            "?, UNKNOWN",
            "*, UNKNOWN",
    })
    public void parseStrand(String value, Strand expected) {
        assertThat(Strand.parseStrand(value), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, true",
            "NEGATIVE, true",
            "UNKNOWN, false",
            "UNSTRANDED, false",
    })
    public void hasComplement(Strand strand, boolean expected) {
        assertThat(strand.hasComplement(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, NEGATIVE, true",
            "NEGATIVE, POSITIVE, true",
            "POSITIVE, POSITIVE, false",
            "NEGATIVE, NEGATIVE, false",
            "UNKNOWN, UNKNOWN, false",
            "UNSTRANDED, UNSTRANDED, false",
            "UNKNOWN, POSITIVE, false",
            "UNKNOWN, NEGATIVE, false",
            "UNSTRANDED, POSITIVE, false",
            "UNSTRANDED, NEGATIVE, false",
            "UNKNOWN, UNSTRANDED, false",
            "UNSTRANDED, UNKNOWN, false",
    })
    public void isComplementOf(Strand strand, Strand other,boolean expected) {
        assertThat(strand.isComplementOf(other), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, +",
            "NEGATIVE, -",
            "UNKNOWN, ?",
            "UNSTRANDED, .",
    })
    public void toString(Strand strand, String expected) {
        assertThat(strand.toString(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE,   POSITIVE,   true",
            "POSITIVE,   NEGATIVE,   true",
            "POSITIVE,   UNSTRANDED, true",
            "POSITIVE,   UNKNOWN,    true",

            "NEGATIVE,   POSITIVE,   true",
            "NEGATIVE,   NEGATIVE,   true",
            "NEGATIVE,   UNSTRANDED, true",
            "NEGATIVE,   UNKNOWN,    true",

            "UNSTRANDED, POSITIVE,   false",
            "UNSTRANDED, NEGATIVE,   false",
            "UNSTRANDED, UNSTRANDED, true",
            "UNSTRANDED, UNKNOWN,    true",

            "UNKNOWN,    POSITIVE,   false",
            "UNKNOWN,    NEGATIVE,   false",
            "UNKNOWN,    UNSTRANDED, false",
            "UNKNOWN,    UNKNOWN,    true"})
    public void conversionIsLegal(Strand source, Strand target, boolean expected) {
        assertThat(source.conversionIsLegal(target), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE,   POSITIVE,   false",
            "POSITIVE,   NEGATIVE,   true",
            "POSITIVE,   UNSTRANDED, true",
            "POSITIVE,   UNKNOWN,    true",

            "NEGATIVE,   POSITIVE,   true",
            "NEGATIVE,   NEGATIVE,   false",
            "NEGATIVE,   UNSTRANDED, true",
            "NEGATIVE,   UNKNOWN,    true",

            "UNSTRANDED, POSITIVE,   false",
            "UNSTRANDED, NEGATIVE,   false",
            "UNSTRANDED, UNSTRANDED, false",
            "UNSTRANDED, UNKNOWN,    true",

            "UNKNOWN,    POSITIVE,   false",
            "UNKNOWN,    NEGATIVE,   false",
            "UNKNOWN,    UNSTRANDED, false",
            "UNKNOWN,    UNKNOWN,    false"})
    public void needsConversion(Strand source, Strand target, boolean expected) {
        assertThat(source.needsConversion(target), equalTo(expected));
    }
}