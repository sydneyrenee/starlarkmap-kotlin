// port-lint: source hashed.rs
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
 * A key and its hash.
 *
 * [hashCode] hashes only the hash field, not the key.
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
     * Get the underlying key, as mutable.
     */
    fun keyMut(): K = key

    /**
     * Get the underlying key taking ownership.
     */
    fun intoKey(): K = key

    /**
     * Get the underlying hash.
     */
    fun hash(): StarlarkHashValue = hash

    /**
     * Hash only the hash field, not the key.
     */
    fun hash(state: StarlarkHasher) {
        state.writeU32(hash.get())
    }

    /**
     * Strong hash this value using the key, not the weak hash.
     */
    fun strongHash(state: StarlarkHasher) {
        val value = key
        when (value) {
            is StarlarkStrongHashable -> value.writeStrongHash(state)
            is StarlarkHashable -> value.writeHash(state)
            null -> state.writeU8(0u)
            is Boolean -> state.writeU8(if (value) 1u else 0u)
            is Int -> state.writeU32(value)
            is UInt -> state.writeU32(value)
            is Long -> state.writeU64(value.toULong())
            is ULong -> state.writeU64(value)
            is ByteArray -> state.write(value)
            is String -> state.write(value.encodeToByteArray())
            else -> state.writeU32(value.hashCode())
        }
    }

    /**
     * Get the underlying key.
     */
    fun deref(): K = key

    /**
     * Format the underlying key.
     */
    fun fmt(): String = key.toString()

    /**
     * Convert this hash/key pair to a borrowed reference. Kotlin references are already
     * references, so this returns [this].
     */
    fun asRef(): Hashed<K> = this

    /**
     * Make an owned hash/key pair from a borrowed reference. Kotlin values are already
     * owned by the runtime, so this returns [this].
     */
    fun owned(): Hashed<K> = this

    /**
     * Make an owned hash/key pair from a copied borrowed reference. Kotlin values are
     * already owned by the runtime, so this returns [this].
     *
     * Kotlin references are already values/references, so this returns [this].
     */
    fun copied(): Hashed<K> = this

    /**
     * Make an owned hash/key pair from a cloned borrowed reference. Kotlin values are
     * already owned by the runtime, so this returns [this].
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

/**
 * Kotlin equivalent for values that provide a stable strong hash.
 */
fun interface StarlarkStrongHashable {
    fun writeStrongHash(hasher: StarlarkHasher)
}

/**
 * Compare the underlying key.
 */
fun <K : Comparable<K>> Hashed<K>.partialCmp(other: Hashed<K>): Int? {
    return key().compareTo(other.key())
}

/**
 * Compare the underlying key.
 */
fun <K : Comparable<K>> Hashed<K>.cmp(other: Hashed<K>): Int {
    return key().compareTo(other.key())
}
