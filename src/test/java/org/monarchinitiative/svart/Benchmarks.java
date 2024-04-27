package org.monarchinitiative.svart;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.impl.CompactSequenceVariant;
import org.monarchinitiative.svart.impl.DefaultSequenceVariant;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Benchmarks {

    private static final List<GenomicVariant> compactVariants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .toList();
    private static final List<DefaultSequenceVariant> defaultVariants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .map(compactVariant -> DefaultSequenceVariant.of(compactVariant.contig(), compactVariant.id(), compactVariant.strand(), compactVariant.coordinateSystem(), compactVariant.start(), compactVariant.ref(), compactVariant.alt()))
            .toList();


    private static final List<GenomeVariant> genomeVariants = new VcfGenomeVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomeVariant -> genomeVariant.getGenomeInterval().length() <= CompactSequenceVariant.MAX_BASES)
            .toList();


//    REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
//    why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
//    experiments, perform baseline and negative tests that provide experimental control, make sure
//    the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
//    Do not assume the numbers tell you what you want them to tell.
//
//    Benchmark                          Mode  Cnt      Score      Error  Units
//    Benchmarks.compactAlt             thrpt    5   1040.660 ±   60.180  ops/s ->  2203.821 ±  103.569  ops/s * 5423.175 ± 171.614 (array)
//    Benchmarks.compactEnd             thrpt    5   8959.968 ±  125.378  ops/s ->  9255.713 ±  124.246  ops/s
//    Benchmarks.compactLength          thrpt    5  10505.113 ± 1806.242  ops/s -> 10720.183 ±  232.260  ops/s
//    Benchmarks.compactOppositeStrand  thrpt    5    939.276 ±  212.803  ops/s ->  2060.543 ±  232.505  ops/s *
//    Benchmarks.compactRef             thrpt    5    965.305 ±  383.621  ops/s ->  2296.525 ±   56.027  ops/s * 6310.107 ± 102.176 (array)
//    Benchmarks.compactStart           thrpt    5   8581.371 ± 3443.939  ops/s ->  8372.756 ± 3340.599  ops/s
//    Benchmarks.compactToOneBased      thrpt    5   9173.265 ± 3596.668  ops/s ->  9545.169 ± 3548.820  ops/s -> 9768.447 ± 390.431
//    Benchmarks.compactToZeroBased     thrpt    5   4722.046 ± 1577.933  ops/s ->  4412.244 ± 1237.885  ops/s -> 5051.027 ±  72.906

//    Benchmarks.defaultAlt             thrpt    5   3844.564 ±   67.110  ops/s ->  3946.975 ±   25.651  ops/s
//    Benchmarks.defaultEnd             thrpt    5   1646.615 ±  647.118  ops/s ->  2182.289 ±  743.834  ops/s
//    Benchmarks.defaultLength          thrpt    5   1445.958 ±   37.008  ops/s ->  1739.747 ±  684.989  ops/s
//    Benchmarks.defaultOppositeStrand  thrpt    5    322.649 ±   49.079  ops/s ->   335.711 ±    4.918  ops/s
//    Benchmarks.defaultRef             thrpt    5   3215.081 ±  474.616  ops/s ->  4210.874 ±   36.588  ops/s
//    Benchmarks.defaultStart           thrpt    5   7698.631 ± 6029.497  ops/s ->  2213.178 ±  755.360  ops/s
//    Benchmarks.defaultToOneBased      thrpt    5   1902.064 ±   52.874  ops/s ->  1885.103 ±   23.221  ops/s
//    Benchmarks.defaultToZeroBased     thrpt    5    568.933 ±  154.338  ops/s ->   639.957 ±   14.421  ops/s

//Benchmark                          Mode  Cnt      Score      Error  Units
//Benchmarks.compactAlt             thrpt    5   2173.188 ± 113.340  ops/s
//Benchmarks.compactEnd             thrpt    5   9239.456 ± 683.444  ops/s
//Benchmarks.compactLength          thrpt    5  10611.716 ± 162.619  ops/s
//Benchmarks.compactOppositeStrand  thrpt    5   1949.412 ±  54.162  ops/s
//Benchmarks.compactRef             thrpt    5   2335.252 ±  45.224  ops/s
//Benchmarks.compactStart           thrpt    5   9622.296 ± 157.008  ops/s
//Benchmarks.compactStartOneBased   thrpt    5  10736.110 ±  75.036  ops/s
//Benchmarks.compactStartZeroBased  thrpt    5  10997.489 ± 102.975  ops/s
//Benchmarks.compactToOneBased      thrpt    5  10029.448 ± 110.255  ops/s
//Benchmarks.compactToZeroBased     thrpt    5   5224.590 ±  99.765  ops/s

//Benchmarks.defaultAlt             thrpt    5   3637.614 ±  89.865  ops/s
//Benchmarks.defaultEnd             thrpt    5   2051.605 ± 504.719  ops/s
//Benchmarks.defaultLength          thrpt    5   1747.344 ±  66.728  ops/s
//Benchmarks.defaultOppositeStrand  thrpt    5    343.190 ±   4.419  ops/s
//Benchmarks.defaultRef             thrpt    5   3993.852 ± 190.443  ops/s
//Benchmarks.defaultStart           thrpt    5   2060.962 ± 499.647  ops/s
//Benchmarks.defaultStartOneBased   thrpt    5   1753.038 ± 239.052  ops/s
//Benchmarks.defaultStartZeroBased  thrpt    5   1762.910 ±  59.315  ops/s
//Benchmarks.defaultToOneBased      thrpt    5   1745.539 ± 177.385  ops/s
//Benchmarks.defaultToZeroBased     thrpt    5    636.525 ±  21.953  ops/s

//Benchmarks.jannovarAlt             thrpt    5  4417.538 ± 250.722  ops/s
//Benchmarks.jannovarEnd             thrpt    5  1116.893 ± 314.577  ops/s
//Benchmarks.jannovarLength          thrpt    5  1382.077 ± 148.480  ops/s
//Benchmarks.jannovarOppositeStrand  thrpt    5   406.022 ±   5.321  ops/s
//Benchmarks.jannovarRef             thrpt    5  4297.626 ± 442.264  ops/s
//Benchmarks.jannovarStart           thrpt    5  1836.905 ±  25.524  ops/s
//Benchmarks.jannovarStartOneBased   thrpt    5  1842.008 ±  22.988  ops/s
//Benchmarks.jannovarStartZeroBased  thrpt    5  1422.128 ±  58.807  ops/s

    // There are 37526 CompactVariants in the Pfeiffer sample so all values must be multiplied by 37526
    // e.g. 37526 * 10736 = 403265632 ops/s
    // TODO benchmark vs Jannovar & HTSJDK


    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void compactStart(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void compactEnd(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void compactLength(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void compactRef(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void compactAlt(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void compactOppositeStrand(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void compactStartZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.startZeroBased());
        }
    }

    @Benchmark
    public void compactStartOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.startOneBased());
        }
    }

    @Benchmark
    public void compactToZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void compactToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : compactVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    @Benchmark
    public void defaultStart(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void defaultEnd(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void defaultLength(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void defaultRef(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void defaultAlt(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void defaultOppositeStrand(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void defaultStartZeroBased(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.startZeroBased());
        }
    }

    @Benchmark
    public void defaultStartOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.startOneBased());
        }
    }

    @Benchmark
    public void defaultToZeroBased(Blackhole blackhole) {
        for (DefaultSequenceVariant variant : defaultVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void defaultToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    @Benchmark
    public void jannovarStart(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getPos());
        }
    }

    @Benchmark
    public void jannovarEnd(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getGenomeInterval().getEndPos());
        }
    }

    @Benchmark
    public void jannovarLength(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getGenomeInterval().length());
        }
    }

    @Benchmark
    public void jannovarRef(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getRef());
        }
    }

    @Benchmark
    public void jannovarAlt(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getAlt());
        }
    }

    @Benchmark
    public void jannovarOppositeStrand(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.withStrand(variant.getGenomePos().getStrand().isForward() ? de.charite.compbio.jannovar.reference.Strand.REV : de.charite.compbio.jannovar.reference.Strand.FWD ));
        }
    }

    @Benchmark
    public void jannovarStartZeroBased(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getGenomeInterval().getBeginPos());
        }
    }

    @Benchmark
    public void jannovarStartOneBased(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getPos() + 1);
        }
    }

// Jannovar has no equivalent methods for changing the coordinate system
//    @Benchmark
//    public void jannovarToZeroBased(Blackhole blackhole) {
//        for (GenomeVariant variant : genomeVariants) {
//            blackhole.consume(variant.getGenomeInterval().toZeroBased());
//        }
//    }
//
//    @Benchmark
//    public void jannovarToOneBased(Blackhole blackhole) {
//        for (GenomeVariant variant : genomeVariants) {
//            blackhole.consume(variant.toOneBased());
//        }
//    }

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

    // super-simple VCF reader which will only read the variant coordinates, ignoring any sample genotype information.
    private static class VcfGenomeVariantReader {

        private final ReferenceDictionary refDict;

        public VcfGenomeVariantReader(GenomicAssembly genomicAssembly) {
            this.refDict = buildRefDictFromGenomicAssembly(genomicAssembly);
        }

        private ReferenceDictionary buildRefDictFromGenomicAssembly(GenomicAssembly genomicAssembly) {
            var builder = new ReferenceDictionaryBuilder();
            for (Contig contig : genomicAssembly.contigs()) {
                builder.putContigID(contig.name(), contig.id());
                builder.putContigName(contig.id(), contig.name());
                builder.putContigLength(contig.id(), contig.length());
            }
            return builder.build();
        }

        public Stream<GenomeVariant> readVariants(Path vcfPath) {
            try {
                return Files.lines(vcfPath)
                        .filter(line -> !line.startsWith("#"))
                        .flatMap(toVariants())
                        .filter(Objects::nonNull);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Stream.empty();
        }

        private Function<String, Stream<GenomeVariant>> toVariants() {
            return line -> {
                // #CHROM POS ID REF ALT QUAL FILTER INFO
                String[] columns = line.split("\t");
                String alt = columns[4];
                return Arrays.stream(alt.split(",")).map(allele -> convertToVariant(allele, columns));
            };
        }

        private GenomeVariant convertToVariant(String altAllele, String[] columns) {
            // #CHROM POS ID REF ALT QUAL FILTER INFO
            String chrom = columns[0];
            int start = Integer.parseInt(columns[1]);
            String id = columns[2];
            String ref = columns[3];
            String info = columns[7];

//            if (VariantType.isSymbolic(altAllele)) {
//                Map<String, String> infoFields = readInfoFields(info);
//                int changeLength = intOrDefault(infoFields.get("SVLEN"), 0);
//                int end = intOrDefault(infoFields.get("END"), 0);
//                return GenomicVariant.of(genomicAssembly.contigByName(chrom), id, Strand.POSITIVE, CoordinateSystem.oneBased(), start, end, ref, altAllele, changeLength);
//            }
            int chr = refDict.getContigNameToID().getOrDefault(chrom, 0);
            if (chr == 0) {
                return null;
            }
            var genomePosition = new GenomePosition(refDict, de.charite.compbio.jannovar.reference.Strand.FWD, chr, start, PositionType.ONE_BASED);
            return new GenomeVariant(genomePosition, ref, altAllele);
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
