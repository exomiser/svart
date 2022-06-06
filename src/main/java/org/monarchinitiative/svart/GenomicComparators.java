package org.monarchinitiative.svart;

import java.util.Comparator;

/**
 * Helper class for comparisons
 */
class GenomicComparators {

    enum CoordinatesNaturalOrderComparator implements Comparator<Coordinates>{
        INSTANCE;

        @Override
        public int compare(Coordinates o1, Coordinates o2) {
            return Coordinates.compare(o1, o2);
        }
    }

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

    enum GenomicVariantNaturalOrderComparator implements Comparator<GenomicVariant> {
        INSTANCE;

        @Override
        public int compare(GenomicVariant o1, GenomicVariant o2) {
            return GenomicVariant.compare(o1, o2);
        }
    }
}
