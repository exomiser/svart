package org.monarchinitiative.variant.api;

import java.util.Comparator;

/**
 * Helper class for comparisons
 */
class GenomicComparators {

    enum GenomicRegionNaturalOrderComparator implements Comparator<GenomicRegion> {
        INSTANCE;

        @Override
        public int compare(GenomicRegion o1, GenomicRegion o2) {
            return GenomicRegion.compare(o1, o2);
        }
    }

    enum VariantNaturalOrderComparator implements Comparator<Variant> {
        INSTANCE;

        @Override
        public int compare(Variant o1, Variant o2) {
            return Variant.compare(o1, o2);
        }
    }

    enum GenomicPositionNaturalOrderComparator implements Comparator<GenomicPosition> {
        INSTANCE;

        @Override
        public int compare(GenomicPosition o1, GenomicPosition o2) {
            return GenomicPosition.compare(o1, o2);
        }
    }
}
