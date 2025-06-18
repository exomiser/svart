package org.monarchinitiative.svart.assembly;

import org.monarchinitiative.svart.Contig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultGenomicAssembly implements GenomicAssembly {

    private final String name;
    private final String organismName;
    private final String taxId;
    private final String submitter;
    private final String date;
    private final String genBankAccession;
    private final String refSeqAccession;
    private final Set<Contig> contigs;

    // derived fields
    private final List<Contig> contigsById;
    private final Map<String, Contig> contigsByName;

    private DefaultGenomicAssembly(Builder builder) {
        this.name = builder.name;
        this.organismName = builder.organismName;
        this.taxId = builder.taxId;
        this.submitter = builder.submitter;
        this.date = builder.date;
        this.genBankAccession = builder.genBankAccession;
        this.refSeqAccession = builder.refSeqAccession;
        List<Contig> sortedContigs = sortAndCheckContigs(builder.contigs);
        // Don't use a TreeSet here as TreeSet uses the comparable() method to check for equality, not hashCode() which means
        // that contigs.contains(x) will return true for any contig with the same id as x, even if they are otherwise
        // completely different contigs. For example a GRCh38 assembly will return true when asked if it contains chr 1
        // from GRCh37. This is not true of a HashSet implementation. We're using LinkedHashSet to maintain and predictably
        // return the contigs in their ordered form.
        this.contigs = Collections.unmodifiableSet(new LinkedHashSet<>(sortedContigs));
        this.contigsById = indexContigsById(sortedContigs);
        this.contigsByName = mapContigsByNames(sortedContigs);
    }

    private List<Contig> sortAndCheckContigs(Collection<Contig> contigs) {
        // remove Contig.unknown() if included as this isn't really a part of the GenomicAssembly
        List<Contig> sortedContigs = contigs.stream()
                .filter(contig -> !Contig.unknown().equals(contig))
                .sorted()
                .distinct()
                .toList();
        requireFirstIdNotZero(sortedContigs);
        requireUniqueSequentialIdentifiers(sortedContigs);
        return sortedContigs;
    }

    /**
     * In case of alternative implementations of {@link Contig} ensure that no zero ids exist
     */
    private void requireFirstIdNotZero(List<Contig> sortedContigs) {
        if (sortedContigs.isEmpty()) {
            return;
        }
        Contig first = sortedContigs.get(0);
        if (first.id() == 0) {
            throw new IllegalArgumentException("Illegal contig id. 0 is reserved for the unknown contig");
        }
    }

    /**
     * Checks the contigs provided have unique and sequentially-ordered numerical ids, starting from 1
     */
    private void requireUniqueSequentialIdentifiers(Collection<Contig> contigs) {
        int expectedIndex = 1;
        for (Contig contig : contigs) {
            if (contig.id() != expectedIndex) {
                throw new IllegalStateException("Expected contig id of " + expectedIndex + " but was " + contig.id() + " when checking " + contig);
            }
            expectedIndex++;
        }
    }

    /**
     * creates a new list of contigs with the first (index 0) element being the Contig.unknown(). This is to enable
     * direct lookups in the GenomicAssembly.contigById() method
     */
    private List<Contig> indexContigsById(Collection<Contig> contigs) {
        Contig[] contigsById = new Contig[contigs.size() + 1];
        contigsById[0] = Contig.unknown();
        for (Contig contig : contigs) {
            contigsById[contig.id()] = contig;
        }
        return List.of(contigsById);
    }

    private Map<String, Contig> mapContigsByNames(Collection<Contig> contigs) {
        Map<String, Contig> contigsByIdentifier = new ConcurrentHashMap<>();
        for (Contig contig : contigs) {
            putIfNotNa(contigsByIdentifier, contig, contig.name());
            putIfNotNa(contigsByIdentifier, contig, contig.ucscName());
            if (contig.sequenceRole() == SequenceRole.ASSEMBLED_MOLECULE) {
                // only add the assembled molecules here as otherwise other patch/alt scaffolds etc. will have the same
                // value. Note, this may or may not be the same as 'name'.
                putIfNotNa(contigsByIdentifier, contig, contig.assignedMolecule());
            }
            putIfNotNa(contigsByIdentifier, contig, contig.genBankAccession());
            putIfNotNa(contigsByIdentifier, contig, contig.refSeqAccession());
        }
        return contigsByIdentifier;
    }

    private void putIfNotNa(Map<String, Contig> contigsByIdentifier, Contig contig, String name) {
        if (!"na".equals(name) && !name.isEmpty()) {
            contigsByIdentifier.put(name, contig);
        }
    }

    //    GRCh38.p12
    public String name() {
        return name;
    }

    public String organismName() {
        return organismName;
    }

    public String taxId() {
        return taxId;
    }

    public String submitter() {
        return submitter;
    }

    public String date() {
        return date;
    }

    public String genBankAccession() {
        return genBankAccession;
    }

    public String refSeqAccession() {
        return refSeqAccession;
    }

    public Set<Contig> contigs() {
        return contigs;
    }

    public Contig contigById(int contigId) {
        if (contigId < 0 || contigId > contigsById.size() - 1) {
            return Contig.unknown();
        }
        return contigsById.get(contigId);
    }

    public Contig contigByName(String contigName) {
        return contigsByName.getOrDefault(contigName, Contig.unknown());
    }

    @Override
    public boolean containsContig(Contig contig) {
        return contigs.contains(contig);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGenomicAssembly that = (DefaultGenomicAssembly) o;
        return name.equals(that.name) && organismName.equals(that.organismName) && taxId.equals(that.taxId) && submitter.equals(that.submitter) && date.equals(that.date) && genBankAccession.equals(that.genBankAccession) && refSeqAccession.equals(that.refSeqAccession) && contigs.equals(that.contigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, organismName, taxId, submitter, date, genBankAccession, refSeqAccession, contigs);
    }

    @Override
    public String toString() {
        return "GenomicAssembly{" +
                "name='" + name + '\'' +
                ", organismName='" + organismName + '\'' +
                ", taxId='" + taxId + '\'' +
                ", submitter='" + submitter + '\'' +
                ", date='" + date + '\'' +
                ", genBankAccession='" + genBankAccession + '\'' +
                ", refSeqAccession='" + refSeqAccession + '\'' +
                ", contigs=" + contigs +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name = "";
        private String organismName = "";
        private String taxId = "";
        private String submitter = "";
        private String date = "";
        private String genBankAccession = "";
        private String refSeqAccession = "";
        private Collection<Contig> contigs = List.of();

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public Builder organismName(String organismName) {
            this.organismName = Objects.requireNonNull(organismName);
            return this;
        }

        public Builder taxId(String taxId) {
            this.taxId = Objects.requireNonNull(taxId);
            return this;
        }

        public Builder submitter(String submitter) {
            this.submitter = Objects.requireNonNull(submitter);
            return this;
        }

        public Builder date(String date) {
            this.date = Objects.requireNonNull(date);
            return this;
        }

        public Builder genBankAccession(String genBankAccession) {
            this.genBankAccession = Objects.requireNonNull(genBankAccession);
            return this;
        }

        public Builder refSeqAccession(String refSeqAccession) {
            this.refSeqAccession = Objects.requireNonNull(refSeqAccession);
            return this;
        }

        public Builder contigs(Collection<Contig> contigs) {
            this.contigs = Objects.requireNonNull(contigs);
            return this;
        }

        public DefaultGenomicAssembly build() {
            return new DefaultGenomicAssembly(this);
        }
    }

}
