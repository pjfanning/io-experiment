# Java I/O Experiments

This borrows simplified versions of Spring's [FastByteArrayOutputStream](https://github.com/vigna/fastutil/blob/master/src/it/unimi/dsi/fastutil/io/FastByteArrayInputStream.java)
and Unimi's [FastByteArrayInputStream](https://github.com/vigna/fastutil/blob/master/src/it/unimi/dsi/fastutil/io/FastByteArrayInputStream.java).

All code is Apache Licensed.

This target is to have some code that can be used in Apache Pekko to replace the use of Java ByteArrayInputStream and ByteArrayOutputStream.

## FastByteArrayInputStream

* this class is not synchronized like ByteArrayInputStream
* I've kept the Unimi code that allows the position to be reset so that data can be skipped or re-read - probably not useful for us but may still be worth keeping.

## FastByteArrayOutputStream

* this class is not synchronized like ByteArrayOutputStream
* it has an optimised `getInputStream` that let's you read back the data stored in the FastByteArrayOutputStream - without cloing the stored data
* it also has a `toByteArrayUnsafe` - which gives you the raw data without cloning it - `getInputStream` should be used unless you absolutely need the raw array
