svart - (structural) variant tool
=
[![Java CI with Maven](https://github.com/exomiser/svart/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/exomiser/svart/actions/workflows/maven.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.monarchinitiative.svart/svart/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.monarchinitiative.svart/svart)

Svart is a small library for representing genomic variants and regions. It attempts to solve several common issues:

- First-class genomic assemblies
- Coordinate system off-by-one errors
- Representation of VCF small, structural and breakend variants with a consistent API
- Different variant trimming strategies 

The library provides a consistent API for creating, manipulating and comparing variation of different types by providing
default implementations and extensible base classes and interfaces along with immutable default implementations which 
developers can utilise to gain maximum utility from a minimum amount of code without having to address issues in
bioinformatics which are a common source of duplicated code and frequently errors.

The code is completely free of external dependencies.

Motivation
==
This is intended to be used as a standard library for various Monarch and Monarch-related projects involving genomic 
variation such as [Exomiser](https://github.com/exomiser/Exomiser/),
[LIRICAL](https://github.com/TheJacksonLaboratory/LIRICAL), [Squirls](https://github.com/TheJacksonLaboratory/Squirls),
[SvAnna](https://github.com/TheJacksonLaboratory/SvAnna), to name a few.

These projects are inter-dependent to some extent and all require some level of ability to represent and manipulate 
genomic variation.

Given the inter-dependency there is substantial copying/re-implementation in the code-bases of these projects of various 
core concepts surrounding genomic variation. This library aims to provide a common set of interfaces and implementations 
which can be used to fulfill their collective use-cases and so reduce code duplication, bugs, and GC pressure due to 
object conversion between common types.

Design
==
There is a (mostly) compositional hierarchy of data types:

Contig
-
`Contig` -has_length-> `int`

`Contig` -has_identifier-> `int`

`Contig` -has_name-> `String`

GenomicAssembly
-
 `GenomicAssembly` -has_some-> `Contig`

CoordinateSystem
-
 `ONE_BASED` or `ZERO_BASED`

Coordinates
-
 `Coordinates` -has_a-> `CoordinateSystem`

 `Coordinates` -has_start-> `int`

 `Coordinates` -has_end-> `int`

Interval
-
 `Interval` -has_some-> `Coordinates`

Strand
-
 `POSITIVE` or `NEGATIVE`

GenomicInterval
-
 `GenomicInterval` -is_a-> `Interval`

 `GenomicInterval` -has_a-> `Contig`

 `GenomicInterval` -has_a-> `Strand`

GenomicRegion
-
 `GenomicRegion` -is_a-> `GenomicInterval`

 `GenomicRegion` -is-> `Transposable`

 `GenomicRegion` -is-> `Convertible`

GenomicVariant
-
 `GenomicVariant` -is_a-> `GenomicRegion`

 `GenomicVariant` -has_a-> `VariantType`

GenomicBreakendVariant
-
 `GenomicBreakendVariant` -is_a-> `GenomicVariant`

 `GenomicBreakendVariant` -has_left-> `GenomicBreakend`

 `GenomicBreakendVariant` -has_right-> `GenomicBreakend`

 `GenomicBreakend` -is_a-> `GenomicRegion`


With this model, it is possible to express variation, using zero or one-based coordinates on a strand of a chromosome, for a 
given genome assembly in any coordinate system and easily manipulate and compare them without having to worry about what
strand or which coordinate system another `GenomicRegion` uses. Svart will do all this for you eliminating off-by-one
errors and providing an extensible model to plug into your code, so you can focus on the task at hand. It will ensure a
region can be placed on a contig and will validate the input sequence to conform to VCF standards.

With the v2.0 API a new interface `GenomicInterval` has been added as the parent to `GenomicRegion`. The `GenomicInterval`
is untyped to ease implementation by other classes and in particular it omits the `Convertible` and `Transposable` traits
whilst implementing helper methods such as `overlapsWith(GenomicInterval other)` or `startOnStrandWithCoordinateSystem()`.
Consequently, for developers wondering which interface to implement, in general they should decide based on whether their
class will benefit from being `Convertible` or `Transposable`. Methods accepting a `GenomicX` should therefore prefer 
the `GenomicInterval` type for the widest utility.

Example
==

Modelling a gene as a region on a chromosome. Here we're using the gene [FGFR2](https://www.ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=ENSG00000066468;r=10:121478332-121598458).

```
Chromosome 10 (CM000672.1): 123,237,848-123,353,481 reverse strand. (1-based, positive strand coordinates for the FGFR2 gene on GRCh37)
Chromosome 10 (CM000672.2): 121,478,332-121,598,458 reverse strand. (1-based, positive strand coordinates for the FGFR2 gene on GRCh38)
```
The code below shows how to create a `GenomicAssembly`, create a `GenomicRegion` to represent the region of chromosome
10 to which the longest transcript expressing the FGFR2 can be aligned. We then create a `Variant` object representing a
SNV and check to see if this is located within the gene. FGFR2 is located on the negative strand, whereas variants from 
VCF are reported on the positive strand, so at first sight it might not appear that the variant is contained within the
gene. 

```java
class SnpAndGeneTest {

    public void checkGeneContainsVariant() {
        // Load the Human GRCh37.13 assembly from a NCBI assembly report
        GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
        // alternatively you can use svart to download one using the GenBank or RefSeq accession:
        //    GenomicAssembly grch37p13 = GenomicAssemblies.downloadAssembly("GCF_000001405.25");
        // or use a default assembly:
        //    GenomicAssembly grch37p13 = GenomicAssemblies.GRCh37p13();

        // FGFR2 gene is located on chromosome 10 (CM000672.1): 123,237,848-123_357_972 reverse strand. (1-based, positive strand coordinates)
        Contig chr10b37 = b37.contigByName("10");
        GenomicRegion fgfr2Gene = GenomicRegion.of(chr10b37, Strand.POSITIVE, CoordinateSystem.oneBased(), 123_237_848, 123_357_972);
        // 10	123256215	.	T	G  - a pathogenic missense variant (GRCh37 VCF coordinates - 1-based, positive strand)
        GenomicVariant snv = GenomicVariant.of(chr10b37, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 123_256_215, "T", "G");
        // Because svart knows about coordinate systems and strands it is possible to...
        // keep the gene on the positive strand:
        // GenomicRegion{contig=10, strand=+, coordinateSystem=ONE_BASED, startPosition=123237848, endPosition=123357972}
        assertThat(fgfr2Gene.contains(snv), equalTo(true));
        // or use it on the negative strand:
        // GenomicRegion{contig=10, strand=-, coordinateSystem=ONE_BASED, startPosition=12176776, endPosition=12296900}
        assertThat(fgfr2Gene.toNegativeStrand().contains(snv), equalTo(true));
    }
}
```

In the above example the default implementations provided by the library were used. These are immutable classes which 
can be used compositionally. Alternatively, for users requiring extra functionality on top of the default implementations
there are several `Base` classes which can be extended - `BaseGenomicRegion`, `BaseVariant` and `BaseBreakendVariant`.

Representing variants - VCF
==
Svart models variants in a similar manner to [VCF](https://samtools.github.io/hts-specs/VCFv4.3.pdf), however it 
attempts to provide a unified and consistent model using the `GenomicVariant` interface.
In VCF precise variants of known sequence (_sequence_ variants) described using CHR, ID, POS, REF, ALT and also the
_symbolic_ variants where the ALT field denotes the type in angle brackets e.g. `<INS>` for an
insertion along with a change length and end (the SVLEN and END fields in the INFO column). 

Internally Svart treats both the _sequence_ and _symbolic_ types in the same way modelling them as `GenomicRegion` so 
that all variants have a start and end (like BED), but also have a `VariantType` a `refLength` and a `changeLength` derived
from the REF/ALT sequences and END if available. The `GenomicVariant` interface contains static factory methods to help users
easily create variants, requiring them to consider important, but often implicit values for the `Strand` and `CoordinateSystem`
with which a variant is described.

_Sequence_ and _symbolic_ variants are always intra-chromosomal changes (i.e. on the same chromosome/contig) whereas
_breakend_ variants are somewhat anomalous as they describe the way one or two _different_ chromosomes are broken and 
re-arranged. These are handled by the `GenomicBreakendVariant` which is composed of a left and right `GenomicBreakend` derived from a
single VCF record (line). The full re-arrangement requires assembling the `GenomicBreakend`s together by matching their `id` 
with another `GenomicBreakend.mateId`. The VCF representation of breakends is quite esoteric and relies on a pattern in the
`ALT` column of a record to indicate the strand, chromosome, location and orientation of a mate end relative to the `REF`
position of the record e.g. `C[ctg2:5[` indicates `ctg2` position `5` (fully-closed coordinates) on the positive strand
is to the right of the reference allele `C` also on the positive strand. Svart provides a `VcfBreakendResolver` to 
handle the parsing and conversion of these into `GenomicBreakendVariant` instances. 

While svart is heavily influenced by VCF it does not provide a parser for VCF as this is better handled by the HTSJDK,
however much of the terminology and data is very close, for example the `ConfidenceInterval` class is used in conjunction
with the `Coordinates` class to encapsulate the POS/CIPOS and END/CIEND fields from VCF. Svart does, however provide a
`VcfConverter` class which provides methods to easily convert VCF small, symbolic and breakend records into svart `GenomicVariant`
instances from, for example an HTSJDK `VariantContext`. The `VcfConverter` needs to be provided with the correct
`GenomicAssembly` for the VCF file and a `VariantTrimmer` to allow the user to specify how they wish any multi-allelic
sites to be trimmed. For example, this is how the VCF fields for a SNV called against GRCh37 would be converted:

```java
class ConvertVcfTest {
    @Test
    public void convert() {
        VcfConverter vcfConverter = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

        // non-symbolic alleles
        // CHR	POS	ID	REF	ALT
        // chr1	12345	rs123456	C	T	6	PASS	.
        GenomicVariant snv = vcfConverter.convert(vcfConverter.parseContig("chr1"), "rs123456", 12345, "C", "T");
        assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "C", "T")));

        // symbolic alleles
        // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
        GenomicVariant ins = vcfConverter.convertSymbolic(vcfConverter.parseContig("chr1"), "", 12345, 12345, "C", "<INS>", 200);
        assertThat(ins, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, 12345, "C", "<INS>", 200)));

        // breakend variants
        // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        GenomicVariant bnd = vcfConverter.convertBreakend(vcfConverter.parseContig("chr1"), "bnd_U", 12345, "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

        GenomicBreakend left = Breakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12346, 12345);
        GenomicBreakend right = Breakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 321682, 321681);
        assertThat(bnd, equalTo(Variant.of("tra2", left, right, "C", "")));
    }
}
```

Multi-allelic sites
===
Another source of errors and confusion for the unprepared are VCF multi-allelic sites where a single VCF record lists two
or more ALT alleles at a position. In order that the variants can be correctly represented they require 'trimming' to their
shortest form which, depending on the untrimmed variant can have a different start to what is displayed in the VCF record.

Again there are several, occasionally opposing, schools of thought on how best to trim a variant left, then right ('right shift')
or right, then left ('left shift'). The HGVS recommend right shifting, whereas VCF requires left shifting. This is further
complicated by requiring a 'padding' base, as VCF does for the reference allele, or removing this base. The svart API 
provides the `VariantTrimmer` class to enable users to perform this in a strand-safe way.

```java
class TrimMultipleAlleleTests {
    @Test
    public void trimMultiAllelicSite() {
        // Given the VCF record:
        // chr1    225725424       .       CTT     C,CT    258.06  FreqFilter      AC=1,1;AF=0.500,0.500;AN=2;DP=7;ExcessHet=3.0103;FS=0.000;MAX_FREQ=3.1360424;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=29.21;SOR=0.941    GT:AD:DP:GQ:PL  1/2:0,4,3:7:58:275,76,58,106,0,85
        Contig chr1 = TestContig.of(1, 249_250_621);
        VariantTrimmer leftShiftingTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

        GenomicVariant firstAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "C");
        assertThat(firstAllele, equalTo(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "C")));

        GenomicVariant secondAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CTT", "CT");
        assertThat(secondAllele, equalTo(GenomicVariant.of(chr1, "", Strand.POSITIVE, CoordinateSystem.oneBased(), 225_725_424, "CT", "C")));
    }

    private GenomicVariant trim(VariantTrimmer variantTrimmer, Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, String ref, String alt) {
        VariantPosition trimmed = variantTrimmer.trim(strand, start, ref, alt);
        return GenomicVariant.of(contig, id, strand, coordinateSystem, trimmed.start(), trimmed.ref(), trimmed.alt());
    }
}
```

Svart does not however perform full variant normalisation as this should have been performed as part of the VCF creation. 

BED
==

What about [BED](https://grch37.ensembl.org/info/website/upload/bed.html)? BED provides coordinates of a genomic region in
_standard genomic coordinates_ i.e. left open intervals (a.k.a. zero-based) on the positive strand, but can optionally
indicate the strand on which the feature is located in column 6 with a `+` (forward/positive) or `-` (reverse/negative) character. The code
below shows how these records can be parsed into `GenomicRegion` instances.

```java
class BedTests {
    @Test
    public void parseGenomicRegionFromBedFile() {
        // Load the Human GRCh37.13 assembly
        GenomicAssembly b37 = GenomicAssembly.GRCh37p13();
        // BED uses left-open coordinates, with positions in standard genomic coordinates (i.e. positive strand), with
        // the 6th column indicating the strand. Using the example from - https://grch37.ensembl.org/info/website/upload/bed.html
        GenomicRegion pos1 = parseBedRecord(b37, "chr7\t127471196\t127472363\tPos1\t0\t+");
        GenomicRegion neg1 = parseBedRecord(b37, "chr7\t127475864\t127477031\tNeg1\t0\t-");

        assertThat(pos1.contigName(), equalTo("7"));
        assertThat(pos1.startOnStrand(Strand.POSITIVE), equalTo(127471196));
        assertThat(pos1.endOnStrand(Strand.POSITIVE), equalTo(127472363));
        assertThat(pos1.strand(), equalTo(Strand.POSITIVE));

        assertThat(neg1.contigName(), equalTo("7"));
        // a new instance on the positive strand can be created using neg1.toPositiveStrand(), however this will create
        // a new object which may not be wanted in the long term, so to avoid this you can use the start/endOnStrand method
        // which will efficiently calculate the result of neg1.withStrand(POSITIVE).start()/.end()
        assertThat(neg1.startOnStrand(Strand.POSITIVE), equalTo(127475864));
        assertThat(neg1.endOnStrand(Strand.POSITIVE), equalTo(127477031));
        assertThat(neg1.strand(), equalTo(Strand.NEGATIVE));
    }

    private GenomicRegion parseBedRecord(GenomicAssembly genomicAssembly, String bedRecord) {
        String[] fields = bedRecord.split("\t");
        Contig contig = genomicAssembly.contigByName(fields[0]);
        Strand strand = Strand.parseStrand(fields[5]);
        int start = strand == Strand.POSITIVE ? Integer.parseInt(fields[1]) : Coordinates.invertCoordinate(CoordinateSystem.zeroBased(), contig, Integer.parseInt(fields[2]));
        int end = strand == Strand.POSITIVE ? Integer.parseInt(fields[2]) : Coordinates.invertCoordinate(CoordinateSystem.zeroBased(), contig, Integer.parseInt(fields[1]));
        return GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(), start, end);
    }
}
```