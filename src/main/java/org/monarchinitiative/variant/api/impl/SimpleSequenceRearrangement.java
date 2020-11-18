package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Adjacency;
import org.monarchinitiative.variant.api.SequenceRearrangement;
import org.monarchinitiative.variant.api.Strand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class SimpleSequenceRearrangement implements SequenceRearrangement {

    private final List<Adjacency> adjacencies;

    private SimpleSequenceRearrangement(List<Adjacency> adjacencies) {
        if (adjacencies.isEmpty()) {
            throw new IllegalArgumentException("Adjacency list cannot be empty");
        }
        this.adjacencies = adjacencies;
    }

    public static SimpleSequenceRearrangement of(Adjacency... adjacencies) {
        return new SimpleSequenceRearrangement(Arrays.asList(adjacencies));
    }

    public static SimpleSequenceRearrangement of(List<Adjacency> adjacencies) {
        return new SimpleSequenceRearrangement(adjacencies);
    }

    @Override
    public List<Adjacency> adjacencies() {
        return adjacencies;
    }

    @Override
    public Strand strand() {
        return leftmostStrand();
    }

    @Override
    public SequenceRearrangement withStrand(Strand strand) {
        if (adjacencies.isEmpty() || adjacencies.get(0).left().strand().equals(strand)) {
            return this;
        } else {
            // reverse order of adjacencies, while also flipping the adjacencies to the opposite strand
            List<Adjacency> reversed = new ArrayList<>(adjacencies.size());
            for (int i = adjacencies.size() - 1; i >= 0; i--) {

                reversed.add(adjacencies.get(i).toOppositeStrand());
            }
            return new SimpleSequenceRearrangement(reversed);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSequenceRearrangement that = (SimpleSequenceRearrangement) o;
        return Objects.equals(adjacencies, that.adjacencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjacencies);
    }

    @Override
    public String toString() {
        return "[" + adjacencies + "]";
    }
}
