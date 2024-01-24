package org.example.scala;

import org.example.BenchmarkLauncher;
import org.example.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.Random;

public class ScalaFastByteArrayInputStreamBench extends BenchmarkLauncher {

    private final static byte[] array = new byte[1024 * 16];

    static {
        new Random().nextBytes(array);
    }

    @Benchmark
    public void array(Blackhole blackhole) throws IOException {
        try (FastByteArrayInputStream stream = new FastByteArrayInputStream(array)) {
            stream.markSupported();
            blackhole.consume(IOUtils.toByteArray(stream));
        }
    }
}
