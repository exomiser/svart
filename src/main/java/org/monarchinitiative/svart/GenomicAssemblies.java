package org.monarchinitiative.svart;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading {@link GenomicAssembly} objects from NCBI assembly_report.txt files. This class also
 * provides some more contemporary reference assemblies for human and mouse for user convenience.
 */
public class GenomicAssemblies {

    private static final String ASSEMBLY_RESOURCE_PATH = "org/monarchinitiative/svart/assemblies/";

    private GenomicAssemblies() {
    }

    /**
     * Parses the file 'GCF_000001405.25_GRCh37.p13_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The human GRCh37.p13 {@link GenomicAssembly} also know as b37, hg19
     */
    public static GenomicAssembly GRCh37p13() {
        InputStream inputStream = getResourceAsStream(ASSEMBLY_RESOURCE_PATH + "GCF_000001405.25_GRCh37.p13_assembly_report.txt");
        return GenomicAssembly.readAssembly(inputStream);
    }

    /**
     * Parses the file 'GCF_000001405.39_GRCh38.p13_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The human GRCh38.p13 {@link GenomicAssembly} also know as b38, hg38
     */
    public static GenomicAssembly GRCh38p13() {
        InputStream inputStream = getResourceAsStream(ASSEMBLY_RESOURCE_PATH + "GCF_000001405.39_GRCh38.p13_assembly_report.txt");
        return GenomicAssembly.readAssembly(inputStream);
    }

    /**
     * Parses the file 'GCF_000001635.26_GRCm38.p6_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The mouse GRCm38.p6 {@link GenomicAssembly}
     */
    public static GenomicAssembly GRCm38p6() {
        InputStream inputStream = getResourceAsStream(ASSEMBLY_RESOURCE_PATH + "GCF_000001635.26_GRCm38.p6_assembly_report.txt");
        return GenomicAssembly.readAssembly(inputStream);
    }

    /**
     * Parses the file 'GCF_000001635.26_GRCm38.p6_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The mouse GRCm38.p6 {@link GenomicAssembly}
     */
    public static GenomicAssembly GRCm39() {
        InputStream inputStream = getResourceAsStream(ASSEMBLY_RESOURCE_PATH + "GCF_000001635.27_GRCm39_assembly_report.txt");
        return GenomicAssembly.readAssembly(inputStream);
    }

    private static InputStream getResourceAsStream(String path) {
        InputStream localResourceStream = GenomicAssemblies.class.getClassLoader().getResourceAsStream(path);
        if (localResourceStream != null) {
            return localResourceStream;
        }
        localResourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
        if (localResourceStream != null) {
            return localResourceStream;
        }
        throw new IllegalStateException("Unable to load resource " + path);
    }

    /**
     * Attempts to download the assembly_report.txt file for the given GenBank or RefSeq accession. Accessions must have
     * the pattern "GC[A,F])_[0-9]{9}\.[0-9]+" e.g. GCF_000001405.39
     *
     * This method will attempt to locate and download the file from the NCBI FTP servers from the URL <url>ftp://ftp.ncbi.nlm.nih.gov/genomes/all</url>
     *
     * @param accession A GenBank or RefSeq accession
     * @return a {@link GenomicAssembly} for the given accession.
     */
    public static GenomicAssembly downloadAssembly(String accession) {
        URL url = locateNcbiAssemblyReportUrl(accession);
        return downloadAssembly(url);
    }

    private static URL locateNcbiAssemblyReportUrl(String accession) {
        //  GCF_000001405.39 GCF_000001405.25
        // From ftp://ftp.ncbi.nlm.nih.gov/genomes/all/README.txt
        // Two directories under "all" are named for the accession prefix (GCA or GCF) and these directories
        // contain another three levels of directories named for digits 1-3, 4-6 & 7-9 of the assembly accession.
        // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/
        Pattern accessionPattern = Pattern.compile("(?<prefix>GC[A,F])_(?<accession>[0-9]{9})\\.(?<version>[0-9]+)");
        Matcher matcher = accessionPattern.matcher(accession);
        if (!matcher.matches() ) {
            throw new IllegalArgumentException("Invalid GenBank/RefSeq assembly accession " + accession);
        }
        String acc = matcher.group("accession");
        String assembliesParentDir = String.format("ftp://ftp.ncbi.nlm.nih.gov/genomes/all/%s/%s/%s/%s", matcher.group("prefix"), acc.substring(0, 3), acc.substring(3, 6), acc.substring(6, 9));
        String fullDirName = null;
        try {
            URL parentDirUrl = new URL(assembliesParentDir + ";type=d");
            URLConnection conn = parentDirUrl.openConnection();
            try(InputStream inputStream = conn.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.contains(accession)) {
                        // e.g. 405/GCA_000001405.14_GRCh37.p13 -> GCA_000001405.14_GRCh37.p13
                        fullDirName = line.replace(acc.substring(6, 9) + "/", "");
                    }
                }
            }
            if (fullDirName != null) {
                String assemblyReportUrl = assembliesParentDir + "/" + fullDirName + "/" + fullDirName + "_assembly_report.txt";
                return new URL(assemblyReportUrl);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalArgumentException("Unable to find accession " + accession + " in NCBI FTP directory " + assembliesParentDir);
    }

    /**
     * Attempts to download an NCBI assembly report from the URL provided.
     *
     * @param assemblyReportUrl {@link URL} pointing to an NCBI assembly_report.txt file
     * @return a {@link GenomicAssembly} read from the report.
     */
    public static GenomicAssembly downloadAssembly(URL assemblyReportUrl) {
        try(ReadableByteChannel readableByteChannel = Channels.newChannel(assemblyReportUrl.openStream())) {
            String file = assemblyReportUrl.getFile();
            String fileName = file.substring(file.lastIndexOf('/') + 1);
            Path tempFile = Files.createTempFile(fileName, null);
            FileOutputStream outputStream = new FileOutputStream(tempFile.toFile());
            FileChannel fileChannel = outputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            return GenomicAssembly.readAssembly(tempFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
