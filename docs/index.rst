=====
Svart
=====

**A library to ease representation of variants and performing genomic manipulations.**

Svart is a library for representing genomic variants and regions. It attempts to solve several common issues:

* Coordinate system off-by-one errors
* Representation of VCF small, structural and breakend variants with a consistent API
* Different variant trimming strategies

The library provides a consistent API for creating, manipulating and comparing variation of different types by providing default implementations and extensible base classes and interfaces along with immutable default implementations which developers can utilise to gain maximum utility from a minimum amount of code without having to address issues in bioinformatics which are a common source of duplicated code and frequently errors.

The code is completely free of external dependencies.


.. toctree::
   :maxdepth: 2
   :caption: Contents:

   explanations
   cookbook
   motivation
