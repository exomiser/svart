package org.monarchinitiative.svart;

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

    String sequenceRole;

    SequenceRole(String sequenceRole) {
        this.sequenceRole = sequenceRole;
    }

    public static SequenceRole parseRole(String value) {
        for (SequenceRole role : SequenceRole.values()) {
            if (role.sequenceRole.equals(value)) {
                return role;
            }
        }
        return UNKNOWN;
    }
}
