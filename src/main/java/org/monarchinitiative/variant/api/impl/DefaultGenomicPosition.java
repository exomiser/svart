package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultGenomicPosition extends BaseGenomicPosition<DefaultGenomicPosition> {

    private DefaultGenomicPosition(Contig contig, Strand strand, Position position) {
        super(contig, strand, position);
    }

    public static DefaultGenomicPosition of(Contig contig, Strand strand, Position position) {
        return new DefaultGenomicPosition(contig, strand, position);
    }

    @Override
    protected DefaultGenomicPosition newPositionInstance(Contig contig, Strand strand, Position position) {
        return new DefaultGenomicPosition(contig, strand, position);
    }
}
