package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A {@link GenomicVariant} implementation which stores all strand, coordinates and allele information as a single long.
 * This only requires 32 bytes per object as opposed to about 40-56 for Sequence or SymbolicVariants. The latter will be
 * much larger on the heap as each allele requires another 24 bytes for the String instance and at least 24 bytes for the
 * underlying byte[]. All told a SequenceVariant requires at least an extra 4 objects totalling 160 bytes on the heap
 * per instance compared to a CompactSequenceVariant.
 * <p>
 * Performance hasn't been measured but appears to be at least as performant as the default implementation. The limitations
 * are that this class can only represent small, precise sequence variants with a maximum total length of 11 bases for
 * the sum of the ref and alt alleles. Given this will cover ~90% of most short-read variants, this class could provide
 * substantial memory savings.
 */
public record CompactSequenceVariant(Contig contig, String id, VariantType variantType, long bits) implements GenomicVariant, Comparable<GenomicVariant> {

    public static final int MAX_BASES = 11;
    // 64     32       28       24       22    (22 - refLen) (22 - (refLen + altLen)) therefore max 22 bits = 11 bases (ref + alt)
    // | start | refLen | altLen | refBits | altBits | strand | coordinateSystem
    // start is a positive int for zero-based coords or -ve int for one based coords meaning flipping 1st 32 bits will flip the coordinate system 0 <-> 1
    // therefore long < 0 ? ONE_BASED : ZERO_BASED

    private static final long START_OFFSET = Long.SIZE - 32L;
    private static final long ALLELE_LENGTH_FIELD_BITS = 4L;
    private static final long REF_LENGTH_OFFSET = START_OFFSET - ALLELE_LENGTH_FIELD_BITS;
    private static final long ALT_LENGTH_OFFSET = REF_LENGTH_OFFSET - ALLELE_LENGTH_FIELD_BITS;
    private static final long ALLELE_SEQ_FIELD_BITS = MAX_BASES * 2L;
    private static final long STRAND_OFFSET = 1L; // penultimate bit
    private static final long COORDINATE_SYSTEM_OFFSET = 0L; // last bit

    private static final String A = "A";
    private static final String T = "T";
    private static final String C = "C";
    private static final String G = "G";

    public static CompactSequenceVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
        return of(contig, "", strand, coordinates, ref, alt);
    }

    public static CompactSequenceVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        if (ref.length() != coordinates.length()) {
            throw new IllegalArgumentException("Ref allele length of " + ref.length() + " inconsistent with " + coordinates + " (length " + coordinates.length() + ") ref=" + ref + ", alt=" + alt);
        }
        return of(contig, id, strand, coordinates.coordinateSystem(), coordinates.start(), ref, alt);
    }

    public static CompactSequenceVariant of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        return of(contig, "", strand, coordinateSystem, start, ref, alt);
    }
    public static CompactSequenceVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        VariantType.requireNonSymbolic(alt);
        int end = GenomicVariant.calculateEnd(start, coordinateSystem, ref, alt);
        GenomicInterval.validateCoordinatesOnContig(contig, coordinateSystem, start, end);
        GenomicVariant.validateRefAllele(ref);
        GenomicVariant.validateAltAllele(alt);

        // check for nulls and then assert ref.length() + alt.length() <= 11 && ref & alt are ATGC only
        if (ref.length() + alt.length() > MAX_BASES) {
            throw new IllegalArgumentException("Maximum length of ref and alt alleles must be <= 11. Got " + (ref.length() + alt.length()) + ": ref=" + ref + ", alt=" + alt);
        }
        long bits = toBits(strand, coordinateSystem, start, ref, alt);
        VariantType variantType = VariantType.parseType(ref, alt);
        return new CompactSequenceVariant(contig, GenomicVariant.cacheId(id), variantType, bits);
    }

    public static boolean canBeCompactVariant(String ref, String alt) {
        if (ref.length() + alt.length() > MAX_BASES) {
            return false;
        }
        return isJustACGT(ref) && isJustACGT(alt);
//        return (ref.length() + alt.length()) <= MAX_BASES
//               && !VariantType.isSymbolic(ref)
//               && !VariantType.isSymbolic(alt)
//               && isNotMissingOrStar(alt)
//               && isNotN(ref)
//               && isNotN(alt);
    }

    private static boolean isJustACGT(String ref) {
        for (char c : ref.toCharArray()) {
             if (c != 'A' && c != 'C' && c != 'G' && c != 'T' && c != 'a' && c != 'c' && c != 'g' && c != 't') {
                 return false;
             }
        }
        return true;
    }

    private static boolean isNotN(String alt) {
        return !"N".equals(alt);
    }

    private static boolean isNotMissingOrStar(String allele) {
        return !".".equals(allele) && !"*".equals(allele);
    }

    private static long toBits(Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        // store start internally in zero-based coords
        long bits = (long) (coordinateSystem == CoordinateSystem.ONE_BASED ? start - 1 : start) << START_OFFSET;
        if (strand == Strand.POSITIVE) {
            bits |= (1L << STRAND_OFFSET);
        }
        if (coordinateSystem == CoordinateSystem.ONE_BASED) {
            bits |= (1L << COORDINATE_SYSTEM_OFFSET);
        }
        // given the remaining 30 bits we have 4 bits each to encode the ref and alt allele lengths and then a maximum
        // of 30 - 8 = 22 bits to encode the bases. Using a 2-bit encoding scheme per base, this gives us 11 bases for
        // the sum of the ref and alt alleles. e.g. a max 10 base insertion or deletion (VCF style) or an 11 base indel.
        long refLength = ref.length();
        bits |= (refLength << REF_LENGTH_OFFSET);
        //  4 bits = 8421 = max allele length 16
        long altLength = alt.length();
        bits |= (altLength << ALT_LENGTH_OFFSET);
        long refEncoded = encodeAllele(ref);
        bits |= (refEncoded << ALT_LENGTH_OFFSET - (refLength << 1));
        long altEncoded = encodeAllele(alt);
        bits |= (altEncoded << ALT_LENGTH_OFFSET - ((refLength + altLength) << 1));
        return bits;
    }

    private static long encodeAllele(String allele) {
        long encoded = 0L;
        int alleleLength = allele.length();
        for (int i = 0; i < alleleLength; i++) {
            byte enc = encodeBase(allele.charAt(i));
            encoded |= ((long) enc << ((alleleLength - 1 - i) << 1));
        }
        return encoded;
    }

    private static byte encodeBase(char c) {
        return switch (c) {
            case 'A', 'a' -> 0b00;
            case 'T', 't' -> 0b11;
            case 'C', 'c' -> 0b01;
            case 'G', 'g' -> 0b10;
            default -> throw new IllegalArgumentException("Unable to encode base " + c + " must be [AaTtGgCc]");
        };
    }

    private static final byte[] BASE_BYTES = {'A', 'C', 'G', 'T'};
    private byte decodeBase(byte base) {
        return BASE_BYTES[base];
    }

    private static final String[] BASE_STRINGS = {A, C, G, T};
    private String decodeBaseAsString(byte base) {
        // A = 0 (0b00), C = 1 (0b01), G = 2 (0b10), T = 3 (0b11)
        // this is 2* faster than using a switch statement
        return BASE_STRINGS[base];
    }

    private static final long ALLELE_LEN_MASK = 0b1111L;
    private static final long BASE_MASK = 0b11L;

    private int refLength() {
        return (int) ((bits >> REF_LENGTH_OFFSET) & ALLELE_LEN_MASK);
    }

    private int altLength() {
        return (int) ((bits >> ALT_LENGTH_OFFSET) & ALLELE_LEN_MASK);
    }

    /**
     * @return
     */
    @Override
    public String ref() {
        int alleleLength = refLength();
        return alleleLength == 0 ? "" : decodeAllele(alleleLength, (int) ALLELE_SEQ_FIELD_BITS);
    }

    /**
     * @return
     */
    @Override
    public String alt() {
        int alleleLength = altLength();
        return alleleLength == 0 ? "" : decodeAllele(alleleLength, (int) ALLELE_SEQ_FIELD_BITS - (refLength() << 1));
    }

    private String decodeAllele(int alleleLength, int offset) {
        // optimal path - no string allocation, otherwise do the whole thing and allocate a new string :'(
        return alleleLength == 1 ? decodeBaseAsString((byte) ((bits >> offset) & BASE_MASK)) : newAlleleString(alleleLength, offset);
    }

    private String newAlleleString(int alleleLength, int offset) {
        byte[] bases = new byte[alleleLength];
        for (int i = 0; i < alleleLength; i++) {
            bases[i] = decodeBase((byte) ((bits >> (offset - (i << 1))) & BASE_MASK));
        }
        return new String(bases);
    }

    /**
     * @return 
     */
    @Override
    public int length() {
        return refLength();
    }

    /**
     * @return 
     */
    @Override
    public boolean isPrecise() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public int changeLength() {
        return altLength() - refLength();
    }


    /**
     * @param other
     * @return
     */
    @Override
    public CompactSequenceVariant withStrand(Strand other) {
        if (this.strand() == other) {
            return this;
        }
        // flip start
        long oppositeStart = contig.length() - (long) end();
        long otherStrandBits = oppositeStart << START_OFFSET;
        // set strand
        if (other == Strand.POSITIVE) {
            otherStrandBits |= (1L << STRAND_OFFSET);
        }
        // set coordinate system bit if one-based
        if (coordinateSystem() == CoordinateSystem.ONE_BASED) {
            otherStrandBits |= (1L << COORDINATE_SYSTEM_OFFSET);
        }

        // no need to change ref or alt lengths
        int refLength = refLength();
        otherStrandBits |= (refLength << REF_LENGTH_OFFSET);
        int altLength = altLength();
        otherStrandBits |= (altLength << ALT_LENGTH_OFFSET);

        long refOffset = ALT_LENGTH_OFFSET - (refLength << 1);
        long refRevCompBits = reverseComplementAllele(refLength, refOffset);
        otherStrandBits |= refRevCompBits << refOffset;

        long altOffset = ALT_LENGTH_OFFSET - (refLength + altLength << 1);
        long altRevCompBits = reverseComplementAllele(altLength, altOffset);
        otherStrandBits |= altRevCompBits << altOffset;

        return new CompactSequenceVariant(contig, id, variantType, otherStrandBits);
    }

    private long reverseComplementAllele(int alleleLength, long offset) {
        long revComp = 0L;
        int alleleIndex = alleleLength - 1;
        long complementBits = ~bits;
        for (int i = alleleIndex; i >= 0; i--) {
            long complementIndex = (long) alleleIndex - i;
            long complementBase = complementBits >> offset + (complementIndex << 1) & BASE_MASK;
            int shiftIndex = i << 1;
            revComp |= complementBase << shiftIndex;
        }
        return revComp;
    }

    /**
     * @param coordinateSystem
     * @return
     */
    @Override
    public CompactSequenceVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return this;
        }
        long coordinateSystemBits = bits;
        // toggle coordinate system bit
        coordinateSystemBits ^= (1L << COORDINATE_SYSTEM_OFFSET);
        return new CompactSequenceVariant(contig, id, variantType, coordinateSystemBits);
    }

    /**
     * @return
     */
    @Override
    public Coordinates coordinates() {
        return Coordinates.of(coordinateSystem(), start(), end());
    }


    @Override
    public CoordinateSystem coordinateSystem() {
        return (bits & 1L << COORDINATE_SYSTEM_OFFSET) == 0 ? CoordinateSystem.ZERO_BASED : CoordinateSystem.ONE_BASED;
    }

    @Override
    public boolean isZeroBased() {
        return (bits & 1L << COORDINATE_SYSTEM_OFFSET) == 0;
    }

    @Override
    public boolean isOneBased() {
        return (bits & 1L << COORDINATE_SYSTEM_OFFSET) == 1;
    }

    /**
     * @return
     */
    @Override
    public int start() {
        // top 32 bits is a signed integer representing strand and start. negative strand coordinates are -ve
        return (int) ((bits >> START_OFFSET) + (bits & 1L << COORDINATE_SYSTEM_OFFSET));
    }

    // TODO: override all GenomicInterval and GenomicRegion methods where the default uses coordinates()

    /**
     * @return
     */
    @Override
    public int end() {
        return startZeroBased() + refLength();
    }

    /**
     * @return
     */
    @Override
    public int startStd() {
        return strand() == Strand.POSITIVE ? startZeroBased() : contig.length() - end();
    }

    @Override
    public int startZeroBased() {
        return (int) (bits >> START_OFFSET);
    }
    
    @Override
    public int startOneBased() {
        return startZeroBased() + 1;
    }

    /**
     * @param strand target {@link Strand} on which the start coordinate is required 
     * @return
     */
    @Override
    public int startOnStrand(Strand strand) {
        return this.strand() == strand ? start() : Coordinates.invertCoordinate(coordinateSystem(), contig, end());
    }

    /**
     * @return
     */
    @Override
    public int endStd() {
        return strand() == Strand.POSITIVE ? end() : contig.length() - startZeroBased();
    }

    /**
     * @param strand target {@link Strand} for which the end coordinate is required 
     * @return
     */
    @Override
    public int endOnStrand(Strand strand) {
        return this.strand() == strand ? end() : Coordinates.invertCoordinate(coordinateSystem(), contig, start());
    }

    /**
     * @return
     */
    @Override
    public Strand strand() {
        return (bits & 1L << STRAND_OFFSET) == 0 ? Strand.NEGATIVE : Strand.POSITIVE;
    }


    // A	B	A|B	A&B	A^B	~A
    // 0	0	0	0	0	1
    // 1	0	1	0	1	0
    // 0	1	1	0	1	1
    // 1	1	1	1	0	0
    // https://stackoverflow.com/questions/12015598/how-to-set-unset-a-bit-at-specific-position-of-a-long
    // To dynamically set at bit, use:
    //
    //   x |= (1 << y); // set the yth bit from the LSB (right)
    //
    //  (1 << y) shifts the ...001 y places left, so you can move the set bit y places.
    //
    // You can also set multiple bits at once:
    //
    //   x |= (1 << y) | (1 << z); // set the yth and zth bit from the LSB
    //
    // Or to unset:
    //
    //   x &= ~((1 << y) | (1 << z)); // unset yth and zth bit
    //
    // Or to toggle:
    //
    //   x ^= (1 << y) | (1 << z); // toggle yth and zth bit
    //
    //  Worth to mention how to check bit: right shift it and AND bit = (x >> y) & 1


    /**
     * @return
     */
    @Override
    public String mateId() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public String eventId() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public boolean isSymbolic() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean isBreakend() {
        return false;
    }

    /**
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contig, bits, variantType);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactSequenceVariant that = (CompactSequenceVariant) o;
        return contig.equals(that.contig()) && bits == that.bits && variantType == that.variantType;
    }

    @Override
    public String toString() {
        return "CompactSequenceVariant{" +
               "contig=" + contig().id() +
               ", id='" + id + '\'' +
               ", strand=" + strand() +
               ", coordinateSystem=" + coordinateSystem() +
               ", start=" + start() +
               ", end=" + end() +
               ", ref='" + ref() + '\'' +
               ", alt='" + alt() + '\'' +
               ", variantType=" + variantType +
               ", length=" + length() +
               ", changeLength=" + changeLength() +
               '}';
    }
}
