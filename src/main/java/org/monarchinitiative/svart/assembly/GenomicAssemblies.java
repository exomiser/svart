package org.monarchinitiative.svart.assembly;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading {@link GenomicAssembly} objects from NCBI assembly_report.txt files. This class also
 * provides some more contemporary reference assemblies for human and mouse for user convenience.
 */
public class GenomicAssemblies {

    private enum Assembly {
        GRCH37_P13("GCA_000001405.14", "GCF_000001405.25", "GCF_000001405.25_GRCh37.p13_assembly_report.txt"),
        GRCH38_P13("GCA_000001405.28", "GCF_000001405.39", "GCF_000001405.39_GRCh38.p13_assembly_report.txt"),
        T2T_CHM13_V2_0("GCA_009914755.4", "GCF_009914755.1", "GCF_009914755.1_T2T-CHM13v2.0_assembly_report.txt"),
        GRCM38_P6("GCA_000001635.8", "GCF_000001635.26", "GCF_000001635.26_GRCm38.p6_assembly_report.txt"),
        GRCM39("GCA_000001635.9", "GCF_000001635.27", "GCF_000001635.27_GRCm39_assembly_report.txt");

        private static final String ASSEMBLY_RESOURCE_PATH = "org/monarchinitiative/svart/assemblies/";

        private final String genBankAccession;
        private final String refSeqAccession;
        private final String assemblyResourceFilename;

        /**
         * Do **NOT** access directly as this will be null - use the accessor method
         */
        private volatile GenomicAssembly genomicAssembly = null;

        Assembly(String genBankAccession, String refSeqAccession, String assemblyResourceFilename) {
            this.genBankAccession = genBankAccession;
            this.refSeqAccession = refSeqAccession;
            this.assemblyResourceFilename = assemblyResourceFilename;
        }

        GenomicAssembly genomicAssembly() {
            if (genomicAssembly == null) {
                synchronized (Assembly.class) {
                    if (genomicAssembly == null) {
                        genomicAssembly = readAssemblyResource(assemblyResourceFilename);
                    }
                }
            }
            return genomicAssembly;
        }

        private static GenomicAssembly readAssemblyResource(String resourceFileName) {
            InputStream inputStream = getResourceAsStream(ASSEMBLY_RESOURCE_PATH + resourceFileName);
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
            // load from the module path
            Optional<Module> svartModuleOptional = ModuleLayer.boot().findModule("org.monarchinitiative.svart");
            if (svartModuleOptional.isPresent()) {
                Module svartModule = svartModuleOptional.get();
                try {
                    return svartModule.getResourceAsStream(path);
                } catch (IOException e) {
                    // swallow and fall through
                }
            }
            throw new IllegalStateException("Unable to load resource " + path);
        }
    }

    private static final Pattern ACCESSION_PATTERN = Pattern.compile("(?<prefix>GC[A,F])_(?<accession>[\\d]{9})\\.(?<version>[\\d]+)");
    private static final Map<String, Assembly> ASSEMBLIES;

    static {
        var temp = new HashMap<String, Assembly>();
        for (Assembly assembly : Assembly.values()) {
            temp.put(assembly.genBankAccession, assembly);
            temp.put(assembly.refSeqAccession, assembly);
        }
        ASSEMBLIES = Map.copyOf(temp);
    }

    private GenomicAssemblies() {
    }

    public static Set<String> listAssemblies() {
        return ASSEMBLIES.keySet();
    }

    public static boolean containsAssembly(String accession) {
        return ASSEMBLIES.containsKey(accession);
    }

    /**
     * Will return the cached {@link GenomicAssembly} for the given accession. Will return null if not one of the
     * pre-cached assemblies. To obtain an uncached assembly, use the {@link GenomicAssemblies::downloadAssembly} method.
     *
     * @param accession A GenBank or RefSeq accession
     * @return a {@link GenomicAssembly} for the given accession or null if not recognised.
     */
    public static GenomicAssembly getAssembly(String accession) {
        if (!ACCESSION_PATTERN.matcher(accession).matches()) {
            throw new IllegalArgumentException("Invalid GenBank/RefSeq assembly accession " + accession);
        }
        Assembly assembly = ASSEMBLIES.get(accession);
        return assembly == null ? null : assembly.genomicAssembly();
    }

    /**
     * Parses the file 'GCF_000001405.25_GRCh37.p13_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The human GRCh37.p13 {@link GenomicAssembly} also known as b37, hg19
     */
    public static GenomicAssembly GRCh37p13() {
        return Assembly.GRCH37_P13.genomicAssembly();
    }

    /**
     * Parses the file 'GCF_000001405.39_GRCh38.p13_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The human GRCh38.p13 {@link GenomicAssembly} also known as b38, hg38
     */
    public static GenomicAssembly GRCh38p13() {
        return Assembly.GRCH38_P13.genomicAssembly();
    }

    /**
     * Parses the file 'GCF_009914755.1_T2T-CHM13v2.0_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The human T2T-CHM13v2.0 {@link GenomicAssembly}
     */
    public static GenomicAssembly T2T_CHM13v2_0() {
        return Assembly.T2T_CHM13_V2_0.genomicAssembly();
    }

    /**
     * Parses the file 'GCF_000001635.26_GRCm38.p6_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The mouse GRCm38.p6 {@link GenomicAssembly}
     */
    public static GenomicAssembly GRCm38p6() {
        return Assembly.GRCM38_P6.genomicAssembly();
    }

    /**
     * Parses the file 'GCF_000001635.27_GRCm39_assembly_report.txt' and returns a {@link GenomicAssembly} instance.
     *
     * @return The mouse GRCm39 {@link GenomicAssembly}
     */
    public static GenomicAssembly GRCm39() {
        return Assembly.GRCM39.genomicAssembly();
    }

    /**
     * Attempts to download the assembly_report.txt file for the given GenBank or RefSeq accession. Accessions must have
     * the pattern "GC[A,F])_[\d]{9}\.[\d]+" e.g. GCF_000001405.39
     * <p>
     * This method will attempt to locate and download the file from the NCBI FTP servers from the URL <url>ftp://ftp.ncbi.nlm.nih.gov/genomes/all</url>.
     * It will not cache the result internally so each call will re-download the data.
     *
     * @param accession A GenBank or RefSeq accession
     * @return a {@link GenomicAssembly} for the given accession.
     */
    public static GenomicAssembly downloadAssembly(String accession) {
        URL assemblyReportUrl = locateNcbiAssemblyReportUrl(accession);
        return downloadAssembly(assemblyReportUrl);
    }

    /**
     * Attempts to download an NCBI assembly report from the URL provided. This method will not cache the result, so
     * each call will re-download the data. It is the responsibility of the user to implement cacheing if required.
     *
     * @param assemblyReportUrl {@link URL} pointing to an NCBI assembly_report.txt file
     * @return a {@link GenomicAssembly} read from the report.
     */
    public static GenomicAssembly downloadAssembly(URL assemblyReportUrl) {
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(assemblyReportUrl.openStream())) {
            String file = assemblyReportUrl.getFile();
            String fileName = file.substring(file.lastIndexOf('/') + 1);
            Path tempFile = Files.createTempFile(fileName, null);
            try (FileOutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
            return GenomicAssembly.readAssembly(tempFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Attempts to download an NCBI assembly report for a given accession to the specified directory. This method will
     * not cache the result, so each call will re-download the data. It is the responsibility of the user to implement
     * cacheing if required.
     *
     * @param accession         {@link URL} pointing to an NCBI assembly_report.txt file
     * @param downloadDirectory {@link Path} to the directory where the file should be downloaded
     * @return a {@link Path} to the downloaded report.
     */
    public static Path downloadAssembly(String accession, Path downloadDirectory) {
        try {
            Files.createDirectories(downloadDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        URL assemblyReportUrl = locateNcbiAssemblyReportUrl(accession);
        try (ReadableByteChannel readableByteChannel = Channels.newChannel(assemblyReportUrl.openStream())) {
            String file = assemblyReportUrl.getFile();
            String fileName = file.substring(file.lastIndexOf('/') + 1);
            Path downloadedFile = downloadDirectory.resolve(fileName);
            try (FileOutputStream outputStream = new FileOutputStream(downloadedFile.toFile())) {
                outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
            return downloadedFile;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static URL locateNcbiAssemblyReportUrl(String accession) {
        //  GCF_000001405.39 GCF_000001405.25
        // From ftp://ftp.ncbi.nlm.nih.gov/genomes/all/README.txt
        // Two directories under "all" are named for the accession prefix (GCA or GCF) and these directories
        // contain another three levels of directories named for digits 1-3, 4-6 & 7-9 of the assembly accession.
        // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/
        Matcher matcher = ACCESSION_PATTERN.matcher(accession);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid GenBank/RefSeq assembly accession " + accession);
        }
        String acc = matcher.group("accession");
        String assembliesParentDir = String.format("ftp://ftp.ncbi.nlm.nih.gov/genomes/all/%s/%s/%s/%s", matcher.group("prefix"), acc.substring(0, 3), acc.substring(3, 6), acc.substring(6, 9));
        String fullDirName = null;
        try {
            URL parentDirUrl = new URL(assembliesParentDir + ";type=d");
            URLConnection conn = parentDirUrl.openConnection();
            try (InputStream inputStream = conn.getInputStream()) {
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
}
