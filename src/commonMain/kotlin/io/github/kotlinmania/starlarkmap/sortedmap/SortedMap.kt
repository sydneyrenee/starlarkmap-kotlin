// port-lint: source src/sortedMap.rs
package io.github.kotlinmania.starlarkmap.sortedmap

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
import io.github.kotlinmania.starlarkmap.orderedmap.OrderedMap
import io.github.kotlinmania.starlarkmap.smallmap.SmallMap

/**
 * [OrderedMap] but with keys sorted.
 *
 */
class SortedMap<K, V> internal constructor(
    private val map: OrderedMap<K, V>,
) : Iterable<Pair<K, V>> {

    companion object {
        /** Construct an empty [SortedMap]. */
        fun <K, V> new(): SortedMap<K, V> where K : Comparable<K> =
            SortedMap(OrderedMap.new())

        fun <K, V> default(): SortedMap<K, V> where K : Comparable<K> = new()

        /**
         * Create a [SortedMap] from an iterable of key-value pairs.
         */
        fun <K, V> fromIterator(iter: Iterable<Pair<K, V>>): SortedMap<K, V> where K : Comparable<K> {
            val map = OrderedMap.fromIterator(iter)
            return from(map)
        }

        fun <K, V> from(map: OrderedMap<K, V>): SortedMap<K, V> where K : Comparable<K> {
            map.sortKeys()
            return SortedMap(map)
        }

        fun <K, V> from(map: SmallMap<K, V>): SortedMap<K, V> where K : Comparable<K> =
            from(OrderedMap.from(map))
    }

    /** Iterate over the entries. */
    fun iter(): Sequence<Pair<K, V>> = map.iter()

    /** Iterate over the keys. */
    fun keys(): Sequence<K> = map.keys()

    /** Iterate over the values. */
    fun values(): Sequence<V> = map.values()

    /** Return the number of elements in the map. */
    fun len(): Int = map.len()

    /** Check if the map is empty. */
    fun isEmpty(): Boolean = map.isEmpty()

    /**
     * Get a reference to the value associated with the given key.
     */
    fun get(key: K): V? = map.get(key)

    /** Get a reference to the value using [Equivalent]. */
    fun <Q> get(key: Q): V? where Q : Equivalent<K> = map.get(key)

    /**
     * Check if the map contains the given key.
     */
    fun containsKey(key: K): Boolean = map.containsKey(key)

    /** Check if the map contains the given key using [Equivalent]. */
    fun <Q> containsKey(key: Q): Boolean where Q : Equivalent<K> = map.containsKey(key)

    /** Iterate over the map with hashes. */
    fun iterHashed() = map.iterHashed()

    override fun iterator(): Iterator<Pair<K, V>> = map.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SortedMap<*, *>) return false
        return map == other.map
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}
