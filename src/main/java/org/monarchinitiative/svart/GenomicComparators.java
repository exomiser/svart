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

    enum IntervalNaturalOrderComparator implements Comparator<Interval> {
        INSTANCE;

        @Override
        public int compare(Interval o1, Interval o2) {
            return Interval.compare(o1, o2);
        }
    }

    enum GenomicIntervalNaturalOrderComparator implements Comparator<GenomicInterval> {
        INSTANCE;

        @Override
        public int compare(GenomicInterval o1, GenomicInterval o2) {
            return GenomicInterval.compare(o1, o2);
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
