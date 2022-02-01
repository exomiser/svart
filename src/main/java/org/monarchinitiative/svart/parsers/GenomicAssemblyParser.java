package org.monarchinitiative.svart.parsers;

import org.monarchinitiative.svart.assembly.AssignedMoleculeType;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.monarchinitiative.svart.impl.DefaultContig;
import org.monarchinitiative.svart.impl.DefaultGenomicAssembly;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles parsing of Genome Assembly report files from NCBI such as from here:
 * ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.25_GRCh37.p13/GCF_000001405.25_GRCh37.p13_assembly_report.txt
 *
 * These should be considered the source of truth for reference genomes.
 */
public class GenomicAssemblyParser {

    public static GenomicAssembly parseAssembly(Path assemblyReportPath) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(assemblyReportPath)) {
            return parseAssembly(bufferedReader);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse assembly file " + assemblyReportPath, e);
        }
    }

    public static GenomicAssembly parseAssembly(InputStream inputStream) {
        return parseAssembly(new InputStreamReader(inputStream));
    }

    public static GenomicAssembly parseAssembly(Reader reader) {
        List<String> headerLines = new ArrayList<>();
        List<String> assembledMoleculeLines = new ArrayList<>();
        List<String> otherMoleculeLines = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            for (String line; (line = bufferedReader.readLine()) != null;) {
                if (line.startsWith("#")) {
                    headerLines.add(line);
                }
                else if (line.contains("assembled-molecule")) {
                    assembledMoleculeLines.add(line);
                } else {
                    otherMoleculeLines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse assembly " +  e);
        }

        DefaultGenomicAssembly.Builder assemblyBuilder = DefaultGenomicAssembly.builder();
        parseHeaderLines(headerLines, assemblyBuilder);
        List<Contig> allContigs = parseContigs(assembledMoleculeLines, otherMoleculeLines);
        assemblyBuilder.contigs(allContigs);
        return assemblyBuilder.build();
    }

    private static List<Contig> parseContigs(List<String> assembledMoleculeLines, List<String> otherMoleculeLines) {
        List<Contig> assembledContigs = parseAssembledMolecules(assembledMoleculeLines);
        List<Contig> otherContigs = parseOtherMolecules(assembledContigs.size(), otherMoleculeLines);
        List<Contig> allContigs = new ArrayList<>(assembledContigs);
        allContigs.addAll(otherContigs);
        return allContigs;
    }

    private static List<Contig> parseAssembledMolecules(List<String> assembledMoleculeLines) {
        List<Contig> assembledMolContigs = new ArrayList<>();
        assembledMolContigs.add(Contig.unknown());
        int id = 1;
        for (String line : assembledMoleculeLines) {
            Contig contig = parseContig(id++, line);
            assembledMolContigs.add(contig);
        }
        return assembledMolContigs;
    }

    private static List<Contig> parseOtherMolecules(int assembledContigsSize, List<String> otherMolecules) {
        List<Contig> otherMolContigs = new ArrayList<>();
        int id = assembledContigsSize;
        for (String line : otherMolecules) {
            Contig contig = parseContig(id++, line);
            otherMolContigs.add(contig);
        }
        return otherMolContigs;
    }

    private static Contig parseContig(int id, String line) {
        String[] tokens = line.split("\t");
        String name = tokens[0];
        SequenceRole sequenceRole = SequenceRole.parseRole(tokens[1]);
        String assignedMolecule = tokens[2];
        AssignedMoleculeType assignedMoleculeType = AssignedMoleculeType.parseMoleculeType(tokens[3]);
        String genBankAccession = tokens[4];
        String refSeqAccession = tokens[6];
        int length = Integer.parseInt(tokens[8]);
        String ucscName = tokens[9];
        return DefaultContig.of(id, name, sequenceRole, assignedMolecule, assignedMoleculeType, length, genBankAccession, refSeqAccession, ucscName);
    }

    private static void parseHeaderLines(List<String> headerLines, DefaultGenomicAssembly.Builder assemblyBuilder) {
        for (String line : headerLines) {
            String[] kv = line.split(": ");
            if (kv.length != 2) {
                continue;
            }
            String value = kv[1].trim();
            String key = kv[0].replace("# ", "");
            switch (key) {
                case "Assembly name":
                    assemblyBuilder.name(value);
                case "Description":
                case "Organism name":
                    assemblyBuilder.organismName(value);
                case "Taxid":
                    assemblyBuilder.taxId(value);
                case "BioProject":
                case "Submitter":
                    assemblyBuilder.submitter(value);
                case "Date":
                    assemblyBuilder.date(value);
                case "Assembly type":
                case "Release type":
                case "Assembly level":
                case "Genome representation":
                case "RefSeq category":
                case "GenBank assembly accession":
                    assemblyBuilder.genBankAccession(value);
                case "RefSeq assembly accession":
                    assemblyBuilder.refSeqAccession(value);
                case "RefSeq assembly and GenBank assemblies identical":
            }
        }
    }


}
