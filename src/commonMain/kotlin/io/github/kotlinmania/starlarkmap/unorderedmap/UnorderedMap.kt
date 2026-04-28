// port-lint: source src/unorderedMap.rs
package io.github.kotlinmania.starlarkmap.unorderedmap

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

/**
 * Hash map which does not expose any insertion order-specific behavior
 * (except [toString]).
 *
 * In Kotlin, we import a [HashMap] which provides the same semantics.
 */
class UnorderedMap<K, V> internal constructor(
    internal val table: HashMap<K, V>,
) {

    companion object {
        /** Create a new empty map. */
        fun <K, V> new(): UnorderedMap<K, V> = UnorderedMap(HashMap())

        /** Create a new empty map with the specified capacity. */
        fun <K, V> withCapacity(n: Int): UnorderedMap<K, V> = UnorderedMap(HashMap(n))

        fun <K, V> default(): UnorderedMap<K, V> = new()

        /**
         * Create an [UnorderedMap] from an iterable of key-value pairs.
         */
        fun <K, V> fromIterator(iter: Iterable<Pair<K, V>>): UnorderedMap<K, V> {
            val map = HashMap<K, V>()
            for ((k, v) in iter) {
                map[k] = v
            }
            return UnorderedMap(map)
        }
    }

    /** Get the number of elements in the map. */
    fun len(): Int = table.size

    /** Is the map empty? */
    fun isEmpty(): Boolean = table.isEmpty()

    /**
     * Get a reference to the value associated with the given key.
     */
    fun get(key: K): V? = table[key]

    /**
     * Get a reference to the value associated with the given key using [Equivalent].
     * This performs a linear scan since [Equivalent] may not match [hashCode]/[equals] semantics.
     */
    fun <Q> get(key: Q): V? where Q : Equivalent<K> {
        for ((k, v) in table) {
            if (key.equivalent(k)) return v
        }
        return null
    }

    /**
     * Does the map contain the specified key?
     */
    fun containsKey(key: K): Boolean = table.containsKey(key)

    /** Does the map contain the specified key using [Equivalent]? */
    fun <Q> containsKey(key: Q): Boolean where Q : Equivalent<K> = get(key) != null

    /**
     * Insert an entry into the map.
     */
    fun insert(key: K, value: V): V? {
        val old = table[key]
        table[key] = value
        return old
    }

    /**
     * Remove an entry from the map.
     */
    fun remove(key: K): V? = table.remove(key)

    /** Remove an entry using [Equivalent]. */
    fun <Q> remove(key: Q): V? where Q : Equivalent<K> {
        val k = table.keys.firstOrNull { key.equivalent(it) } ?: return null
        return table.remove(k)
    }

    /**
     * Preserve only the elements specified by the predicate.
     */
    fun retain(predicate: (K, V) -> Boolean) {
        val iter = table.entries.iterator()
        while (iter.hasNext()) {
            val (k, v) = iter.next()
            if (!predicate(k, v)) {
                iter.remove()
            }
        }
    }

    /**
     * Get an entry in the map for in-place manipulation.
     */
    fun entry(key: K): Entry<K, V> {
        return if (table.containsKey(key)) {
            Entry.Occupied(OccupiedEntry(this, key))
        } else {
            Entry.Vacant(VacantEntry(this, key))
        }
    }

    /**
     * Lower-level access to the entry API.
     */
    fun rawEntryMut(): RawEntryBuilderMut<K, V> = RawEntryBuilderMut(this)

    /**
     * Does the map contain the specified key (pre-hashed)?
     */
    fun containsKeyHashed(key: K): Boolean = table.containsKey(key)

    /**
     * Clear the map, removing all entries.
     */
    fun clear() = table.clear()

    /**
     * Entries in the map, in arbitrary order.
     */
    fun entriesUnordered(): Sequence<Pair<K, V>> =
        table.entries.asSequence().map { Pair(it.key, it.value) }

    /**
     * Keys in the map, in arbitrary order.
     */
    fun keysUnordered(): Sequence<K> = table.keys.asSequence()

    /**
     * Values in the map, in arbitrary order.
     */
    fun valuesUnordered(): Sequence<V> = table.values.asSequence()

    /**
     * Get the entries in the map, sorted by key.
     */
    @Suppress("UNCHECKED_CAST")
    fun entriesSorted(): List<Pair<K, V>> =
        entriesUnordered().sortedWith(compareBy { it.first as Comparable<Any> }).toList()

    /**
     * Convert into a [HashMap].
     */
    fun intoHashMap(): HashMap<K, V> = HashMap(table)

    /**
     * Apply the function to each value.
     */
    fun <W> mapValues(f: (V) -> W): UnorderedMap<K, W> {
        val map = HashMap<K, W>(table.size)
        for ((k, v) in table) {
            map[k] = f(v)
        }
        return UnorderedMap(map)
    }

    /**
     * Index by key. Throws if key is not found.
     */
    operator fun get(key: K, default: Nothing? = null): V =
        table[key] ?: throw NoSuchElementException("key not found: $key")

    /**
     * Unordered equality: two maps are equal iff they have the same entries,
     * regardless of iteration order.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnorderedMap<*, *>) return false
        return table == other.table
    }

    /**
     * Order-independent hash.
     *
     * We import XOR-of-hashes (commutative) so that insertion order does not affect the hash.
     */
    override fun hashCode(): Int {
        var sum = 0
        for ((k, v) in table) {
            sum = sum xor (k.hashCode() * 31 + v.hashCode())
        }
        return len() * 31 + sum
    }

    override fun toString(): String {
        return table.toString()
    }
}

/**
 * Reference to an entry in a [UnorderedMap].
 */
sealed class Entry<K, V> {
    /** Occupied entry. */
    class Occupied<K, V>(val entry: OccupiedEntry<K, V>) : Entry<K, V>()

    /** Vacant entry. */
    class Vacant<K, V>(val entry: VacantEntry<K, V>) : Entry<K, V>()

    /** Insert a value if vacant, or return the existing value. */
    fun orInsert(default: V): V {
        return when (this) {
            is Occupied -> entry.get()
            is Vacant -> {
                entry.insert(default)
                default
            }
        }
    }

    /** Insert a value computed by a function if vacant, or return the existing value. */
    fun orInsertWith(default: () -> V): V {
        return when (this) {
            is Occupied -> entry.get()
            is Vacant -> {
                val v = default()
                entry.insert(v)
                v
            }
        }
    }
}

/**
 * Reference to an occupied entry in a [UnorderedMap].
 */
class OccupiedEntry<K, V>(
    private val map: UnorderedMap<K, V>,
    private val key: K,
) {
    /** Get a reference to the value associated with the entry. */
    fun get(): V = map.table[key]!!

    /** Replace the value associated with the entry. Returns the old value. */
    fun insert(value: V): V {
        val old = map.table[key]!!
        map.table[key] = value
        return old
    }

    /** Remove the entry from the map and return the value. */
    fun remove(): V = map.table.remove(key)!!
}

/**
 * Reference to a vacant entry in a [UnorderedMap].
 */
class VacantEntry<K, V>(
    private val map: UnorderedMap<K, V>,
    private val key: K,
) {
    /** Insert a value into the map. */
    fun insert(value: V) {
        map.table[key] = value
    }
}

/**
 * Builder for [RawEntryMut].
 */
class RawEntryBuilderMut<K, V>(
    private val map: UnorderedMap<K, V>,
) {
    /**
     * Find an entry by key.
     */
    fun fromKey(key: K): RawEntryMut<K, V> {
        return if (map.table.containsKey(key)) {
            RawEntryMut.Occupied(RawOccupiedEntryMut(map, key))
        } else {
            RawEntryMut.Vacant(RawVacantEntryMut(map))
        }
    }

    /**
     * Find an entry by hashed key.
     */
    fun fromKeyHashed(key: Hashed<K>): RawEntryMut<K, V> = fromKey(key.key())

    /**
     * Find an entry by hash and equality function.
     *
     * Since Kotlin's [HashMap] does not expose hash-level access, this performs a linear scan.
     */
    fun fromHash(hash: StarlarkHashValue, isMatch: (K) -> Boolean): RawEntryMut<K, V> {
        for (k in map.table.keys) {
            if (isMatch(k)) {
                return RawEntryMut.Occupied(RawOccupiedEntryMut(map, k))
            }
        }
        return RawEntryMut.Vacant(RawVacantEntryMut(map))
    }
}

/**
 * Raw entry in a [UnorderedMap].
 */
sealed class RawEntryMut<K, V> {
    /** Occupied entry. */
    class Occupied<K, V>(val entry: RawOccupiedEntryMut<K, V>) : RawEntryMut<K, V>()
    /** Vacant entry. */
    class Vacant<K, V>(val entry: RawVacantEntryMut<K, V>) : RawEntryMut<K, V>()
}

/**
 * Reference to an occupied raw entry in a [UnorderedMap].
 */
class RawOccupiedEntryMut<K, V>(
    private val map: UnorderedMap<K, V>,
    private var key: K,
) {
    /** Get a reference to the value. */
    fun get(): V = map.table[key]!!

    /** Replace the value. Returns the old value. */
    fun insert(value: V): V {
        val old = map.table[key]!!
        map.table[key] = value
        return old
    }

    /** Replace the key. Returns the old key. */
    fun insertKey(newKey: K): K {
        val oldKey = key
        val value = map.table.remove(oldKey)!!
        map.table[newKey] = value
        key = newKey
        return oldKey
    }

    /** Remove the entry and return the value. */
    fun remove(): V = map.table.remove(key)!!

    /** Remove the entry and return the key-value pair. */
    fun removeEntry(): Pair<K, V> {
        val v = map.table.remove(key)!!
        return Pair(key, v)
    }
}

/**
 * Reference to a vacant raw entry in a [UnorderedMap].
 */
class RawVacantEntryMut<K, V>(
    private val map: UnorderedMap<K, V>,
) {
    /** Insert an entry. Computes the hash of the key. */
    fun insert(key: K, value: V) {
        map.table[key] = value
    }

    /** Insert an entry with a pre-computed hash. */
    fun insertHashed(key: Hashed<K>, value: V) {
        map.table[key.intoKey()] = value
    }
}
