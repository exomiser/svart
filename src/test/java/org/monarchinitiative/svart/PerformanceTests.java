package org.monarchinitiative.svart;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests are used for performance benchmarking the library and for comparing it with the HTSJDK. The pauses
 * introduced by Thread.sleep() are to allow time for benchmarking tools to be launched.
 */
@Disabled("Performance tests")
public class PerformanceTests {

    @Test
    public void readVariantContexts() throws InterruptedException {
        Thread.sleep(2000);

        // warm-up
        var allVariants = new ArrayList<>();
        int totalRead = 0;
        for (int i = 0; i < 50; i++) {
            List<VariantContext> variants = new VcfVariantContextReader()
                    .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
                    .toList();
            allVariants.addAll(variants);
            Thread.sleep(500);
            totalRead += variants.size();
        }
        System.gc();
        System.out.println("Read " + totalRead + " variants to warm-up");
        Thread.sleep(2000);

        Instant start = Instant.now();

        List<VariantContext> variants = new VcfVariantContextReader()
                .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
                .toList();

        Instant end = Instant.now();

        System.out.println("Read " + variants.size() + " variant contexts in " + Duration.between(start, end).toMillis() + " msec");
        assertTrue(true);
    }

    @Test
    public void readVariantContextsHtsJdk() {

        // warm-up
        int totalRead = 0;
        for (int i = 0; i < 50; i++) {
            List<VariantContext> variants = readVariantContexts(Path.of("src/test/resources/pfeiffer.vcf")).toList();
            totalRead += variants.size();
        }
        System.out.println("Read " + totalRead + " variants to warm-up");

        Instant start = Instant.now();

        List<VariantContext> variants = readVariantContexts(Path.of("src/test/resources/pfeiffer.vcf")).toList();

        Instant end = Instant.now();

//        BEDCodec.BED_EXTENSION
        System.out.println("Read " + variants.size() + " HTSJDK variants in " + Duration.between(start, end).toMillis() + " msec");
        assertTrue(true);
    }

    @Test
    public void readSvartVariants() throws InterruptedException {
        Thread.sleep(2000);

        // warm-up
        int totalRead = 0;
        var allVariants = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            List<GenomicVariant> variants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
                    .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
                    .toList();
            Thread.sleep(500);
            allVariants.addAll(variants);
            totalRead += variants.size();
        }
        System.gc();
        System.out.println("Read " + totalRead + " variants to warm-up");
        Thread.sleep(2000);
        Instant start = Instant.now();

        List<GenomicVariant> variants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
                .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
                .toList();

        Instant end = Instant.now();

        System.out.println("Read " + variants.size() + " Svart variants in " + Duration.between(start, end).toMillis() + " msec");
        assertTrue(true);
    }

    public static Stream<VariantContext> readVariantContexts(Path vcfPath) {
        Objects.requireNonNull(vcfPath, "Cannot read from null vcfPath");
        try (VCFFileReader vcfReader = new VCFFileReader(vcfPath, false)) {
            return vcfReader.iterator().stream();
        }
    }

    // super-simple VCF reader which will only read the variant coordinates, ignoring any sample genotype information.
    private static class VcfVariantReader {

        private final GenomicAssembly genomicAssembly;

        public VcfVariantReader(GenomicAssembly genomicAssembly) {
            this.genomicAssembly = genomicAssembly;
        }

        public Stream<GenomicVariant> readVariants(Path vcfPath) {
            try {
                return Files.lines(vcfPath)
                        .filter(line -> !line.startsWith("#"))
                        .flatMap(toVariants());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Stream.empty();
        }

        private Function<String, Stream<GenomicVariant>> toVariants() {
            return line -> {
                // #CHROM POS ID REF ALT QUAL FILTER INFO
                String[] columns = line.split("\t");
                String alt = columns[4];
                return Arrays.stream(alt.split(",")).map(allele -> convertToVariant(allele, columns));
            };
        }

        private GenomicVariant convertToVariant(String altAllele, String[] columns) {
            // #CHROM POS ID REF ALT QUAL FILTER INFO
            String chrom = columns[0];
            int start = Integer.parseInt(columns[1]);
            String id = columns[2];
            String ref = columns[3];
            String info = columns[7];

            if (VariantType.isSymbolic(altAllele)) {
                Map<String, String> infoFields = readInfoFields(info);
                int changeLength = intOrDefault(infoFields.get("SVLEN"), 0);
                int end = intOrDefault(infoFields.get("END"), 0);
                return GenomicVariant.of(genomicAssembly.contigByName(chrom), id, Strand.POSITIVE, CoordinateSystem.oneBased(), start, end, ref, altAllele, changeLength);
            }
            return GenomicVariant.of(genomicAssembly.contigByName(chrom), id, Strand.POSITIVE, CoordinateSystem.oneBased(), start, ref, altAllele);
        }

        private Map<String, String> readInfoFields(String info) {
            Map<String, String> infoMap = new HashMap<>();
            String[] infoFields = info.split(";");
            for (int i = 0; i < infoFields.length; i++) {
                String field = infoFields[i];
                String[] fieldKv = field.split("=");
                infoMap.put(fieldKv[0], fieldKv[1]);
            }
            return infoMap;
        }

        private int intOrDefault(String value, int defaultValue) {
            return value == null ? defaultValue : Integer.parseInt(value);
        }
    }

    private static class VcfVariantContextReader {
        public Stream<VariantContext> readVariants(Path vcfPath) {
            try {
                return Files.lines(vcfPath)
                        .filter(line -> !line.startsWith("#"))
                        .flatMap(toVariants());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Stream.empty();
        }

        private Function<String, Stream<VariantContext>> toVariants() {
            return line -> {
                // #CHROM POS ID REF ALT QUAL FILTER INFO
                String[] columns = line.split("\t");
                String alt = columns[4];
                return Arrays.stream(alt.split(",")).map(allele -> toVariant(allele, columns));
            };
        }

        private VariantContext toVariant(String altAllele, String[] columns) {
            // #CHROM POS ID REF ALT QUAL FILTER INFO
            String chrom = columns[0];
            int start = Integer.parseInt(columns[1]);
            String id = columns[2];
            String ref = columns[3];
            String info = columns[7];

            List<Allele> alleles = new ArrayList<>();
            alleles.add(Allele.create(ref, true));
            alleles.add(Allele.create(altAllele));

            return new VariantContextBuilder()
                    .chr(chrom)
                    .id(id)
                    .alleles(alleles)
                    .start(start)
                    .computeEndFromAlleles(alleles, start)
                    .make();
        }

        private Map<String, String> readInfoFields(String info) {
            Map<String, String> infoMap = new HashMap<>();
            String[] infoFields = info.split(";");
            for (int i = 0; i < infoFields.length; i++) {
                String field = infoFields[i];
                String[] fieldKv = field.split("=");
                infoMap.put(fieldKv[0], fieldKv[1]);
            }
            return infoMap;
        }

        private int intOrDefault(String value, int defaultValue) {
            return value == null ? defaultValue : Integer.parseInt(value);
        }
    }
}
