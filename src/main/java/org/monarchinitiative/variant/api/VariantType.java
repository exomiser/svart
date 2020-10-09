package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum VariantType {

    // default unrecognised type
    UNKNOWN,

    // encompasses all 'non-structural' variants, i.e. SNV, MNV, INDEL < 50 bases
    // n.b. there are differences in definitions of how big a structural variant is ranging from
    // >= 50 (An integrated map of structural variation in 2,504 human genomes https://www.nature.com/articles/nature15394,
    //   A structural variation reference for medical and population genetics https://www.nature.com/articles/s41586-020-2287-8)
    // >150 (HTSJDK VariantContext.MAX_ALLELE_SIZE_FOR_NON_SV)
    // >=1000 (Jannovar VariantAnnotator)
    SNV,
    MNV,    // a multi-nucleotide variation
    INDEL, // non-symbolic and < 999 bases TODO: what cutoff? or remove in favour of INS or DEL
    // start, end, length
    // pos

    SYMBOLIC,
    // VCF standard reserved values for structural variants
    DEL,
    //DEL:ME
    DEL_ME(DEL),
    //DEL:ME:ALU
    DEL_ME_ALU(DEL, DEL_ME),
    DEL_ME_LINE1(DEL, DEL_ME),
    DEL_ME_SVA(DEL, DEL_ME),
    DEL_ME_HERV(DEL, DEL_ME),

    INS,
    INS_ME(INS),
    INS_ME_ALU(INS, INS_ME),
    INS_ME_LINE1(INS, INS_ME),
    INS_ME_SVA(INS, INS_ME),
    INS_ME_HERV(INS, INS_ME),

    DUP,
    //DUP:TANDEM
    DUP_TANDEM(DUP),
    //DUP:INV-BEFORE
    DUP_INV_BEFORE(DUP),
    //DUP:INV-AFTER
    DUP_INV_AFTER(DUP),

    INV,
    CNV,
    BND,

    // Non-canonical types used by other progs,

    // Canvas CNV types see: https://github.com/Illumina/canvas/wiki#output
    // These are found in the ID field: Canvas:GAIN,  Canvas:LOSS,  Canvas:LOH,  Canvas:COMPLEX
    // the SVTYPE=CNV
    CNV_GAIN(CNV),
    CNV_LOSS(CNV),
    CNV_LOH(CNV),
    CNV_COMPLEX(CNV),

    //STR - Short Tandem Repeat from ExpansionHunter
    STR,
    //TRA - Translocation from Sniffles
    TRA;

    private final VariantType baseType;
    private final VariantType subType;

    VariantType() {
        this.baseType = this;
        this.subType = this;
    }

    VariantType(VariantType parent) {
        this.baseType = parent;
        this.subType = this;
    }

    VariantType(VariantType baseType, VariantType subType) {
        this.baseType = baseType;
        this.subType = subType;
    }

    public static VariantType parseValue(String value) {
        String stripped = trimAngleBrackets(Objects.requireNonNull(value));
        // ExpansionHunter formats ShortTandemRepeats with the number of repeats like this: <STR56>
        if (stripped.startsWith("STR")) {
            return STR;
        }
        switch (stripped) {
            case "SNP":
            case "SNV":
                return SNV;
            case "MNP":
            case "MNV":
                return MNV;
            case "DEL":
                return DEL;
            case "INS":
                return INS;
            case "DUP":
                return DUP;
            case "INV":
                return INV;
            case "CNV":
                return CNV;
            case "BND":
                return BND;
            // STR is not part of the formal spec, but is output by ExpansionHunter
            case "STR":
                return STR;
            case "TRA":
                return TRA;

            //extended DEL types
            case "DEL:ME":
                return DEL_ME;
            case "DEL:ME:ALU":
                return DEL_ME_ALU;
            case "DEL:ME:LINE1":
                return DEL_ME_LINE1;
            case "DEL:ME:SVA":
                return DEL_ME_SVA;
            case "DEL:ME:HERV":
                return DEL_ME_HERV;

            //extended INS types
            case "INS:ME":
                return INS_ME;
            case "INS:ME:ALU":
                return INS_ME_ALU;
            case "INS:ME:LINE1":
                return INS_ME_LINE1;
            case "INS:ME:SVA":
                return INS_ME_SVA;
            case "INS:ME:HERV":
                return INS_ME_HERV;

            //extended DUP types
            case "DUP:TANDEM":
                return DUP_TANDEM;
            case "DUP:INV-BEFORE":
                return DUP_INV_BEFORE;
            case "DUP:INV-AFTER":
                return DUP_INV_AFTER;
        }
        // in other cases where we don't recognise the exact type, use the closest type or sub-type
        // given VCF doesn't precisely define these, these are a safer bet that just UNKNOWN
        if (stripped.startsWith("DEL:ME")) {
            return DEL_ME;
        }
        if (stripped.startsWith("DEL")) {
            return DEL;
        }
        if (stripped.startsWith("INS:ME")) {
            return INS_ME;
        }
        if (stripped.startsWith("DUP:TANDEM")) {
            return DUP_TANDEM;
        }
        if (stripped.startsWith("DUP")) {
            return DUP;
        }
        if (stripped.startsWith("CNV")) {
            return CNV;
        }
        if (stripped.startsWith("BND")) {
            return BND;
        }
        if (isSymbolic(value)) {
            return SYMBOLIC;
        }
        return UNKNOWN;
    }

    public static VariantType parseAllele(String ref, String alt) {
        if (isSymbolic(ref, alt)) {
            return parseValue(alt);
        }
        if (ref.length() == alt.length()) {
            if (alt.length() == 1) {
                return VariantType.SNV;
            }
            return VariantType.MNV;
        }
        return ref.length() < alt.length() ? VariantType.INS : VariantType.DEL;
    }

    private static boolean isSymbolic(String ref, String alt) {
        // The VCF spec only mentions alt alleles as having symbolic characters, so check these first then check the ref
        // just in case.
        return isSymbolic(alt) || isSymbolic(ref);
    }

    private static boolean isSymbolic(String allele) {
        // shamelessly copied from HTSJDK Allele via Jannovar
        if (allele.length() <= 1) {
            return false;
        }
        return (allele.charAt(0) == '<' || allele.charAt(allele.length() - 1) == '>') || // symbolic or large insertion
                (allele.charAt(0) == '.' || allele.charAt(allele.length() - 1) == '.') || // single breakend
                (allele.contains("[") || allele.contains("]")); // mated breakend
    }

    private static String trimAngleBrackets(String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public VariantType getBaseType() {
        return baseType;
    }

    public VariantType getSubType() {
        return subType;
    }

}
