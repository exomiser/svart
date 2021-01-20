svart - (structural) variant tool
=

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

 `GenomicAssembly` -has_some-> `Contig`

 `GenomicRegion` -has_a-> `Contig`
 `GenomicRegion` -has_a-> `Strand`
 `GenomicRegion` -has_a-> `CoordinateSystem`
 `GenomicRegion` -has_start-> `Position`
 `GenomicRegion` -has_end-> `Position`

 `Position` -has_a-> `ConfidenceInterval`

 `GenomicVariant` -is_a-> `GenomicRegion`
 `GenomicVariant` -has_a-> `VariantType`

 `BreakendVariant` -is_a-> `GenomicVariant`
 `BreakendVariant` -has_left-> `Breakend`
 `BreakendVariant` -has_right-> `Breakend`

 `Breakend` -is_a-> `GenomicRegion`


So with this is possible to express a variation, using zero or one-based coordinates on a stand of a chromosome for a 
given genome assembly.

e.g. 
```
Chromosome 10 (CM000672.1): 123,237,848-123,353,481 reverse strand. (1-based coordinates for the FGFR2 gene on GRCh37)
Chromosome 10 (CM000672.2): 121,478,332-121,598,458 reverse strand. (1-based coordinates for the FGFR2 gene on GRCh38)
```

example code

```java
// Load the Human GRCh37.13 assembly from a NCBI assembly report
GenomicAssembly b37 = GenomicAssemblyParser.parseAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
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
the `Variant` interface. In VCF precise variants of known sequence ('sequence' variants) described using CHR, ID, POS,
REF, ALT and also the 'symbolic' variants where the ALT field denotes the type in angle brackets e.g. `<INS>` for an
insertion along with a change length and end (the SVLEN and END fields in the INFO column). 

Internally Svart treats both the 'sequence' and 'symbolic' types in the same way modelling them as `GenomicRegion` so 
that all variants have a start and end (like BED), but also have a `VariantType` a `refLength` and a `changeLength` derived
from the REF/ALT sequences and END if available. The `Variant` interface contains static factory methods to help users
easily create variants, requiring them to consider important, but often implicit values for the `Strand` and `CoordinateSystem`
with which a variant is described.

'Sequence' and 'symbolic' variants are always intra-chromosomal changes (i.e. on the same chromosome/contig) whereas
breakend variants are somewhat anomalous as they describe the way one or two _different_ chromosomes are broken and 
re-arranged. These are handled by the `BreakendVariant` which is composed of a left and right `Breakend` derived from a
single VCF record (line). The full re-arrangement requires assembling the `Breakend`s together by matching their `id` 
with another `Breakend.mateId`. 

While svart is heavily influenced by VCF it does not provide a parser for VCF as this is better handled by the HTSJDK,
however much of the terminology and data is very close, for example the `ConfidenceInterval` class is used in conjunction
with the `Position` class to encapsulate the POS/CIPOS and END/CIEND fields from VCF.

Multi-allelic sites
===
Another source of errors and confusion for the unprepared are VCF multi-allelic sites where a single VCF record lists two
or more ALT alleles at a position. In order that the variants can be correctly represented they require 'trimming' to their
shortest form which, depending on the untrimmed variant can have a different start to what is displayed in the VCF record.

//TODO examples...

Again there are several, occasionally opposing, schools of thought on how best to trim a variant left, then right ('right shift')
or right, then left ('left shift'). The HGVS recommend right shifting, whereas VCF requires left shifting. This is further
complicated by requiring a 'padding' base, as VCF does for the reference allele, or removing this base. The svart API 
provides the `VariantTrimmer` class to enable users to perform this in a strand-safe way.

//TODO examples...

Svart does not however perform full variant normalisation as this should have been performed as part of the VCF creation. 

Thoughts
==
Interfaces are nice and all, but implementations of basic types might be more useful, only allowing an interface at the 
bottom-most class with no other dependencies e.g. `GenomicVariant`. This way all implementations will work out of the box 
by simply implementing the interface.

e.g. Jannovar has very nice functionality for flipping positions based on the strand, but is zero based where Exomiser 
has more variation-related functionailty relying in Jannovar for the annotation functionality, but using one-based VCF
coordinates. Converting between these has the posibility of an off-by one error or worse if the coordinates are reported 
on the opposite strand than expected. This library should:
 - Reduce off-by-one errors 
 - Enable applications use some of the core genome position functionality of Jannovar 
 - Not require any further external dependencies 
 
Plan
===

Not completely keen on the pure interface implementation - most of these classes only need to satisfy Jannovar to be 
useful to the other codebases.  
So how about providing GenomicAssembly, Contig, GenomicPosition, GenomicRegion, ConfidenceInterval, Strand, 
CordinateSystem as immutable implementations using Record in Java 17. The core of these implementations will use 
Jannovar-derived code.

Success Metrics
===
- Jannovar will be able to function with only minor changes to replace a few core components API with the svart API. For
  example the Jannovar `VariantDescription` should be replaceable with `Variant` 
- Exomiser will be able to function by implementing the `Variant` interface or extending the `BaseVariant` class.
