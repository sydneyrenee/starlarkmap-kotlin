// port-lint: source src/hashValue.rs
package io.github.kotlinmania.starlarkmap

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
 * A hash value.
 */
@ConsistentCopyVisibility
data class StarlarkHashValue private constructor(
    private val value: UInt,
) {
    companion object {
        /**
         * Create a new [StarlarkHashValue] for the given key.
         */
        fun new(key: Any?): StarlarkHashValue {
            val hasher = StarlarkHasher()
            when (key) {
                null -> hasher.writeU8(0u)
                is Boolean -> hasher.writeU8(if (key) 1u else 0u)
                is Int -> hasher.writeU32(key)
                is UInt -> hasher.writeU32(key)
                is Long -> hasher.writeU64(key.toULong())
                is ULong -> hasher.writeU64(key)
                is ByteArray -> hasher.write(key)
                is String -> hasher.write(key.encodeToByteArray())
                is StarlarkHashable -> key.writeHash(hasher)
                else -> hasher.writeU32(key.hashCode())
            }
            return hasher.finishSmall()
        }

        /**
         * Directly create a new [StarlarkHashValue] using a hash.
         *
         * The expectation is that the key will be well-swizzled, or there may be many hash
         * collisions.
         */
        fun newUnchecked(hash: UInt): StarlarkHashValue = StarlarkHashValue(hash)

        /**
         * Hash 64-bit integer.
         *
         * Input can also be a non-well swizzled hash to create better hash.
         */
        fun hash64(h: ULong): StarlarkHashValue {
            // `fmix64` function from MurMur3 hash (public domain).
            var x = h
            x = x xor (x shr 33)
            x *= 0xff51afd7ed558ccdUL
            x = x xor (x shr 33)
            x *= 0xc4ceb9fe1a85ec53UL
            x = x xor (x shr 33)
            return newUnchecked(x.toUInt())
        }
    }

    fun get(): UInt = value

    /**
     * Make `ULong` hash from this hash.
     *
     * The resulting hash should be good enough to be used in hashbrown hashtable.
     */
    fun promote(): ULong {
        return mixU32(value)
    }
}

/**
 * Kotlin equivalent for hashing into [StarlarkHasher].
 */
fun interface StarlarkHashable {
    fun writeHash(hasher: StarlarkHasher)
}
