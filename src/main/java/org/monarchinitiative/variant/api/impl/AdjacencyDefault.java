package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Adjacency;
import org.monarchinitiative.variant.api.Breakend;
import org.monarchinitiative.variant.api.Strand;

import java.util.Objects;

/**
 * Default implementation of {@link Adjacency}
 */
public class AdjacencyDefault implements Adjacency {

    private final Breakend left, right;

    private AdjacencyDefault(Breakend left, Breakend right) {
        this.left = left;
        this.right = right;
    }

    public static AdjacencyDefault of(Breakend left, Breakend right) {
        return new AdjacencyDefault(left, right);
    }

    @Override
    public Adjacency withStrand(Strand strand) {
        if (getStrand() == strand) {
            return this;
        } else {
            switch (strand) {
                case POSITIVE:
                case NEGATIVE:
                    return new AdjacencyDefault(right.toOppositeStrand(), left.toOppositeStrand());
                default:
                    return this;
            }
        }
    }

    @Override
    public Breakend getLeft() {
        return left;
    }

    @Override
    public Breakend getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdjacencyDefault that = (AdjacencyDefault) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Adjacency{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
