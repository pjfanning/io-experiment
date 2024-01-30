package org.example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class FastByteArrayOutputStreamBench extends BenchmarkLauncher {

    private final static byte[] array = new byte[1024 * 16];

    static {
        new Random().nextBytes(array);
    }

    @Benchmark
    public void array(Blackhole blackhole) throws IOException {
        try (FastByteArrayOutputStream bos = new FastByteArrayOutputStream()) {
            bos.write(array);
            bos.flush();
            try (InputStream bis = bos.getInputStream()) {
                blackhole.consume(IOUtils.toByteArray(bis));
            }
        }
    }
}
