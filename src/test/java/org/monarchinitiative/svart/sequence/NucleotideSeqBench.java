package org.monarchinitiative.svart.sequence;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class NucleotideSeqBench {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .forks(1)
                .include(NucleotideSeqBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    private static final byte[] BYTES = new byte[] {'a', 'A', 't', 'T', 'c', 'C', 'g', 'G', 'n', 'N', '*', '.'};
    private static final String STRING = new String(BYTES);

    @Benchmark
    public void revCompByteArray(Blackhole blackhole) {
        blackhole.consume(NucleotideSeq.reverseComplement(BYTES));
    }

    @Benchmark
    public void revCompString(Blackhole blackhole) {
        blackhole.consume(NucleotideSeq.reverseComplement(STRING));
    }

    @Benchmark
    public void revCompStringSingleBase(Blackhole blackhole) {
        blackhole.consume(NucleotideSeq.reverseComplement("A"));
    }
}
