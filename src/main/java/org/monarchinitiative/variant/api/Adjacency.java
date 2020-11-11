package org.monarchinitiative.variant.api;

/**
 * Adjacency ties together two breakends, as described in VCF specs.
 */
public interface Adjacency extends Breakended, Stranded<Adjacency> {

    @Override
    default Strand getStrand() {
        return getLeft().getStrand();
    }

    @Override
    default Adjacency toOppositeStrand() {
        return withStrand(getStrand().opposite());
    }
}
