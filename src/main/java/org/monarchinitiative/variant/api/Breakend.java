package org.monarchinitiative.variant.api;

public interface Breakend extends GenomicPosition {

    static Breakend unresolved() {
        return UnresolvedBreakend.instance();
    }

    /**
     * @return id corresponding to id of the record (e.g. VCF) this breakend was created from
     */
    String id();

    // override Stranded<T> methods from ChromosomalRegion in order to return the more specific Breakend type
    @Override
    Breakend withStrand(Strand strand);

    /**
     * Convert the breakend to opposite strand no matter what.
     */
    @Override
    default Breakend toOppositeStrand() {
        return withStrand(strand().opposite());
    }
}
