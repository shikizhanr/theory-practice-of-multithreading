package com.example.crawler;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class CrawlerBenchmark {

    private List<String> dummyData;

    @Setup
    public void setup() {
        dummyData = new ArrayList<>();
        // Эмулируем 100 000 контактов из БД
        for (int i = 0; i < 100000; i++) {
            dummyData.add("test_contact_" + i + "@domain.com");
        }
    }

    @Benchmark
    public long testStandardForLoop() {
        long count = 0;
        for (String s : dummyData) {
            if (s.contains("999")) count++;
        }
        return count;
    }

    @Benchmark
    public long testSequentialStream() {
        return dummyData.stream().filter(s -> s.contains("999")).count();
    }

    @Benchmark
    public long testParallelStream() {
        return dummyData.parallelStream().filter(s -> s.contains("999")).count();
    }

    // Метод для простого запуска прямо из IDE
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(CrawlerBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}