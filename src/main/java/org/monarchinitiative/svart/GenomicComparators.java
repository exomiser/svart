package org.monarchinitiative.svart;

import java.util.Comparator;

/**
 * Helper class for comparisons
 */
class GenomicComparators {

    enum RegionNaturalOrderComparator implements Comparator<Region<?>> {
        INSTANCE;

        @Override
        public int compare(Region o1, Region o2) {
            return Region.compare(o1, o2);
        }
    }

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
}
