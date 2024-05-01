package org.monarchinitiative.svart;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.impl.CompactSequenceVariant;
import org.monarchinitiative.svart.impl.DefaultSequenceVariant;
import org.openjdk.jmh.annotations.Benchmark;
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

    private static final List<DelegateGenotypesVariant> delegateCompactVariants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .map(variant -> new DelegateGenotypesVariant(variant, Genotype.HET))
            .toList();

    private static final List<DelegateGenotypesVariant> delegateDefaultVariants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .map(compactVariant -> DefaultSequenceVariant.of(compactVariant.contig(), compactVariant.id(), compactVariant.strand(), compactVariant.coordinateSystem(), compactVariant.start(), compactVariant.ref(), compactVariant.alt()))
            .map(variant -> new DelegateGenotypesVariant(variant, Genotype.HET))
            .toList();

    private static final List<InheritanceBasedDelegateGenomicVariant> inheritanceCompactVariants = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .map(variant -> new InheritanceBasedDelegateGenomicVariant(variant, Genotype.HET))
            .toList();

    private static final List<VariantContext> variantContexts = new VcfVariantReader(GenomicAssemblies.GRCh37p13())
            .readVariants(Path.of("src/test/resources/pfeiffer.vcf"))
            .filter(genomicVariant -> genomicVariant instanceof CompactSequenceVariant)
            .map(toVariantContext())
            .toList();

    private static Function<GenomicVariant, VariantContext> toVariantContext() {
        return genomicVariant -> {
            List<Allele> alleles = List.of(Allele.create(genomicVariant.ref(), true), Allele.create(genomicVariant.alt()));
            return new VariantContextBuilder()
                    .chr(genomicVariant.contigName())
                    .id(genomicVariant.id())
                    .start(genomicVariant.start())
                    .alleles(alleles)
                    .computeEndFromAlleles(alleles, genomicVariant.start())
                    .make();
        };
    }
//    REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
//    why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
//    experiments, perform baseline and negative tests that provide experimental control, make sure
//    the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
//    Do not assume the numbers tell you what you want them to tell.
//
//    Benchmark                          Mode  Cnt      Score      Error  Units
//    Benchmarks.compactAlt             thrpt    5   1040.660 ±   60.180  ops/s ->  2203.821 ±  103.569  ops/s -> 5423.175 ± 171.614 (array)
//    Benchmarks.compactEnd             thrpt    5   8959.968 ±  125.378  ops/s ->  9255.713 ±  124.246  ops/s
//    Benchmarks.compactLength          thrpt    5  10505.113 ± 1806.242  ops/s -> 10720.183 ±  232.260  ops/s
//    Benchmarks.compactOppositeStrand  thrpt    5    939.276 ±  212.803  ops/s ->  2060.543 ±  232.505  ops/s *
//    Benchmarks.compactRef             thrpt    5    965.305 ±  383.621  ops/s ->  2296.525 ±   56.027  ops/s -> 6310.107 ± 102.176 (array)
//    Benchmarks.compactStart           thrpt    5   8581.371 ± 3443.939  ops/s ->  8372.756 ± 3340.599  ops/s
//    Benchmarks.compactToOneBased      thrpt    5   9173.265 ± 3596.668  ops/s ->  9545.169 ± 3548.820  ops/s -> 9768.447 ± 390.431
//    Benchmarks.compactToZeroBased     thrpt    5   4722.046 ± 1577.933  ops/s ->  4412.244 ± 1237.885  ops/s -> 5051.027 ±  72.906
    

    // There are 37526 CompactVariants in the Pfeiffer sample so all values must be multiplied by 37526
    // e.g. 37526 * 10736 = 403265632 ops/s
//    Benchmark                                  Mode  Cnt      Score      Error  Units
//    Benchmarks.compactAlt                     thrpt    5   5510.843 ±   89.973  ops/s
//    Benchmarks.compactEnd                     thrpt    5   9961.695 ±  387.120  ops/s
//    Benchmarks.compactLength                  thrpt    5  11153.347 ±  300.928  ops/s
//    Benchmarks.compactOppositeStrand          thrpt    5   2211.894 ±   21.031  ops/s
//    Benchmarks.compactRef                     thrpt    5   6092.992 ±   82.673  ops/s
//    Benchmarks.compactStart                   thrpt    5   9948.732 ±   95.912  ops/s
//    Benchmarks.compactStartOneBased           thrpt    5  10914.213 ±   97.015  ops/s
//    Benchmarks.compactStartZeroBased          thrpt    5  11482.406 ±   63.769  ops/s
//    Benchmarks.compactToOneBased              thrpt    5  10661.458 ±  123.680  ops/s
//    Benchmarks.compactToZeroBased             thrpt    5   5061.602 ±   52.675  ops/s

//    Benchmarks.defaultAlt                     thrpt    5   9602.609 ±   91.239  ops/s
//    Benchmarks.defaultEnd                     thrpt    5   9332.589 ±  109.797  ops/s
//    Benchmarks.defaultLength                  thrpt    5   8051.962 ±   83.717  ops/s
//    Benchmarks.defaultOppositeStrand          thrpt    5    338.719 ±    7.317  ops/s
//    Benchmarks.defaultRef                     thrpt    5   9646.041 ±    8.615  ops/s
//    Benchmarks.defaultStart                   thrpt    5   8966.542 ±  140.602  ops/s
//    Benchmarks.defaultStartOneBased           thrpt    5   8412.463 ±  283.386  ops/s
//    Benchmarks.defaultStartZeroBased          thrpt    5   8465.088 ±  109.346  ops/s
//    Benchmarks.defaultToOneBased              thrpt    5   8650.708 ±  360.022  ops/s
//    Benchmarks.defaultToZeroBased             thrpt    5    640.607 ±   16.690  ops/s

//    Benchmarks.delegateCompactAlt             thrpt    5   4998.888 ±  166.550  ops/s
//    Benchmarks.delegateCompactEnd             thrpt    5   6629.503 ±  197.108  ops/s
//    Benchmarks.delegateCompactLength          thrpt    5   6242.121 ±   43.779  ops/s
//    Benchmarks.delegateCompactOppositeStrand  thrpt    5    973.367 ±   36.225  ops/s
//    Benchmarks.delegateCompactRef             thrpt    5   5490.170 ±  141.087  ops/s
//    Benchmarks.delegateCompactStart           thrpt    5   6661.699 ±   38.874  ops/s
//    Benchmarks.delegateCompactStartOneBased   thrpt    5   6680.050 ±   87.853  ops/s
//    Benchmarks.delegateCompactStartZeroBased  thrpt    5   6437.064 ±  243.283  ops/s
//    Benchmarks.delegateCompactToOneBased      thrpt    5   6624.994 ±  137.594  ops/s
//    Benchmarks.delegateCompactToZeroBased     thrpt    5   2919.154 ±  692.073  ops/s

//    Benchmarks.delegateDefaultAlt             thrpt    5   8093.398 ±  173.580  ops/s
//    Benchmarks.delegateDefaultEnd             thrpt    5   7895.103 ±  313.203  ops/s
//    Benchmarks.delegateDefaultLength          thrpt    5   7178.487 ±  691.470  ops/s
//    Benchmarks.delegateDefaultOppositeStrand  thrpt    5    297.987 ±   77.710  ops/s
//    Benchmarks.delegateDefaultRef             thrpt    5   8132.101 ±  634.861  ops/s
//    Benchmarks.delegateDefaultStart           thrpt    5   7791.418 ±   75.556  ops/s
//    Benchmarks.delegateDefaultStartOneBased   thrpt    5   7568.391 ±  325.076  ops/s
//    Benchmarks.delegateDefaultStartZeroBased  thrpt    5   6850.250 ±   90.531  ops/s
//    Benchmarks.delegateDefaultToOneBased      thrpt    5   7440.287 ±   78.424  ops/s
//    Benchmarks.delegateDefaultToZeroBased     thrpt    5    558.627 ±   17.714  ops/s

//    Benchmarks.htsjdkAlt                      thrpt    5   1874.343 ±   36.714  ops/s
//    Benchmarks.htsjdkEnd                      thrpt    5   8135.517 ±  352.572  ops/s
//    Benchmarks.htsjdkLength                   thrpt    5   7315.199 ±   93.799  ops/s
//    Benchmarks.htsjdkRef                      thrpt    5   2145.302 ±   40.892  ops/s
//    Benchmarks.htsjdkStart                    thrpt    5   8245.544 ±  192.343  ops/s

//    Benchmarks.jannovarAlt                    thrpt    5   9135.857 ±   74.926  ops/s
//    Benchmarks.jannovarEnd                    thrpt    5   7777.722 ±   93.533  ops/s
//    Benchmarks.jannovarLength                 thrpt    5   7749.275 ±  190.929  ops/s
//    Benchmarks.jannovarOppositeStrand         thrpt    5    390.066 ±  100.433  ops/s
//    Benchmarks.jannovarRef                    thrpt    5   8289.992 ±   36.936  ops/s
//    Benchmarks.jannovarStart                  thrpt    5   7770.833 ±  392.999  ops/s
//    Benchmarks.jannovarStartOneBased          thrpt    5   9474.978 ±  280.803  ops/s
//    Benchmarks.jannovarStartZeroBased         thrpt    5   8350.182 ±  303.768  ops/s


    public static void main(String[] args) throws Exception {
        System.out.println(compactVariants.size());
        System.out.println(defaultVariants.size());
        System.out.println(genomeVariants.size());
        System.out.println(delegateCompactVariants.size());
        System.out.println(delegateDefaultVariants.size());
        System.out.println(variantContexts.size());
        Options opt = new OptionsBuilder()
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    // Compact
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

    // Default
    @Benchmark
    public void defaultStart(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void defaultEnd(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void defaultLength(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void defaultRef(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void defaultAlt(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void defaultOppositeStrand(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void defaultStartZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
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
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void defaultToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : defaultVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    // Jannovar
    // Jannovar has no equivalent methods for changing the coordinate system
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
            blackhole.consume(variant.withStrand(variant.getGenomePos().getStrand().isForward() ? de.charite.compbio.jannovar.reference.Strand.REV : de.charite.compbio.jannovar.reference.Strand.FWD));
        }
    }

    @Benchmark
    public void jannovarStartZeroBased(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getPos());
        }
    }

    @Benchmark
    public void jannovarStartOneBased(Blackhole blackhole) {
        for (GenomeVariant variant : genomeVariants) {
            blackhole.consume(variant.getPos() + 1);
        }
    }

    // HTSJDK
    // HTSJDK VariantContext is primarily used for parsing the VCF file and is a direct representation of a VCF record
    // which by default represents one or more alleles at a genomic location, so the API isn't quite aligned with what
    // svart tries to solve.
    @Benchmark
    public void htsjdkStart(Blackhole blackhole) {
        for (VariantContext variant : variantContexts) {
            blackhole.consume(variant.getStart());
        }
    }

    @Benchmark
    public void htsjdkEnd(Blackhole blackhole) {
        for (VariantContext variant : variantContexts) {
            blackhole.consume(variant.getEnd());
        }
    }

    @Benchmark
    public void htsjdkLength(Blackhole blackhole) {
        for (VariantContext variant : variantContexts) {
            blackhole.consume(variant.getLengthOnReference());
        }
    }

    @Benchmark
    public void htsjdkRef(Blackhole blackhole) {
        for (VariantContext variant : variantContexts) {
            blackhole.consume(variant.getReference().getDisplayString());
        }
    }

    @Benchmark
    public void htsjdkAlt(Blackhole blackhole) {
        for (VariantContext variant : variantContexts) {
            blackhole.consume(variant.getAlternateAllele(0).getDisplayString());
        }
    }

// Delegate
    @Benchmark
    public void delegateCompactStart(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void delegateCompactEnd(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void delegateCompactLength(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void delegateCompactRef(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void delegateCompactAlt(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void delegateCompactOppositeStrand(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void delegateCompactStartZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.startZeroBased());
        }
    }

    @Benchmark
    public void delegateCompactStartOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.startOneBased());
        }
    }

    @Benchmark
    public void delegateCompactToZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void delegateCompactToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateCompactVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    // DelegateDefault
    @Benchmark
    public void delegateDefaultStart(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void delegateDefaultEnd(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void delegateDefaultLength(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void delegateDefaultRef(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void delegateDefaultAlt(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void delegateDefaultOppositeStrand(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void delegateDefaultStartZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.startZeroBased());
        }
    }

    @Benchmark
    public void delegateDefaultStartOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.startOneBased());
        }
    }

    @Benchmark
    public void delegateDefaultToZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void delegateDefaultToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : delegateDefaultVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    // InheritanceDelegate
    @Benchmark
    public void inheritanceCompactStart(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.start());
        }
    }

    @Benchmark
    public void inheritanceCompactEnd(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.end());
        }
    }

    @Benchmark
    public void inheritanceCompactLength(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.length());
        }
    }

    @Benchmark
    public void inheritanceCompactRef(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.ref());
        }
    }

    @Benchmark
    public void inheritanceCompactAlt(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.alt());
        }
    }

    @Benchmark
    public void inheritanceCompactOppositeStrand(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.toOppositeStrand());
        }
    }

    @Benchmark
    public void inheritanceCompactStartZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.startZeroBased());
        }
    }

    @Benchmark
    public void inheritanceCompactStartOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.startOneBased());
        }
    }

    @Benchmark
    public void inheritanceCompactToZeroBased(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.toZeroBased());
        }
    }

    @Benchmark
    public void inheritanceCompactToOneBased(Blackhole blackhole) {
        for (GenomicVariant variant : inheritanceCompactVariants) {
            blackhole.consume(variant.toOneBased());
        }
    }

    static class InheritanceBasedDelegateGenomicVariant extends AbstractGenomicVariant<InheritanceBasedDelegateGenomicVariant> {

        private final Genotype genotype;

        protected InheritanceBasedDelegateGenomicVariant(GenomicVariant genomicVariant, Genotype genotype) {
            super(genomicVariant);
            this.genotype = genotype;
        }

        /**
         * @param genomicVariant 
         * @return
         */
        @Override
        protected InheritanceBasedDelegateGenomicVariant newVariantInstance(GenomicVariant genomicVariant) {
            return new InheritanceBasedDelegateGenomicVariant(genomicVariant, genotype);
        }
    }
    
    /**
     *
     */
    interface DelegateGenomicVariant extends GenomicVariant {

        GenomicVariant genomicVariant();

        /**
         * @return
         */
        @Override
        default Contig contig() {
            return genomicVariant().contig();
        }

        /**
         * @return
         */
        @Override
        default String id() {
            return genomicVariant().id();
        }

        /**
         * @return
         */
        @Override
        default String ref() {
            return genomicVariant().ref();
        }

        /**
         * @return
         */
        @Override
        default String alt() {
            return genomicVariant().alt();
        }

        /**
         * @return
         */
        @Override
        default int changeLength() {
            return genomicVariant().changeLength();
        }


        /**
         * @return
         */
        @Override
        default Coordinates coordinates() {
            return genomicVariant().coordinates();
        }

        /**
         * @return
         */
        @Override
        default Strand strand() {
            return genomicVariant().strand();
        }
    }

    enum Genotype {
        HET, HOM
    }

    record DelegateGenotypesVariant(GenomicVariant genomicVariant,
                                    Genotype genotype) implements DelegateGenomicVariant {

        @Override
        public DelegateGenotypesVariant withStrand(Strand other) {
            if (strand() == other) {
                return this;
            }
            return new DelegateGenotypesVariant(genomicVariant.withStrand(other), genotype);
        }

        /**
         * @param coordinateSystem
         * @return
         */
        @Override
        public DelegateGenotypesVariant withCoordinateSystem(CoordinateSystem coordinateSystem) {
            if (coordinateSystem() == coordinateSystem) {
                return this;
            }
            return new DelegateGenotypesVariant(genomicVariant.withCoordinateSystem(coordinateSystem), genotype);
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

    }
}
