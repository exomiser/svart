package org.monarchinitiative.svart;

class IdCache {

    private static final String MISSING = ".";
    private static final String EMPTY = "";

    private IdCache() {
    }

    /**
     * Returns a cached empty ("") or missing value (".") instance. Nulls will return an empty value. Other
     * identifiers are returned as input.
     *
     * @param id An identifier string.
     * @return A cached "" or "." instance or the original input value
     */
    static String cacheId(String id) {
        if (id == null || id.isEmpty()) {
            return EMPTY;
        }
        if (id.length() == 1 && MISSING.equals(id)) {
            return MISSING;
        }
        return id;
    }
}
