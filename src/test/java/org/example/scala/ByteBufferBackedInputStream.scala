package org.example.scala

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Simple {@link InputStream} implementation that exposes currently
 * available content of a {@link ByteBuffer}.
 *
 * Derived from https://github.com/FasterXML/jackson-databind/blob/1e73db1fabd181937c68b49ffc502fb7f614d0c2/src/main/java/com/fasterxml/jackson/databind/util/ByteBufferBackedInputStream.java
 */
class ByteBufferBackedInputStream(bb: ByteBuffer) extends InputStream {
  override def available: Int = bb.remaining

  @throws[IOException]
  override def read: Int = {
    if (bb.hasRemaining) bb.get & 0xFF
    else -1
  }

  @throws[IOException]
  override def read(bytes: Array[Byte], off: Int, len: Int): Int = {
    if (!bb.hasRemaining) {
      -1
    } else {
      val newLen = Math.min(len, bb.remaining)
      bb.get(bytes, off, newLen)
      newLen
    }
  }
}
