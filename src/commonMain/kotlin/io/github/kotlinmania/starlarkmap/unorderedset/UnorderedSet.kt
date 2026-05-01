// port-lint: source src/unorderedSet.rs
package io.github.kotlinmania.starlarkmap.unorderedset

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

import io.github.kotlinmania.starlarkmap.Equivalent
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.StarlarkHashValue
import io.github.kotlinmania.starlarkmap.unorderedmap.RawEntryBuilderMut as MapRawEntryBuilderMut
import io.github.kotlinmania.starlarkmap.unorderedmap.RawEntryMut as MapRawEntryMut
import io.github.kotlinmania.starlarkmap.unorderedmap.RawOccupiedEntryMut as MapRawOccupiedEntryMut
import io.github.kotlinmania.starlarkmap.unorderedmap.RawVacantEntryMut as MapRawVacantEntryMut
import io.github.kotlinmania.starlarkmap.unorderedmap.UnorderedMap

/**
 * `HashSet` that does not expose insertion order.
 *
 * In Kotlin, we wrap [UnorderedMap]<T, [Unit]> to maintain the same structure.
 */
class UnorderedSet<T> internal constructor(
    private val map: UnorderedMap<T, Unit>,
) {
    companion object {
        /** Create a new empty set. */
        fun <T> new(): UnorderedSet<T> = UnorderedSet(UnorderedMap.new())

        /** Create a new empty set with the specified capacity. */
        fun <T> withCapacity(n: Int): UnorderedSet<T> = UnorderedSet(UnorderedMap.withCapacity(n))

        fun <T> default(): UnorderedSet<T> = new()

        /**
         * Create an [UnorderedSet] from an iterable.
         */
        fun <T> fromIterator(iter: Iterable<T>): UnorderedSet<T> {
            val set = new<T>()
            for (v in iter) {
                set.insert(v)
            }
            return set
        }
    }

    /** Insert a value into the set. Returns `true` if the value was not already present. */
    fun insert(k: T): Boolean = map.insert(k, Unit) == null

    /** Clear the set, removing all values. */
    fun clear() = map.clear()

    /** Is the set empty? */
    fun isEmpty(): Boolean = map.isEmpty()

    /** Get the number of elements in the set. */
    fun len(): Int = map.len()

    /**
     * Does the set contain the specified value?
     */
    fun contains(value: T): Boolean = map.containsKey(value)

    /**
     * Does the set contain the specified value using [Equivalent]?
     */
    fun <Q> contains(value: Q): Boolean where Q : Equivalent<T> = map.containsKey(value)

    /**
     * Does the set contain the specified value (pre-hashed)?
     */
    fun containsHashed(value: T): Boolean = map.containsKeyHashed(value)

    /**
     * Lower-level access to the underlying map.
     */
    fun rawEntryMut(): RawEntryBuilderMut<T> = RawEntryBuilderMut(map.rawEntryMut())

    /** Iterate over the values in the set (private). */
    private fun iter(): Sequence<T> = map.keysUnordered()

    /**
     * Get the entries in the set, sorted.
     */
    @Suppress("UNCHECKED_CAST")
    fun entriesSorted(): List<T> =
        iter().sortedWith(compareBy { it as Comparable<Any> }).toList()

    /**
     * Unordered equality: two sets are equal iff they have the same elements,
     * regardless of iteration order.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnorderedSet<*>) return false
        return map == other.map
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}

/**
 * Builder for [RawEntryMut].
 */
class RawEntryBuilderMut<T>(
    private val entry: MapRawEntryBuilderMut<T, Unit>,
) {
    /**
     * Find the entry for a key.
     */
    fun fromEntry(value: T): RawEntryMut<T> {
        return when (val raw = entry.fromKey(value)) {
            is MapRawEntryMut.Occupied ->
                RawEntryMut.Occupied(RawOccupiedEntryMut(raw.entry))
            is MapRawEntryMut.Vacant ->
                RawEntryMut.Vacant(RawVacantEntryMut(raw.entry))
        }
    }

    /**
     * Find the entry for a pre-hashed key.
     */
    fun fromEntryHashed(value: Hashed<T>): RawEntryMut<T> = fromEntry(value.key())

    /**
     * Find the entry by hash and equality function.
     */
    fun fromHash(hash: StarlarkHashValue, isMatch: (T) -> Boolean): RawEntryMut<T> {
        return when (val raw = entry.fromHash(hash, isMatch)) {
            is MapRawEntryMut.Occupied ->
                RawEntryMut.Occupied(RawOccupiedEntryMut(raw.entry))
            is MapRawEntryMut.Vacant ->
                RawEntryMut.Vacant(RawVacantEntryMut(raw.entry))
        }
    }
}

/**
 * Reference to an entry in a [UnorderedSet].
 */
sealed class RawEntryMut<T> {
    /** Occupied entry. */
    class Occupied<T>(val entry: RawOccupiedEntryMut<T>) : RawEntryMut<T>()
    /** Vacant entry. */
    class Vacant<T>(val entry: RawVacantEntryMut<T>) : RawEntryMut<T>()
}

/**
 * Reference to an occupied entry in a [UnorderedSet].
 */
class RawOccupiedEntryMut<T>(
    private val entry: MapRawOccupiedEntryMut<T, Unit>,
) {
    /** Remove the entry. */
    fun remove(): T = entry.removeEntry().first

    /** Replace the entry. Returns the old value. */
    fun insert(value: T): T = entry.insertKey(value)
}

/**
 * Reference to a vacant entry in a [UnorderedSet].
 */
class RawVacantEntryMut<T>(
    private val entry: MapRawVacantEntryMut<T, Unit>,
) {
    /** Insert an entry to the set. Computes the hash of the key. */
    fun insert(value: T) {
        entry.insert(value, Unit)
    }

    /** Insert an entry to the set with a pre-computed hash. */
    fun insertHashed(value: Hashed<T>) {
        entry.insertHashed(value, Unit)
    }
}
