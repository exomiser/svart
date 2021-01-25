package org.monarchinitiative.svart;

import java.io.InputStream;

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
}
