Monarch Variant API
=

Motivation
==
This is intended to be used as a standard library for various Monarch and Monarch-related projects involving genomic 
variation such as Jannovar, Exomiser, Squirls, PBGA.

These projects are inter-dependent to some extent and all require some level of ability to represent and manipulate 
genomic variation.

Given the inter-dependency there is substantial copying/re-implementation in the codebases of these projects of various 
core concepts surrounding genomic variation. This library aims to provide a common set of interfaces and implementations 
which can be used to fulfill their collective use-cases and so reduce code duplication, bugs, and GC pressure due to 
object conversion between common types.

Design
==
There is a (mostly) compositional hierarchy of data types:

 `GenomicAssembly` -has_some-> `GenomicContig`
 
 `GenomicPosition` -has_a-> `GenomicContig`

 `GenomicPosition` -has_a-> `position`
 
 `GenomicPosition` -has_a-> `CoordinateSystem`
 
 `GenomicPosition` -has_a-> `Strand`
 
 `GenomicRegion` -has_start-> `GenomicPosition`

 `GenomicRegion` -has_end-> `GenomicPosition`

 `GenomicVariation` -is_a-> `GenomicRegion`
 
 `GenomicVariation` -has_a-> `VariantType`


So with this is possible to express a variation, using zero or one-based coordinates on a stand of a chromosome for a 
given genome assembly.

e.g. 
```
Chromosome 10 (CM000672.1): 123,237,848-123,353,481 reverse strand. (1-based coordinates for the FGFR2 gene on GRCh37)
Chromosome 10 (CM000672.2): 121,478,332-121,598,458 reverse strand. (1-based coordinates for the FGFR2 gene on GRCh38)
```

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
- Jannovar will be able to function with only minor changes to replace a few core components API with the grrl API
  - Jannovar `VariantDescription` should be replaceable with `GenomicVariation` 
- Exomiser will be able to function by extending the GenomicVariation interface 
