package org.example;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class UnsynchronizedByteArrayOutputStreamBench extends BenchmarkLauncher {

    private final static byte[] array = new byte[1024 * 16];

    static {
        new Random().nextBytes(array);
    }

    @Benchmark
    public void array(Blackhole blackhole) throws IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            bos.write(array);
            bos.flush();
            try (InputStream bis = bos.toInputStream()) {
                blackhole.consume(IOUtils.toByteArray(bis));
            }
        }
    }
}
