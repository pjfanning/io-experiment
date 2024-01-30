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

package org.example.scala

import java.io.{IOException, InputStream, OutputStream}
import java.nio.charset.Charset
import java.util

private object FastByteArrayOutputStream {
  private val DefaultBlockSize = 256
}

/**
 * A speedy alternative to {@link java.io.ByteArrayOutputStream}.
 * It is derived from
 * https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/util/FastByteArrayOutputStream.java
 *
 * <p>Unlike {@link java.io.ByteArrayOutputStream}, this implementation is backed
 * by a {@link java.util.ArrayDeque} of {@code byte[]} buffers instead of one
 * constantly resizing {@code byte[]}. It does not copy buffers when it gets expanded.
 *
 * <p>The initial buffer is only created when the stream is first written.
 * There is also no copying of the internal buffers if the stream's content is
 * extracted via the {@link # writeTo ( OutputStream )} method.
 */
class FastByteArrayOutputStream(private var initialBlockSize: Int) extends OutputStream {

  def this() = this(FastByteArrayOutputStream.DefaultBlockSize)

  // The buffers used to store the content bytes
  private val buffers: util.Deque[Array[Byte]] = new util.ArrayDeque[Array[Byte]]

  // The size, in bytes, to use when allocating the next byte[]
  private var nextBlockSize: Int = 0

  // The number of bytes in previous buffers.
  // (The number of bytes in the current buffer is in 'index'.)
  private var alreadyBufferedSize: Int = 0

  // The index in the byte[] found at buffers.getLast() to be written next
  private var index: Int = 0

  // Is the stream closed?
  private var closed: Boolean = false

  @throws[IOException]
  override def write(datum: Int): Unit = {
    if (this.closed) throw new IOException("Stream closed")
    else {
      if (this.buffers.peekLast == null || this.buffers.getLast.length == this.index) addBuffer(1)
      // store the byte
      this.buffers.getLast()(this.index) = datum.toByte
      this.index += 1
    }
  }

  @throws[IOException]
  override def write(data: Array[Byte], offset: Int, length: Int): Unit = {
    var l = length
    if (offset < 0 || offset + l > data.length || l < 0) throw new IndexOutOfBoundsException
    else if (this.closed) throw new IOException("Stream closed")
    else {
      if (this.buffers.peekLast == null || this.buffers.getLast.length == this.index) addBuffer(l)
      if (this.index + l > this.buffers.getLast.length) {
        var pos = offset
        do {
          if (this.index == this.buffers.getLast.length) addBuffer(l)
          var copyLength = this.buffers.getLast.length - this.index
          if (l < copyLength) copyLength = l
          System.arraycopy(data, pos, this.buffers.getLast, this.index, copyLength)
          pos += copyLength
          this.index += copyLength
          l -= copyLength
        } while (l > 0)
      }
      else {
        // copy in the sub-array
        System.arraycopy(data, offset, this.buffers.getLast, this.index, l)
        this.index += l
      }
    }
  }

  override def close(): Unit = {
    this.closed = true
  }

  /**
   * Convert this stream's contents to a string by decoding the bytes using the
   * platform's default character set. The length of the new {@code String}
   * is a function of the character set, and hence may not be equal to the
   * size of the buffers.
   * <p>This method always replaces malformed-input and unmappable-character
   * sequences with the default replacement string for the platform's
   * default character set. The {@linkplain java.nio.charset.CharsetDecoder}
   * class should be used when more control over the decoding process is
   * required.
   *
   * @return a String decoded from this stream's contents
   * @see #toString(Charset)
   */
  override def toString: String = toString(Charset.defaultCharset)

  /**
   * Convert this stream's contents to a string by decoding the bytes using the
   * specified {@link Charset}.
   *
   * @param charset the {@link Charset} to use to decode the bytes
   * @return a String decoded from this stream's contents
   * @see #toString()
   */
  def toString(charset: Charset): String = {
    if (size == 0) {
      ""
    } else if (this.buffers.size == 1) {
      new String(this.buffers.getFirst, 0, this.index, charset)
    } else {
      new String(toByteArrayUnsafe, charset)
    }
  }

  /**
   * Return the number of bytes stored in this {@code FastByteArrayOutputStream}.
   */
  def size: Int = this.alreadyBufferedSize + this.index

  /**
   * Convert this stream's contents to a byte array and return the byte array.
   * <p>Also replaces the internal structures with the byte array to
   * conserve memory: if the byte array is being created anyway, we might
   * as well as use it. This approach also means that if this method is
   * called twice without any writes in the interim, the second call is
   * a no-op.
   * <p>This method is "unsafe" as it returns the internal buffer.
   * Callers should not modify the returned buffer.
   *
   * @return the current contents of this stream as a byte array
   * @see #size()
   * @see #toByteArray()
   */
  def toByteArrayUnsafe: Array[Byte] = {
    val totalSize = size
    if (totalSize == 0) {
      Array.empty
    } else {
      resize(totalSize)
      this.buffers.getFirst
    }
  }

  /**
   * Create a newly allocated byte array.
   * <p>Its size is the current size of this output stream, and it will
   * contain the valid contents of the internal buffers.
   *
   * @return the current contents of this stream as a byte array
   * @see #size()
   * @see #toByteArrayUnsafe()
   */
  def toByteArray: Array[Byte] = {
    val bytesUnsafe = toByteArrayUnsafe
    bytesUnsafe.clone
  }

  // TODO implement this method using internal InputStream implementation
  def getInputStream(): InputStream = {
    new java.io.ByteArrayInputStream(toByteArrayUnsafe)
  }

  /**
   * Reset the contents of this {@code FastByteArrayOutputStream}.
   * <p>All currently accumulated output in the output stream is discarded.
   * The output stream can be used again.
   */
  def reset(): Unit = {
    this.buffers.clear()
    this.nextBlockSize = this.initialBlockSize
    this.closed = false
    this.index = 0
    this.alreadyBufferedSize = 0
  }

  /**
   * Resize the internal buffer size to the specified capacity.
   *
   * @param targetCapacity the desired size of the buffer
   * @throws IllegalArgumentException if the given capacity is smaller than
   *                                  the actual size of the content stored in the buffer already
   * @see FastByteArrayOutputStream#size()
   */
  def resize(targetCapacity: Int): Unit = {
    // Assert.isTrue(targetCapacity >= size(), "New capacity must not be smaller than current size");
    if (this.buffers.peekFirst == null) this.nextBlockSize = targetCapacity - size
    else if (size == targetCapacity && this.buffers.getFirst.length == targetCapacity) {
    }
    else {
      val totalSize = size
      val data = new Array[Byte](targetCapacity)
      var pos = 0
      val it = this.buffers.iterator
      while (it.hasNext) {
        val bytes = it.next
        if (it.hasNext) {
          System.arraycopy(bytes, 0, data, pos, bytes.length)
          pos += bytes.length
        }
        else System.arraycopy(bytes, 0, data, pos, this.index)
      }
      this.buffers.clear()
      this.buffers.add(data)
      this.index = totalSize
      this.alreadyBufferedSize = 0
    }
  }

  /**
   * Create a new buffer and store it in the ArrayDeque.
   * <p>Adds a new buffer that can store at least {@code minCapacity} bytes.
   */
  private def addBuffer(minCapacity: Int): Unit = {
    if (this.buffers.peekLast != null) {
      this.alreadyBufferedSize += this.index
      this.index = 0
    }
    if (this.nextBlockSize < minCapacity) this.nextBlockSize = nextPowerOf2(minCapacity)
    this.buffers.add(new Array[Byte](this.nextBlockSize))
    this.nextBlockSize *= 2 // block size doubles each time

  }

  /**
   * Get the next power of 2 of a number (ex, the next power of 2 of 119 is 128).
   */
  private def nextPowerOf2(init: Int) = {
    var v = init
    v -= 1
    v = (v >> 1) | v
    v = (v >> 2) | v
    v = (v >> 4) | v
    v = (v >> 8) | v
    v = (v >> 16) | v
    v += 1
    v
  }
}
