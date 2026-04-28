// port-lint: source src/hashed.rs
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
 * A key and its hash.
 *
 * Note: in Rust, `Hash` for `Hashed<K>` hashes only the hash field (not the key).
 * Kotlin uses [hashCode] for hashed collections, so we mirror that behaviour by
 * hashing only [hash].
 */
class Hashed<out K> internal constructor(
    internal val hash: StarlarkHashValue,
    internal val key: K,
) {
    companion object {
        /**
         * Create a new [Hashed] value using the hash of the key.
         */
        fun <K> new(key: K): Hashed<K> {
            return newUnchecked(StarlarkHashValue.new(key), key)
        }

        /**
         * Directly create a new [Hashed] using a given hash value.
         * If the hash does not correspond to the key, it will cause issues.
         */
        fun <K> newUnchecked(hash: StarlarkHashValue, key: K): Hashed<K> {
            return Hashed(hash, key)
        }
    }

    /**
     * Get the underlying key.
     */
    fun key(): K = key

    /**
     * Get the underlying key taking ownership.
     */
    fun intoKey(): K = key

    /**
     * Get the underlying hash.
     */
    fun hash(): StarlarkHashValue = hash

    /**
     * Convert `Hashed<K>` to `Hashed<&K>` in Rust. Kotlin references are already by reference,
     * so this returns [this].
     */
    fun asRef(): Hashed<K> = this

    /**
     * Make `Hashed<K>` from `Hashed<&K>` in Rust when `K: Copy`.
     *
     * Kotlin references are already values/references, so this returns [this].
     */
    fun copied(): Hashed<K> = this

    /**
     * Make `Hashed<K>` from `Hashed<&K>` in Rust when `K: Clone`.
     *
     * Kotlin references are already values/references, so this returns [this].
     */
    fun cloned(): Hashed<K> = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Hashed<*>) return false
        return hash == other.hash && key == other.key
    }

    override fun hashCode(): Int {
        // Only hash the hash, not the key.
        return hash.hashCode()
    }

    override fun toString(): String {
        return key.toString()
    }
}
