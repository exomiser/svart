Changelog
=

2.0.0
==

- Simplified `CoordinateSystem` to just `ONE_BASED`
  and `ZERO_BASED`. ([issue #58](https://github.com/exomiser/svart/issues/58))
- Removed redundant `Position` class.
- Removed redundant `Bound` class. ([issue #60](https://github.com/exomiser/svart/issues/60))
- Moved `Position` and `Bound` semantics into `Coordinates` with new `Coordinates.of()`, `Coordinates.oneBased()`
  and `Coordinates.oneBased()` static constructor methods
- Changed behaviour of `Coordinates.overlap` such that zero-length intervals are not considered as overlapping if they
  touch/abut another coordinates boundaries. For example, an insertion directly 5' of a transcript start site will not
  be considered to overlap the start site.
- Variant, Breakend and BreakendVariant and their implementations have all had `Genomic` prepended to their names in
  order to try and minimise name clashes with uses in other
  libraries. ([issue #57](https://github.com/exomiser/svart/issues/57))
- Added `Transposable` and `Convertible` interfaces for use
  by `GenomicRegion`. ([issue #68](https://github.com/exomiser/svart/issues/68))
- Added new `GenomicInterval` interface for all non-transposable, non-convertible genomic interval functionality
- Renamed `Region` to `Interval`
- Added new `IntervalTree` and `GenomicIntervalIndex` utility classes for efficient searching of overlapping intervals.
- Updated `GenomicAssemblies` ergonomics for easier use by client
  code ([issue #66](https://github.com/exomiser/svart/issues/66))
- Added new `GenomicAssemblies.T2T_CHM13v2_0()` method for those wanting to use the latest Human T2T-CHM13v2.0 assembly.
- Relaxed variant type requirements in various constructors and added default `GenomicVariant::mateId`
  and `GenomicVariant::eventId` to enable seamless and more intuitive handling of sequence, symbolic and breakend
  variants. ([issue #64](https://github.com/exomiser/svart/issues/64))
- Used [SO:0001059](http://www.sequenceontology.org/browser/current_svn/term/SO:0001059) as a base for
  defining `VariantType`, in particular SNV, MNV, DEL, INS. ([issue #67](https://github.com/exomiser/svart/issues/67))
- Added new `VariantType.DELINS` for indicating a deletion+insertion
  event. ([issue #69](https://github.com/exomiser/svart/issues/69))
- Updated `VariantType.MNV` detection and added ability to detect `VariantType.INV`.
- Added new methods `startStd()`, `startZeroBased()`, `startZeroBased(strand)`, and their "end" and "oneBased"
  combinations to the `GenomicInterval` class for more concise client code.
- Removed the test for `id` equality from BaseGenomicVariant for comparison only based on sequence change identity.
- Added a new `GenomicBreakendVariant.toSymbolicGenomicVariant()` method for easy conversion of `GenomicBreakendVariant`
  s back to symbolic `GenomicVariant` representation.
- Added new `VcfBreakendFormatter` functions, `makePosVcfField()` and `makeRefVcfField()`, to allow easy conversion
  of `GenomicBreakendVariant` `start`, `ref`, and `alt` values back to symbolic variant representation.
- Added extensive documentation to various classes to explain their relationships and usage contexts.
- Updated Java requirement to 17