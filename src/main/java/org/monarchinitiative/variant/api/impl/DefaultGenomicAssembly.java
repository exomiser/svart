package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.Contig;
import org.monarchinitiative.variant.api.GenomicAssembly;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class DefaultGenomicAssembly implements GenomicAssembly {

    private final String name;
    private final String organismName;
    private final String taxId;
    private final String submitter;
    private final String date;
    private final String genBankAccession;
    private final String refSeqAccession;
    private final SortedSet<Contig> contigs;

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
        this.contigs = Collections.unmodifiableSortedSet(new TreeSet<>(builder.contigs));

        contigsById = new ArrayList<>(contigs);
        contigsByName = contigs.stream().collect(toMap(Contig::name, Function.identity()));
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

    public SortedSet<Contig> contigs() {
        return contigs;
    }

    public Contig contigById(int contigId) {
        if (contigId <= contigsById.size() - 1) {
            return contigsById.get(contigId);
        }
        return Contig.unknown();
    }

    public Contig contigByName(String contigName) {
        return contigsByName.getOrDefault(contigName, Contig.unknown());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGenomicAssembly that = (DefaultGenomicAssembly) o;
        return name.equals(that.name) && organismName.equals(that.organismName) && taxId.equals(that.taxId) && submitter.equals(that.submitter) && date.equals(that.date) && genBankAccession.equals(that.genBankAccession) && refSeqAccession.equals(that.refSeqAccession) && contigs.equals(that.contigs) && contigsById.equals(that.contigsById) && contigsByName.equals(that.contigsByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, organismName, taxId, submitter, date, genBankAccession, refSeqAccession, contigs, contigsById, contigsByName);
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
                ", contigsById=" + contigsById +
                ", contigsByName=" + contigsByName +
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
        private Collection<Contig> contigs = new ArrayList<>();

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
