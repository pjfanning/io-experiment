package org.example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class ByteArrayOutputStreamBench extends BenchmarkLauncher {

    private final static byte[] array = new byte[1024 * 16];

    static {
        new Random().nextBytes(array);
    }

    @Benchmark
    public void array(Blackhole blackhole) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(array);
            bos.flush();
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                blackhole.consume(IOUtils.toByteArray(bis));
            }
        }
    }
}
