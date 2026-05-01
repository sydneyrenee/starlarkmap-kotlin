// port-lint: source hasher.rs
package io.github.kotlinmania.starlarkmap

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A hasher used by Starlark implementation.
 *
 * Starlark relies on stable hashing, and this is the hasher.
 *
 * This Kotlin port implements the same algorithm and reads bytes as little-endian
 * (matching common target architectures).
 */
class StarlarkHasher {
    companion object {
        /**
         * Creates a new hasher.
         */
        fun new(): StarlarkHasher = StarlarkHasher()
    }

    private val fx: FxHasher64 = FxHasher64()

    /**
     * Finish the hash computation and return the result.
     */
    fun finish(): ULong = fx.finish()

    /**
     * Finish the hash computation and return the lower 32 bits as a [StarlarkHashValue].
     *
     */
    fun finishSmall(): StarlarkHashValue {
        return StarlarkHashValue.newUnchecked(finish().toUInt())
    }

    fun write(bytes: ByteArray) {
        fx.write(bytes)
    }

    fun writeU8(i: UInt) {
        fx.writeU8(i.toUByte())
    }

    fun writeU16(i: UInt) {
        fx.writeU16(i.toUShort())
    }

    fun writeU32(i: Int) {
        fx.writeU32(i.toUInt())
    }

    fun writeU32(i: UInt) {
        fx.writeU32(i)
    }

    fun writeU64(i: ULong) {
        fx.writeU64(i)
    }

    fun writeU128(low: ULong, high: ULong = 0UL) {
        fx.writeU128(low, high)
    }

    fun writeUsize(i: ULong) {
        fx.writeUsize(i)
    }
}

/**
 * `BuildHasher` implementation which produces [StarlarkHasher].
 */
class StarlarkHasherBuilder {
    fun buildHasher(): StarlarkHasher = StarlarkHasher()
}

private class FxHasher64 {
    private var hash: ULong = 0UL

    fun write(bytes: ByteArray) {
        hash = write64(hash, bytes)
    }

    fun writeU8(i: UByte) {
        hash = hashWord(hash, i.toULong())
    }

    fun writeU16(i: UShort) {
        hash = hashWord(hash, i.toULong())
    }

    fun writeU32(i: UInt) {
        hash = hashWord(hash, i.toULong())
    }

    fun writeU64(i: ULong) {
        hash = hashWord(hash, i)
    }

    fun writeU128(low: ULong, high: ULong) {
        writeU64(low)
        writeU64(high)
    }

    fun writeUsize(i: ULong) {
        writeU64(i)
    }

    fun finish(): ULong = hash
}

private const val FX_ROTATE: Int = 5
private const val FX_SEED64: ULong = 0x517cc1b727220a95UL

private fun rotateLeft64(x: ULong, bits: Int): ULong {
    val n = bits and 63
    if (n == 0) return x
    return (x shl n) or (x shr (64 - n))
}

private fun hashWord(hash: ULong, word: ULong): ULong {
    return (rotateLeft64(hash, FX_ROTATE) xor word) * FX_SEED64
}

private fun write64(initial: ULong, bytes: ByteArray): ULong {
    var hash = initial

    var offset = 0
    while (bytes.size - offset >= 8) {
        val n = readU64Le(bytes, offset)
        hash = hashWord(hash, n)
        offset += 8
    }

    if (bytes.size - offset >= 4) {
        val n = readU32Le(bytes, offset)
        hash = hashWord(hash, n.toULong())
        offset += 4
    }

    while (offset < bytes.size) {
        hash = hashWord(hash, bytes[offset].toUByte().toULong())
        offset += 1
    }

    return hash
}

private fun readU32Le(bytes: ByteArray, offset: Int): UInt {
    return (bytes[offset + 0].toUByte().toUInt()) or
        (bytes[offset + 1].toUByte().toUInt() shl 8) or
        (bytes[offset + 2].toUByte().toUInt() shl 16) or
        (bytes[offset + 3].toUByte().toUInt() shl 24)
}

private fun readU64Le(bytes: ByteArray, offset: Int): ULong {
    return (bytes[offset + 0].toUByte().toULong()) or
        (bytes[offset + 1].toUByte().toULong() shl 8) or
        (bytes[offset + 2].toUByte().toULong() shl 16) or
        (bytes[offset + 3].toUByte().toULong() shl 24) or
        (bytes[offset + 4].toUByte().toULong() shl 32) or
        (bytes[offset + 5].toUByte().toULong() shl 40) or
        (bytes[offset + 6].toUByte().toULong() shl 48) or
        (bytes[offset + 7].toUByte().toULong() shl 56)
}
