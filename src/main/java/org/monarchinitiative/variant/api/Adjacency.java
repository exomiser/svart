package org.monarchinitiative.variant.api;

/**
 * Adjacency ties together two breakends, as described in VCF specs.
 *
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Adjacency extends Breakended, Stranded<Adjacency> {

    @Override
    default Strand strand() {
        return left().strand();
    }

    @Override
    Adjacency toOppositeStrand();
}
