package org.monarchinitiative.variant.api;

/**
 * Enum representing the 'sequence-role' of a {@link Contig} in a {@link GenomicAssembly}
 */
public enum SequenceRole {
    ASSEMBLED_MOLECULE,
    UNLOCALIZED_SCAFFOLD,
    UNPLACED_SCAFFOLD,
    FIX_PATCH,
    NOVEL_PATCH,
    ALT_SCAFFOLD,
    UNKNOWN
}
