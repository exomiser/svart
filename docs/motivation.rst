.. _rstmotivation:

==========
Motivation
==========

This is intended to be used as a standard library for various Monarch and Monarch-related projects involving genomic
variation such as Jannovar, Exomiser, and Squirls.

These projects are inter-dependent to some extent and all require some level of ability to represent and manipulate
genomic variation.

Given the inter-dependency there is substantial copying/re-implementation in the code-bases of these projects
of various core concepts surrounding genomic variation. This library aims to provide a common set of interfaces
and implementations which can be used to fulfill their collective use-cases and so reduce code duplication, bugs,
and GC pressure due to object conversion between common types.

Success Metrics
~~~~~~~~~~~~~~~

* *Jannovar* will be able to function with only minor changes to replace a few core components API with the Svart elements.
  For example the Jannovar `VariantDescription` should be replaceable with `Variant`
* *Exomiser* will be able to function by implementing the `Variant` interface or extending the `BaseVariant` class
