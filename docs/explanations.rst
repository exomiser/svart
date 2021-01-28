.. _rstexplanations:

===============
Building blocks
===============

This document describes concepts and building blocks of the Svart library. We start by explaining how the library models
the genomic assembly and contigs. Then we explain concepts that are necessary for defining a (genomic) region, including
coordinate system and strand. Finally, we describe how the library models genomic variation.

Genomic Assembly
~~~~~~~~~~~~~~~~

Svart adopts NCBI's representation of genomic assembly, where the assembly is a collection of
contigs with some metadata.

There are three ways how to create a ``GenomicAssembly``:

Built in Genomic Assemblies
###########################

Svart ships with several built-in genomic assemblies for *Homo sapiens* and *Mus musculus* organisms:

.. TODO - Make this a table?

* ``GRCh37.p13``
* ``GRCh38.p13``
* ``GRCm39``
* ``GRCm38.p6``

Let's get our hands on the ``GRCh38.p13`` assembly::

  GenomicAssembly grch38p13 = GenomicAssemblies.GRCh38p13();

  System.out.println(grch38p13.name() + ':' + grch38p13.organismName());
  // prints `GRCh38.p13:Homo sapiens (human)`

NCBI assembly report file
#########################

Svart provides a parser that reads NCBI's genomic assembly report file and returns the corresponding ``GenomicAssembly``.

Let's read  for `GRCm38.p6 assembly report`_  from NCBI's FTP site using the corresponding RefSeq assembly accession ``GCF_000001635.26``::

  GenomicAssembly grcm38p6 = GenomicAssemblies.downloadAssembly("GCF_000001635.26");

  System.out.println(grcm38p6.name() + ':' + grcm38p6.organismName());
  // prints `GRCm38.p6:Mus musculus (house mouse)`

From scratch
############

Let's create an example genomic assembly with metadata corresponding to ``GRCh38.p13``. The assembly contains the first
2 contigs of the assembly::

  List<Contig> contigs = List.of(
    Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 248_956_422, "CM000663.2", "NC_000001.11", "chr1"),
    Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, "2", AssignedMoleculeType.CHROMOSOME, 248_956_422, "CM000664.2", "NC_000002.12", "chr2"));
  GenomicAssembly assembly = GenomicAssembly.of("GRCh38.p13", "Homo sapiens (human)", "9606",
        "Genome Reference Consortium", "2019-02-28",
        "GCA_000001405.28", "GCF_000001405.39",
        contigs);

  System.out.println(assembly.name() + ':' + assembly.organismName());
  // prints `GRCh38.p13:Homo sapiens (human)`


Contig
~~~~~~

In Svart, contig is a linear

Coordinate Systems
~~~~~~~~~~~~~~~~~~

.. TODO - explain the contemporary coordinate systems used in genomics,
  - how this is different from another domains (e.g. joda-time) where only a single coordinate system is allowed,
  - difference between coordinates and positions,
  - why we define all 4 possible coordinate systems

Strand
~~~~~~

.. TODO - explain why we use 2 strands and not the strands of GFF format
  - methods of `Stranded<T>` that allow conversions of coordinates between strands
  - why strand of breakend variants cannot be changed

Genomic Region
~~~~~~~~~~~~~~

.. TODO -

Variant
~~~~~~~

.. _GRCm38.p6 assembly report: ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCA/000/001/405/GCA_000001405.14_GRCh37.p13/GCA_000001405.14_GRCh37.p13_assembly_report.txt