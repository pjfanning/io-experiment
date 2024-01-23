package org.example.scala

import java.io.InputStream

/**
 * Simple, fast and repositionable byte-array input stream.
 * It is derived from
 * https://github.com/vigna/fastutil/blob/master/src/it/unimi/dsi/fastutil/io/FastByteArrayInputStream.java
 *
 * <p><strong>Warning</strong>: this class implements the correct semantics
 * of {@link #read(byte[], int, int)} as described in {@link java.io.InputStream}.
 * The implementation given in {@link java.io.ByteArrayInputStream} is broken,
 * but it will never be fixed because it's too late.
 */
class FastByteArrayInputStream(array: Array[Byte], offset: Int, length: Int) extends InputStream {

  /** The current position as a distance from {@link # offset}. */
  private var pos: Int = 0

  /** The current mark as a position, or -1 if no mark exists. */
  private var mark: Int = 0

  /**
   * Creates a new array input stream using a given array.
   *
   * @param array the backing array.
   */
  def this(array: Array[Byte]) = {
    this(array, 0, array.length)
  }

  override def markSupported = true

  override def reset(): Unit = {
    pos = mark
  }

  /** Closing a fast byte array input stream has no effect. */
  override def close(): Unit = {
  }

  override def mark(dummy: Int): Unit = {
    mark = pos
  }

  override def available: Int = length - pos

  override def skip(n: Long): Long = {
    val newPos = length - pos
    if (n <= newPos) {
      pos += n.toInt
      n
    } else {
      pos = length
      newPos
    }
  }

  override def read: Int = {
    if (length == pos) {
      -1
    } else {
      val idx = offset + pos
      pos += 1
      array(idx) & 0xFF
    }
  }

  /**
   * Reads bytes from this byte-array input stream as
   * specified in {@link java.io.InputStream# read ( byte [ ], int, int)}.
   * Note that the implementation given in {@link java.io.ByteArrayInputStream# read ( byte [ ], int, int)}
   * will return -1 on a zero-length read at EOF, contrarily to the specification. We won't.
   */
  override def read(b: Array[Byte], offset: Int, length: Int): Int = {
    if (this.length == this.pos) {
      if (length == 0) 0 else -1
    } else {
      val n = Math.min(length, this.length - this.pos)
      System.arraycopy(array, this.offset + this.pos, b, offset, n)
      this.pos += n
      n
    }
  }

  def position(): Long = pos

  def position(newPosition: Long): Unit = {
    pos = Math.min(newPosition, length).toInt
  }

  def length(): Long = length
}
