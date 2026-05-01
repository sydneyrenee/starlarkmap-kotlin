// port-lint: source small_map.rs
package io.github.kotlinmania.starlarkmap.smallmap

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

/**
 * A map with deterministic iteration order.
 *
 * Kotlin does not have an equivalent to `hashbrown::HashTable` in commonMain, so this port
 * keeps the same observable behaviour while using a simple insertion-ordered storage.
 */
class SmallMap<K, V> internal constructor(
    internal val entries: ArrayList<Entry<K, V>>,
) {
    internal data class Entry<K, V>(
        val key: Hashed<K>,
        var value: V,
    )

    companion object {
        /**
         * Empty map.
         */
        fun <K, V> new(): SmallMap<K, V> = SmallMap(ArrayList())

        fun <K, V> default(): SmallMap<K, V> = new()

        /**
         * Create an empty map with specified capacity.
         */
        fun <K, V> withCapacity(n: Int): SmallMap<K, V> = SmallMap(ArrayList(n))

        fun <K, V> fromIterator(iter: Iterable<Pair<K, V>>): SmallMap<K, V> {
            val map = withCapacity<K, V>(if (iter is Collection<*>) iter.size else 0)
            for ((key, value) in iter) {
                map.insert(key, value)
            }
            return map
        }

        fun <K, V> fromIter(iter: Iterable<Pair<K, V>>): SmallMap<K, V> = fromIterator(iter)
    }

    fun maybeDropIndex() {
        // No-op in this Kotlin implementation.
    }

    fun keys(): Sequence<K> = entries.asSequence().map { it.key.key() }

    fun values(): Sequence<V> = entries.asSequence().map { it.value }

    fun valuesMut(): Sequence<V> = values()

    fun iter(): Sequence<Pair<K, V>> = entries.asSequence().map { Pair(it.key.key(), it.value) }

    fun iterMut(): Sequence<Pair<K, V>> = iter()

    fun iterMutUnchecked(): Sequence<Pair<K, V>> = iter()

    operator fun iterator(): Iterator<Pair<K, V>> = iter().iterator()

    fun intoIter(): Iterator<Pair<K, V>> = iterator()

    fun intoIterHashed(): Sequence<Pair<Hashed<K>, V>> = iterHashed()

    fun iterHashed(): Sequence<Pair<Hashed<K>, V>> = entries.asSequence().map { Pair(it.key, it.value) }

    fun reserve(additional: Int) {
        // No-op in this Kotlin implementation.
    }

    fun capacity(): Int = entries.size

    fun first(): Pair<K, V>? = entries.firstOrNull()?.let { Pair(it.key.key(), it.value) }

    fun last(): Pair<K, V>? = entries.lastOrNull()?.let { Pair(it.key.key(), it.value) }

    fun isEmpty(): Boolean = entries.isEmpty()

    fun len(): Int = entries.size

    fun clear() {
        entries.clear()
    }

    fun getIndex(index: Int): Pair<K, V>? {
        val e = entries.getOrNull(index) ?: return null
        return Pair(e.key.key(), e.value)
    }

    fun getHashedByValue(key: Hashed<K>): V? {
        val index = getIndexOfHashedByValue(key) ?: return null
        return entries[index].value
    }

    fun <Q> getHashed(key: Hashed<Q>): V? where Q : Equivalent<K> {
        val index = getIndexOfHashed(key) ?: return null
        return entries[index].value
    }

    fun get(key: K): V? {
        val index = getIndexOf(key) ?: return null
        return entries[index].value
    }

    fun <Q> get(key: Q): V? where Q : Equivalent<K> {
        val index = getIndexOf(key) ?: return null
        return entries[index].value
    }

    fun getIndexOfHashedByValue(key: Hashed<K>): Int? {
        for ((i, e) in entries.withIndex()) {
            if (e.key == key) return i
        }
        return null
    }

    fun <Q> getIndexOfHashed(key: Hashed<Q>): Int? where Q : Equivalent<K> {
        val q = key.key()
        for ((i, e) in entries.withIndex()) {
            if (q.equivalent(e.key.key())) return i
        }
        return null
    }

    fun getIndexOf(key: K): Int? {
        for ((i, e) in entries.withIndex()) {
            if (e.key.key() == key) return i
        }
        return null
    }

    fun <Q> getIndexOf(key: Q): Int? where Q : Equivalent<K> {
        for ((i, e) in entries.withIndex()) {
            if (key.equivalent(e.key.key())) return i
        }
        return null
    }

    fun insertHashedUniqueUnchecked(key: Hashed<K>, value: V) {
        entries.add(Entry(key, value))
    }

    fun insertHashed(key: Hashed<K>, value: V): V? {
        val index = getIndexOfHashedByValue(key)
        return if (index != null) {
            val prev = entries[index].value
            entries[index].value = value
            prev
        } else {
            entries.add(Entry(key, value))
            null
        }
    }

    fun insert(key: K, value: V): V? {
        return insertHashed(Hashed.new(key), value)
    }

    fun insertUniqueUnchecked(key: K, value: V): Pair<K, V> {
        val hashed = Hashed.new(key)
        entries.add(Entry(hashed, value))
        val inserted = entries.last()
        return Pair(inserted.key.key(), inserted.value)
    }

    fun shiftRemoveHashedByValue(key: Hashed<K>): V? {
        val index = getIndexOfHashedByValue(key) ?: return null
        return entries.removeAt(index).value
    }

    fun <Q> shiftRemoveHashed(key: Hashed<Q>): V? where Q : Equivalent<K> {
        val index = getIndexOfHashed(key) ?: return null
        return entries.removeAt(index).value
    }

    fun <Q> shiftRemoveHashedEntry(key: Hashed<Q>): Pair<K, V>? where Q : Equivalent<K> {
        val index = getIndexOfHashed(key) ?: return null
        val entry = entries.removeAt(index)
        return Pair(entry.key.key(), entry.value)
    }

    fun shiftRemoveIndexHashed(i: Int): Pair<Hashed<K>, V>? {
        if (i !in entries.indices) return null
        val entry = entries.removeAt(i)
        return Pair(entry.key, entry.value)
    }

    fun shiftRemoveIndex(i: Int): Pair<K, V>? {
        val (key, value) = shiftRemoveIndexHashed(i) ?: return null
        return Pair(key.intoKey(), value)
    }

    fun shiftRemove(key: K): V? {
        val index = getIndexOf(key) ?: return null
        return entries.removeAt(index).value
    }

    fun <Q> shiftRemove(key: Q): V? where Q : Equivalent<K> {
        val index = getIndexOf(key) ?: return null
        return entries.removeAt(index).value
    }

    fun shiftRemoveEntry(key: K): Pair<K, V>? {
        val index = getIndexOf(key) ?: return null
        val entry = entries.removeAt(index)
        return Pair(entry.key.key(), entry.value)
    }

    fun <Q> shiftRemoveEntry(key: Q): Pair<K, V>? where Q : Equivalent<K> {
        val index = getIndexOf(key) ?: return null
        val entry = entries.removeAt(index)
        return Pair(entry.key.key(), entry.value)
    }

    fun pop(): Pair<K, V>? {
        if (entries.isEmpty()) return null
        val entry = entries.removeAt(entries.lastIndex)
        return Pair(entry.key.intoKey(), entry.value)
    }

    fun stateCheck() {
        val seen = HashSet<K>()
        for (entry in entries) {
            check(seen.add(entry.key.key()))
        }
    }

    /** Equal if the keys and values are equal in the iteration order. */
    fun eqOrdered(other: SmallMap<K, V>): Boolean {
        if (len() != other.len()) return false
        val thisIter = iter().iterator()
        val otherIter = other.iter().iterator()
        while (thisIter.hasNext()) {
            if (!otherIter.hasNext()) return false
            if (thisIter.next() != otherIter.next()) return false
        }
        return true
    }

    /** Hash entries in the iteration order. */
    fun hashOrdered(): Int {
        var result = 1
        for ((key, value) in iterHashed()) {
            result = 31 * result + key.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
        }
        return result
    }

    /** Reverse the iteration order of the map. */
    fun reverse() {
        entries.reverse()
    }

    /** Retains only the elements specified by the predicate. */
    fun retain(f: (K, V) -> Boolean) {
        var i = 0
        while (i < entries.size) {
            val entry = entries[i]
            if (f(entry.key.key(), entry.value)) {
                i += 1
            } else {
                entries.removeAt(i)
            }
        }
    }

    fun extend(iter: Iterable<Pair<K, V>>) {
        for ((key, value) in iter) {
            insert(key, value)
        }
    }
}

private fun <K : Comparable<K>, V> SmallMap<K, V>.isSortedByKey(): Boolean {
    return entries.asSequence().map { it.key.key() }.zipWithNext().all { (left, right) -> left <= right }
}

/** Sort entries by key. */
fun <K : Comparable<K>, V> SmallMap<K, V>.sortKeys() {
    if (isSortedByKey()) return
    entries.sortWith(compareBy { it.key.key() })
}
