/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.scala;

import org.example.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class FastByteArrayOutputStreamTest {
    @Test
    void testInputOutput() throws IOException {
        byte[] bytes = new byte[128];
        Random random = new Random();
        random.nextBytes(bytes);
        try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
            os.write(bytes);
            try (InputStream is = os.getInputStream()) {
                Assertions.assertArrayEquals(bytes, IOUtils.toByteArray(is));
            }
        }
    }
}
