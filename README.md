svart - (structural) variant tool
=
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.monarchinitiative.svart/svart/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.monarchinitiative.svart/svart)

Svart is a small library for representing genomic variants and regions. It attempts to solve several common issues:

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
variation such as Jannovar, Exomiser, Squirls, PBGA.

These projects are inter-dependent to some extent and all require some level of ability to represent and manipulate 
genomic variation.

Given the inter-dependency there is substantial copying/re-implementation in the code-bases of these projects of various 
core concepts surrounding genomic variation. This library aims to provide a common set of interfaces and implementations 
which can be used to fulfill their collective use-cases and so reduce code duplication, bugs, and GC pressure due to 
object conversion between common types.

Design
==
There is a (mostly) compositional hierarchy of data types:

GenomicAssembly
-
 `GenomicAssembly` -has_some-> `Contig`

Region
-
 `Region` -has_a-> `CoordinateSystem`

 `Region` -has_start-> `Position`

 `Region` -has_end-> `Position`

GenomicRegion
-
 `GenomicRegion` -is_a-> `Region`

 `GenomicRegion` -has_a-> `Contig`

 `GenomicRegion` -has_a-> `Strand`

Position
-
 `Position` -has_a-> `ConfidenceInterval`

GenomicVariant
-
 `GenomicVariant` -is_a-> `GenomicRegion`

 `GenomicVariant` -has_a-> `VariantType`

BreakendVariant
-
 `BreakendVariant` -is_a-> `GenomicVariant`

 `BreakendVariant` -has_left-> `Breakend`

 `BreakendVariant` -has_right-> `Breakend`

 `Breakend` -is_a-> `GenomicRegion`


With this model, it is possible to express variation, using zero or one-based coordinates on a strand of a chromosome, for a 
given genome assembly in any coordinate system and easily manipulate and compare them without having to worry about what
strand or which coordinate system another `GenomicRegion` uses. Svart will do all this for you eliminating off-by-one
errors and providing an extensible model to plug into your code, so you can focus on the task at hand. It will enforce

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
// Load the Human GRCh37.13 assembly from a NCBI assembly report
GenomicAssembly b37 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
// alternatively you can use svart to download one using the GenBank or RefSeq accession:
//    GenomicAssembly grch37p13 = GenomicAssemblies.downloadAssembly("GCF_000001405.25");
// or use a default assembly:
//    GenomicAssembly grch37p13 = GenomicAssemblies.GRCh37p13();

// FGFR2 gene is located on chromosome 10 (CM000672.1): 123,237,848-123_357_972 reverse strand. (1-based, positive strand coordinates)
Contig chr10b37 = b37.contigByName("10");
GenomicRegion fgfr2Gene = GenomicRegion.of(chr10b37, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, 123_237_848, 123_357_972);
// 10	123256215	.	T	G  - a pathogenic missense variant (GRCh37 VCF coordinates - 1-based, positive strand)
Variant snv = Variant.of(chr10b37, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(123_256_215), "T", "G");
// Because svart knows about coordinate systems and strands it is possible to...
// keep the gene on the positive strand:
// GenomicRegion{contig=10, strand=+, coordinateSystem=FULLY_CLOSED, startPosition=123237848, endPosition=123357972}
assertThat(fgfr2Gene.contains(snv), equalTo(true));
// or use it on the negative strand:
// GenomicRegion{contig=10, strand=-, coordinateSystem=FULLY_CLOSED, startPosition=12176776, endPosition=12296900}
assertThat(fgfr2Gene.toNegativeStrand().contains(snv), equalTo(true));
```

In the above example the default implementations provided by the library were used. These are immutable classes which 
can be used compositionally. Alternatively, for users requiring extra functionality on top of the default implementations
there are several `Base` classes which can be extended - `BaseGenomicRegion`, `BaseVariant` and `BaseBreakendVariant`.

Representing variants
==
Svart models variants in a similar manner to VCF, however it attempts to provide a unified and consistent model using
the `Variant` interface. In VCF precise variants of known sequence (_sequence_ variants) described using CHR, ID, POS,
REF, ALT and also the _symbolic_ variants where the ALT field denotes the type in angle brackets e.g. `<INS>` for an
insertion along with a change length and end (the SVLEN and END fields in the INFO column). 

Internally Svart treats both the _sequence_ and _symbolic_ types in the same way modelling them as `GenomicRegion` so 
that all variants have a start and end (like BED), but also have a `VariantType` a `refLength` and a `changeLength` derived
from the REF/ALT sequences and END if available. The `Variant` interface contains static factory methods to help users
easily create variants, requiring them to consider important, but often implicit values for the `Strand` and `CoordinateSystem`
with which a variant is described.

_Sequence_ and _symbolic_ variants are always intra-chromosomal changes (i.e. on the same chromosome/contig) whereas
_breakend_ variants are somewhat anomalous as they describe the way one or two _different_ chromosomes are broken and 
re-arranged. These are handled by the `BreakendVariant` which is composed of a left and right `Breakend` derived from a
single VCF record (line). The full re-arrangement requires assembling the `Breakend`s together by matching their `id` 
with another `Breakend.mateId`. The VCF representation of breakends is quite esoteric and relies on a pattern in the
`ALT` column of a record to indicate the strand, chromosome, location and orientation of a mate end relative to the `REF`
position of the record e.g. `C[ctg2:5[` indicates `ctg2` position `5` (fully-closed coordinates) on the positive strand
is to the right of the reference allele `C` also on the positive strand. Svart provides a `VcfBreakendResolver` to handle the parsing and conversion of 
these into `BreakendVariant` instances. 

While svart is heavily influenced by VCF it does not provide a parser for VCF as this is better handled by the HTSJDK,
however much of the terminology and data is very close, for example the `ConfidenceInterval` class is used in conjunction
with the `Position` class to encapsulate the POS/CIPOS and END/CIEND fields from VCF. Svart does, however provide a
`VcfConverter` class which provides methods to easily convert VCF small, symbolic and breakend records into svart `Variant`
instances from, for example an HTSJDK `VariantContext`. The `VcfConverter` needs to be provided with the correct
`GenomicAssembly` for the VCF file and a `VariantTrimmer` to allow the user to specify how they wish any multi-allelic
sites to be trimmed. For example, this is how the VCF fields for a SNV called against GRCh37 would be converted:

```java
@Test
public void convert() {
    VcfConverter vcfConverter = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
    
    // non-symbolic alleles
    // CHR	POS	ID	REF	ALT
    // chr1	12345	rs123456	C	T	6	PASS	.
    Variant snv = vcfConverter.convert(vcfConverter.parseContig("chr1"), "rs123456", 12345, "C", "T");
    assertThat(snv, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), "C", "T")));

    // symbolic alleles
    // chr1	12345	.	C	<INS>	6	PASS	SVTYPE=INS;END=12345;SVLEN=200
    Variant ins = instance.convertSymbolic(instance.parseContig("chr1"),  "", Position.of(12345), Position.of(12345), "C", "<INS>", 200);
    assertThat(ins, equalTo(Variant.of(chr1, "rs123456", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12345), Position.of(12345), "C", "<INS>", 200)));

    // breakend variants
    // 1	12345	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
    Variant bnd = instance.convertBreakend(instance.parseContig("chr1"), "bnd_U", Position.of(12345), "C", "C[2:321682[", ConfidenceInterval.precise(), "bnd_V", "tra2");

    Breakend left = Breakend.of(chr1, "bnd_U", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(12346), Position.of(12345));
    Breakend right = Breakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(321682), Position.of(321681));
    assertThat(bnd, equalTo(Variant.of("tra2", left, right, "C", "")));
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
@Test
public void trimMultiAllelicSite() {
    // Given the VCF record:
    // chr1    225725424       .       CTT     C,CT    258.06  FreqFilter      AC=1,1;AF=0.500,0.500;AN=2;DP=7;ExcessHet=3.0103;FS=0.000;MAX_FREQ=3.1360424;MLEAC=1,1;MLEAF=0.500,0.500;MQ=60.00;QD=29.21;SOR=0.941    GT:AD:DP:GQ:PL  1/2:0,4,3:7:58:275,76,58,106,0,85
    Contig chr1 = TestContig.of(1, 249_250_621);
    VariantTrimmer leftShiftingTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

    Variant firstAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "C");
    assertThat(firstAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "C")));

    // throws an IllegalArgumentException without being trimmed first
    Variant secondAllele = trim(leftShiftingTrimmer, chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CTT", "CT");
    assertThat(secondAllele, equalTo(Variant.of(chr1, "", Strand.POSITIVE, oneBased(), Position.of(225_725_424), "CT", "C")));
}

private Variant trim(VariantTrimmer variantTrimmer, Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, String ref, String alt) {
    VariantPosition firstTrimmed = variantTrimmer.trim(strand, start.pos(), ref, alt);
    return Variant.of(contig, id, strand, coordinateSystem, start.withPos(firstTrimmed.start()), firstTrimmed.ref(), firstTrimmed.alt());
}
```

Svart does not however perform full variant normalisation as this should have been performed as part of the VCF creation. 
