package org.monarchinitiative.svart.assembly;

import org.monarchinitiative.svart.Contig;

/**
 * Enum representing the 'sequence-role' of a {@link Contig} in a {@link GenomicAssembly}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum SequenceRole {
    ASSEMBLED_MOLECULE("assembled-molecule"),
    UNLOCALIZED_SCAFFOLD("unlocalized-scaffold"),
    UNPLACED_SCAFFOLD("unplaced-scaffold"),
    FIX_PATCH("fix-patch"),
    NOVEL_PATCH("novel-patch"),
    ALT_SCAFFOLD("alt-scaffold"),
    UNKNOWN("unknown");

    private final String role;

    SequenceRole(String role) {
        this.role = role;
    }

    public static SequenceRole parseRole(String value) {
        for (SequenceRole sequenceRole : SequenceRole.values()) {
            if (sequenceRole.role.equals(value)) {
                return sequenceRole;
            }
        }
        return UNKNOWN;
    }
}
