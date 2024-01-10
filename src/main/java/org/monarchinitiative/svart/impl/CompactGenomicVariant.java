package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.*;

import java.util.Objects;

/**
 * A {@link GenomicVariant} implementation which stores all strand, coordinates and allele information as a single long.
 * This only requires 32 bytes per object as opposed to about 56 for the {@link DefaultGenomicVariant}.
 * <p>
 * Performance hasn't been measured but appears to be at least as performant as the default implementation. The limitations
 * are that this class can only represent small, precise sequence variants with a maximum total length of 11 bases for
 * the sum of the ref and alt alleles. Given this will cover ~90% of most short-read variants, this class could provide
 * substantial memory savings.
 */
public class CompactGenomicVariant implements GenomicVariant {

    public static final int MAX_BASES = 11;
    // 64     32       31        27       23    (23 - refLen) (23 - (refLen + altLen)) therefore max 22 bits = 11 bases (ref + alt)
    // | start | strand | refLen | altLen | refBits | altBits |
    // start is a positive int for zero-based coords or -ve int for one based coords meaning flipping 1st 32 bits will flip the coordinate system 0 <-> 1
    // therefore long < 0 ? ONE_BASED : ZERO_BASED
    // strand flip can be a single bit flip then just reverse reading ref and alt complements with a 2-bit window. e.g.
    //    POS ref = reflen++, alt = altLen++
    //    NEG ref = ^altLen--, alt = refLen--
    // alternative is to shift the refLen >> 4, altLen << 4 and the shift refBits >> altLen and altBits << refLen and flip

    private final Contig contig;
    private final String id;
    private final VariantType variantType;
    private final long bits;

    private CompactGenomicVariant(Contig contig, String id, VariantType variantType, long bits) {
        this.contig = contig;
        this.id = id;
        this.variantType = variantType;
        this.bits = bits;
    }

    public static CompactGenomicVariant of(Contig contig, Strand strand, Coordinates coordinates, String ref, String alt) {
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
        // check for nulls and then assert ref.length() + alt.length() <= 11 && ref & alt are ATGC only
        return of(contig, "", strand, coordinates.coordinateSystem(), coordinates.start(), ref, alt);
    }

    public static CompactGenomicVariant of(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt) {
        GenomicInterval.validateCoordinatesOnContig(contig, coordinates);
        // check for nulls and then assert ref.length() + alt.length() <= 11 && ref & alt are ATGC only
        return of(contig, id, strand, coordinates.coordinateSystem(), coordinates.start(), ref, alt);
    }

    public static CompactGenomicVariant of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
       // check for nulls and then assert ref.length() + alt.length() <= 11 && ref & alt are ATGC only
        if (ref.length() + alt.length() > MAX_BASES) {
            throw new IllegalArgumentException("Maximum length of ref and alt alleles must be <= 11. Got " + (ref.length() + alt.length()) + ": ref=" + ref + ", alt=" + alt);
        }
        long bits = toBits(strand, coordinateSystem, start, ref, alt);
        VariantType variantType = VariantType.parseType(ref, alt);
        return new CompactGenomicVariant(contig, id, variantType, bits);
    }

    private static long toBits(Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        long bits = (long) start << 32;
        if (coordinateSystem == CoordinateSystem.ONE_BASED) {
            // if store start as a +ve int for zero_based then when doing toOneBased only a ~bits is required to change sign and add 1
            bits = -bits;

//            // toOneBased(): flips bits in range
//            long firstWordMask = WORD_MASK << 32;
//            long lastWordMask  = WORD_MASK >>> -0;
//            System.out.println(Long.toBinaryString(firstWordMask));
//            System.out.println(Long.toBinaryString(lastWordMask));
//            System.out.println(Long.toBinaryString(firstWordMask & lastWordMask));
////            long oneBasedBitsFlipped = zeroBasedBits ^ (firstWordMask & lastWordMask);
//            long START_MASK = WORD_MASK << 32;
//            long oneBasedBitsFlipped = zeroBasedBits ^ START_MASK;
//            System.out.println(Long.toBinaryString(zeroBasedBits));
//            System.out.println(Long.toBinaryString(oneBasedBitsFlipped));
//            System.out.println(Long.toBinaryString(oneBasedBitsFlipped ^ firstWordMask));
//
//            System.out.println("start zero based=" + (int) (zeroBasedBits < 0 ? -(zeroBasedBits >> 32) : zeroBasedBits >> 32));
//            System.out.println("strand zero based=" + ((zeroBasedBits & 1L << 31) != 0 ? Strand.POSITIVE : Strand.NEGATIVE));
//
//            System.out.println("start to one based=" + (int) (oneBasedBitsFlipped < 0 ? -(oneBasedBitsFlipped >> 32) : oneBasedBitsFlipped >> 32));
//            System.out.println("strand to one based=" + ((oneBasedBitsFlipped & 1L << 31) != 0 ? Strand.POSITIVE : Strand.NEGATIVE));
        }
        if (strand == Strand.POSITIVE) {
            bits |= (1L << 31); // 33rd bit from left
        }
        // given the remaining 30 bits we have 4 bits each to encode the ref and alt allele lengths and then a maximum
        // of 30 - 8 = 22 bits to encode the bases. Using a 2-bit encoding scheme per base, this gives us 11 bases for
        // the sum of the ref and alt alleles. e.g. a max 10 base insertion or deletion (VCF style) or an 11 base indel.
        long refLength = ref.length();
        bits |= (refLength << 27);
        //  4 bits = 8421 = max allele length 16
        long altLength = alt.length();
        bits |= (altLength << 23);
        long refEncoded = encodeAllele(ref);
        bits |= (refEncoded << 23 - (refLength << 1));
        long altEncoded = encodeAllele(alt);
        bits |= (altEncoded << 23 - ((refLength + altLength) << 1));
        return bits;
    }

    private static long encodeAllele(String allele) {
        long encoded = 0L;
        int alleleLength = allele.length();
        for (int i = 0; i < alleleLength; i++) {
            byte enc = encodeBase(allele.charAt(i));
//            System.out.println("encoded " + allele.charAt(i) + "=" + Long.toBinaryString(enc));
            encoded |= ((long) enc << ((alleleLength - 1 - i) << 1));
//            System.out.println("encoded " + Long.toBinaryString(encoded));
        }
        return encoded;
    }

    private static byte encodeBase(char c) {
        return switch (c) {
            case 'A', 'a' -> 0b00;
            case 'T', 't' -> 0b11;
            case 'G', 'g' -> 0b01;
            case 'C', 'c' -> 0b10;
            default -> throw new IllegalStateException("Unable to encode base " + c + " must be [AaTtGgCc]");
        };
    }

    private byte decodeBase(byte base) {
        return switch (base) {
            case 0b00 -> 'A';
            case 0b11 -> 'T';
            case 0b01 -> 'G';
            case 0b10 -> 'C';
            default -> throw new IllegalStateException("Unable to decode base " + base + " must be [AaTtGgCc]");
        };
    }

    private byte decodeBaseComplement(byte base) {
        return switch (base) {
            case 0b00 -> 0b11;
            case 0b11 -> 0b00;
            case 0b01 -> 0b10;
            case 0b10 -> 0b01;
            default -> throw new IllegalStateException("Unable to decode base " + base + " must be [AaTtGgCc]");
        };
    }



    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public String id() {
        return id;
    }

    private static final long WORD_MASK = 0xffffffffffffffffL; // -1L
    private static final long REF_LEN_MASK = 0b0000_0000_0000_0000_0000_0000_0000_0000_0111_1000_0000_0000_0000_0000_0000_0000;
    private static final long ALT_LEN_MASK = 0b0000_0000_0000_0000_0000_0000_0000_0000_0000_0111_1000_0000_0000_0000_0000_0000;
    private static final long BASE_MASK = 0b11L;

    private int refLength() {
        return (int) (bits & REF_LEN_MASK) >> 27;
    }

    private int altLength() {
        return (int) (bits & ALT_LEN_MASK) >> 23;
    }

    /**
     * @return
     */
    @Override
    public String ref() {
        int alleleLength = refLength();
        return alleleLength == 0 ? "" : decodeAllele(alleleLength, 21);
    }

    /**
     * @return
     */
    @Override
    public String alt() {
        int alleleLength = altLength();
        return alleleLength == 0 ? "" : decodeAllele(alleleLength, 21 - (refLength() << 1));
    }

    private String decodeAllele(int alleleLength, int offset) {
        byte[] bases = new byte[alleleLength];
        for (int i = 0; i < alleleLength; i++) {
            long base = (bits >> (offset - (i << 1))) & BASE_MASK;
            byte decoded = decodeBase((byte) base);
//            System.out.println("decoded " + Long.toBinaryString(base) + "=" + (char) decoded);
            bases[i] = decoded;
        }
        return new String(bases);
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
    public GenomicVariant withStrand(Strand other) {
        if (this.strand() == other) {
            return this;
        }
        // flip start, maintaining coordinateSystem
        boolean isOneBased = bits < 0;
//        long startZeroBased = (bits < 0 ? ~(bits >> 32) : bits >> 32);
        long oppositeStart = contig.length() - (long) end();
        if (isOneBased) {
            oppositeStart = -(oppositeStart + 1);
        }
        long otherStrandBits = oppositeStart << 32;

        // set strand bit if positive
        if (strand() == Strand.NEGATIVE) {
            otherStrandBits |= (1L << 31);
        }

        // no need to change ref or alt lengths
        int refLength = refLength();
        otherStrandBits |= (refLength << 27);
        int altLength = altLength();
        otherStrandBits |= (altLength << 23);

        long refRevCompBits = reverseComplementAllele(refLength, (23 - (refLength << 1)));
        otherStrandBits |= refRevCompBits << 23 - (refLength << 1);

        long altRevCompBits = reverseComplementAllele(altLength, (23 - (refLength + altLength << 1)));
        otherStrandBits |= altRevCompBits << (23 - (refLength + altLength << 1));

        return new CompactGenomicVariant(contig, id, variantType, otherStrandBits);
    }

    private long reverseComplementAllele(int alleleLength, int offset) {
//        System.out.println("Allele length=" + alleleLength + ", offset=" + offset);
        long revComp = 0L;
        int alleleIndex = alleleLength - 1;
        for (int i = alleleIndex; i >= 0; i--) {
            int complementIndex = alleleIndex - i;
            long base = bits >> offset + (complementIndex << 1) & BASE_MASK;
            long complement = decodeBaseComplement((byte) base);
//            System.out.println(i + " " + (char) decodeBase((byte) base) + "=" + Long.toBinaryString(base) + "|" + (char) decodeBase((byte) complement) + "=" + Long.toBinaryString(complement) + " " + complementIndex);
            int shiftIndex = i << 1;
            revComp |= complement << shiftIndex;
        }
        return revComp;
    }

    // A	B	A|B	A&B	A^B	~A
    // 0	0	0	0	0	1
    // 1	0	1	0	1	0
    // 0	1	1	0	1	1
    // 1	1	1	1	0	0

    /**
     * @param coordinateSystem
     * @return
     */
    @Override
    public GenomicVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
        if (this.coordinateSystem() == coordinateSystem) {
            return this;
        }
        long coordinateSystemBits = bits ^ 0xffffffff00000000L;
        return new CompactGenomicVariant(contig, id, variantType, coordinateSystemBits);
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
        // top 32 bits is a signed integer representing coordinate system and start. zero-based coordinates are +ve
        // two's compliment still has leftmost bit as 1 to make entire long negative
        return bits < 0 ? CoordinateSystem.ONE_BASED : CoordinateSystem.ZERO_BASED;
    }

    @Override
    public boolean isZeroBased() {
        return bits >= 0;
    }

    @Override
    public boolean isOneBased() {
        return bits < 0;
    }

    /**
     * @return
     */
    @Override
    public int start() {
        // top 32 bits is a signed integer representing coordinate system and start. zero-based coordinates are +ve
        return (int) (bits < 0 ? -(bits >> 32) : bits >> 32);
    }

    /**
     * @return
     */
    @Override
    public int end() {
        return (int) zeroBasedStart() + refLength();
    }

    private long zeroBasedStart() {
        return bits < 0 ? ~(bits >> 32) : bits >> 32;
    }

    /**
     * @return
     */
    @Override
    public Strand strand() {
        return (bits & 1L << 31) == 0 ? Strand.NEGATIVE : Strand.POSITIVE;
    }

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
//    @Override
//    public GenomicVariant toZeroBased() {
//        return GenomicVariant.super.toZeroBased();
//    }
//
//    /**
//     * @return
//     */
//    @Override
//    public GenomicVariant toOneBased() {
//        return GenomicVariant.super.toOneBased();
//    }

    /**
     * @return
     */
    @Override
    public GenomicVariant toOppositeStrand() {
        return GenomicVariant.super.toOppositeStrand();
    }

    /**
     * @return
     */
    @Override
    public VariantType variantType() {
        return variantType;
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


    @Override
    public int hashCode() {
        return Objects.hash(contig, bits, variantType);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactGenomicVariant that = (CompactGenomicVariant) o;
        return contig.equals(that.contig()) && bits == that.bits && variantType == that.variantType;
    }

    @Override
    public String toString() {
        return "CompactGenomicVariant{" +
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
