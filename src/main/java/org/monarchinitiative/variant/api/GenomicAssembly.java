/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.variant.api;

import java.util.SortedSet;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicAssembly {

//    GRCh38.p12
    public String name();

    public String organismName();

    public String taxId();

    public String submitter();

    public String date();

    public String genBankAccession();

    public String refSeqAccession();

    public SortedSet<Contig> contigs();

    public Contig contigById(int contigId);

    public Contig contigByName(String contigName);

//# Assembly name:  GRCh38.p13
//# Description:    Genome Reference Consortium Human Build 38 patch release 13 (GRCh38.p13)
//# Organism name:  Homo sapiens (human)
//# Taxid:          9606
//# BioProject:     PRJNA31257
//# Submitter:      Genome Reference Consortium
//# Date:           2019-02-28
//# Assembly type:  haploid-with-alt-loci
//# Release type:   patch
//# Assembly level: Chromosome
//# Genome representation: full
//# RefSeq category: Reference Genome
//# GenBank assembly accession: GCA_000001405.28
//# RefSeq assembly accession: GCF_000001405.39
//# RefSeq assembly and GenBank assemblies identical: no

}
