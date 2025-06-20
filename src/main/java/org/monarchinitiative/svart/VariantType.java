package org.monarchinitiative.svart;

import org.monarchinitiative.svart.sequence.NucleotideSeq;

import java.util.Objects;

/**
 * VariantType follows the VCF types for structural variants or child terms of
 * <a href="http://www.sequenceontology.org/browser/current_svn/term/SO:0001059">sequence_alteration</a> from the
 * Sequence Ontology (SO), when not specified in VCF. These mostly match-up well, however there are a few terms
 * for sequence variants (i.e. those with a known sequence) which do not have a VCF-specified type e.g. SNV, MNV, DELINS.
 * In these cases the SO definition is followed.
 *
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

    /**
     * SO:0001483 - SNVs are single nucleotide positions in genomic DNA at which different sequence alternatives exist.
     */
    SNV,

    /**
     * SO:0001013 - A multiple nucleotide polymorphism with alleles of common length > 1, for example AAA/TTT.
     */
    MNV,

    /**
     * SO:1000032 - A sequence alteration which included an insertion and a deletion, affecting 2 or more bases.
     */
    DELINS,

    SYMBOLIC,
    // VCF standard reserved values for structural variants
    /**
     * SO:0000159 - The point at which one or more contiguous nucleotides were excised.
     */
    DEL,
    //DEL:ME
    DEL_ME(DEL),
    //DEL:ME:ALU
    DEL_ME_ALU(DEL, DEL_ME),
    DEL_ME_LINE1(DEL, DEL_ME),
    DEL_ME_SVA(DEL, DEL_ME),
    DEL_ME_HERV(DEL, DEL_ME),

    /**
     * SO:0000667 - The sequence of one or more nucleotides added between two adjacent nucleotides in the sequence.
     */
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
    // VCF 4.4 addition for tandem repeat expansions
    // (https://samtools.github.io/hts-specs/VCFv4.4.pdf#subsection.5.7)
    CNV_TR(CNV),
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

    /**
     * Parses the type from the ALT allele.
     * @param alt
     * @return
     */
    public static VariantType parseType(String alt) {
        Objects.requireNonNull(alt);
        if (alt.isEmpty()) {
            return UNKNOWN;
        }
        String stripped = trimAngleBrackets(alt);
        switch (stripped) {
            case "SNP", "SNV":
                return SNV;
            case "MNP",  "MNV":
                return MNV;
            case "DEL":
                return DEL;
            case "INS":
                return INS;
            case "DELINS":
                return DELINS;
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

            // extended CNV types
            case "CNV:GAIN":
                return CNV_GAIN;
            case "CNV:LOSS":
                return CNV_LOSS;
            // VCF 4.4+ Tandem repeat
            case "CNV:TR":
                return CNV_TR;
            default:
                // fall-through
        }
        if (stripped.startsWith("BND")) {
            return BND;
        }
        if (isBreakend(stripped)) {
            return BND;
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
        // ExpansionHunter formats ShortTandemRepeats with the number of repeats like this: <STR56>
        if (stripped.startsWith("STR")) {
            return STR;
        }
        if (isSymbolic(alt)) {
            return SYMBOLIC;
        }
        return UNKNOWN;
    }

    /**
     * Returns the {@link VariantType} for the given ref/alt alleles. This method will determine whether the alleles
     * indicate small sequence variations such as SNP, MNV, INS, DEL or DELINS or if they indicate a large symbolic or
     * breakend allele. It is required that the input alleles conform to the VCF specification, although this method will
     * also handle fully-trimmed alleles where an INS or DEL contain an empty ref or alt allele. For small sequence variants,
     * this method will assign a DELINS type to variants which can be described as a deletion, followed by an insertion
     * event. For example A>GC is a deletion of the reference A followed by an insertion of a GC (a DELINS), whereas
     * A>AGC is an INS of bases GC and AGC>A is a DEL of bases GC.
     *
     * <p>Note that, balanced substitutions such as TG>CA or TGA>CGC are typed as MNV following the Sequence Ontology
     * definition <a href="http://www.sequenceontology.org/browser/current_svn/term/SO:0001013">SO:0001013</a>
     * "A multiple nucleotide polymorphism with alleles of common length > 1, for example AAA/TTT".
     * Similarly, DELINS also follows the <a href="http://www.sequenceontology.org/browser/current_svn/term/SO:1000032">SO:1000032</a>
     * definition "A sequence alteration which included an insertion and a deletion, affecting 2 or more bases".
     * The <a href="https://varnomen.hgvs.org/recommendations/DNA/variant/delins/">HGVS</a> definition of
     * DELINS - "changes involving two or more consecutive nucleotides are described as deletion/insertion (delins)
     * variants", would also include the MNV type.
     * </p>
     *
     * @param ref reference allele string e.g. [ATGCN]+
     * @param alt alternate allele string e.g. a symbolic allele, or a breakend replacement string, or match the regular
     *            expression^([ACGTNacgtn]+|\*|\.)$.
     * @return the {@link VariantType} calculated from the REF and ALT allele.
     */
    public static VariantType parseType(String ref, String alt) {
        if (isSymbolic(alt)) {
            return parseType(alt);
        }
        final int refLength = ref.length();
        final int altLength = alt.length();
        // SNV SO:0001483 - SNVs are single nucleotide positions in genomic DNA at which different sequence alternatives exist.
        if (refLength == altLength && refLength == 1) {
            return VariantType.SNV;
        }
        // MNV SO:0001013 - A multiple nucleotide polymorphism with alleles of common length > 1, for example AAA/TTT.
        if (refLength == altLength && refLength > 1) {
            // INV SO:1000036 - A continuous nucleotide sequence is inverted in the same position.
            if (isInversion(ref, alt)) {
                return VariantType.INV;
            }
            return VariantType.MNV;
        }
        // DEL SO:0000159 - The point at which one or more contiguous nucleotides were excised.
        // ATC>AT, AT>A or A> == DEL - handle untrimmed cases too
        // check start and end of ref in case of flipping to opposite strands e.g. AG>A becomes CT>T
        if (altLength < refLength && (ref.startsWith(alt) || ref.endsWith(alt))) {
            return DEL;
        }
        // INS SO:0000667 - The sequence of one or more nucleotides added between two adjacent nucleotides in the sequence.
        // >A or A>AT, AT>ATC == INS - handle untrimmed cases too
        // check start and end of alt in case of flipping to opposite strands e.g. A>AG becomes T>CT
        if (refLength < altLength && (alt.startsWith(ref) || alt.endsWith(ref))) {
            return INS;
        }
        // DELINS SO:1000032 - A sequence alteration which included an insertion and a deletion, affecting 2 or more bases.
        //  ATG>TC, AT>TCA  == DELINS (i.e. a deletion followed by an insertion)
        return DELINS;
    }

    /**
     * Requires both ref and alt are <b>of equal length</b>
     * @param ref
     * @param alt
     * @return
     */
    private static boolean isInversion(String ref, String alt) {
        // check first and last bases are rev complement before checking the entire sequence
        if (isReverseComplement(ref.charAt(0), alt.charAt(alt.length() -1)) && isReverseComplement(ref.charAt(ref.length() -1), alt.charAt(0))) {
            if (ref.length() == 2) {
                // Already checked the sequences are reverse complemented, although here AT -> AT, GC -> GC, so these are actually not variations!
                // Other dinucleotide combinations are true inversions GA -> TC, CT -> AG
                return true;
            }
            return NucleotideSeq.reverseComplement(ref).equalsIgnoreCase(alt);
        }
        return false;
    }

    private static boolean isReverseComplement(char c, char c1) {
        return switch (c) {
            case 'A', 'a' -> c1 == 'T' || c1 == 't';
            case 'T', 't' -> c1 == 'A' || c1 == 'a';
            case 'C', 'c' -> c1 == 'G' || c1 == 'g';
            case 'G', 'g' -> c1 == 'C' || c1 == 'c';
            case 'N', 'n' -> c1 == 'N' || c1 == 'n';
            default -> false;
        };
    }

    public static boolean isSymbolic(String allele) {
        return isLargeSymbolic(allele) || isBreakend(allele);
    }

    public static boolean isBreakend(String allele) {
        return isSingleBreakend(allele) || isMatedBreakend(allele);
    }

    public static boolean isLargeSymbolic(String allele) {
        return allele.length() > 1 && (allele.charAt(0) == '<' && allele.charAt(allele.length() - 1) == '>');
    }

    public static boolean isSingleBreakend(String allele) {
        return allele.length() > 1 && (allele.charAt(0) == '.' || allele.charAt(allele.length() - 1) == '.');
    }

    public static boolean isMatedBreakend(String allele) {
        return allele.length() > 1 && (allele.contains("[") || allele.contains("]"));
    }

    public static VariantType validateType(String ref, String alt, VariantType variantType) {
        VariantType expected = parseType(ref, alt);
        if (variantType == expected) {
            return variantType;
        }
        throw new IllegalArgumentException("Variant type " + variantType + " not consistent with variant " + ref + "-" + alt);
    }

    public static String requireNonSymbolic(String alt) {
        if (alt == null || isSymbolic(alt)) {
            throw new IllegalArgumentException("Illegal symbolic alt allele '" + alt + "'");
        }
        if (alt.contains(",")) {
            throw new IllegalArgumentException("Illegal multi-allelic alt allele '" + alt + "'");
        }
        return alt;
    }

    /**
     * Checks the input allele string is a symbolic allele where symbolic includes structural variants of the form <DEL>,
     * <INS> or uses breakend notation e.g. A[1:12345[ or A. or .A
     * @param alt an alt allele string
     * @return the input alt allele
     * @throws IllegalArgumentException for an incorrect alt allele string
     */
    public static String requireSymbolic(String alt) {
        if (alt == null || !isSymbolic(alt)) {
            throw new IllegalArgumentException("Illegal non-symbolic alt allele '" + alt + "'");
        }
        return alt;
    }

    public static String requireBreakend(String alt) {
        if (alt == null || !isBreakend(alt)) {
            throw new IllegalArgumentException("Illegal non-breakend alt allele '" + alt + "'");
        }
        return alt;
    }

    public static String requireNonBreakend(String alt) {
        if (alt == null || VariantType.isBreakend(alt)) {
            throw new IllegalArgumentException("Illegal breakend allele '" + alt + "'");
        }
        return alt;
    }

    /**
     * Returns true if the provided allele matches the string '*'. This is defined in VCF 4.3 as "allele missing due to
     * overlapping deletion"
     *
     * @param allele
     * @return true if the input allele equals '*'
     */
    public static boolean isMissingUpstreamDeletion(String allele) {
        return allele.equals("*");
    }

    /**
     * Returns true if the provided allele matches the string '.'. This is defined in VCF 4.3 as missing or "no  variant"
     *
     * @param allele
     * @return
     */
    public static boolean isMissing(String allele) {
        return allele.equals(".");
    }

    private static String trimAngleBrackets(String value) {
        if (value.startsWith("<") && value.endsWith(">")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public VariantType baseType() {
        return baseType;
    }

    public VariantType subType() {
        return subType;
    }

}
