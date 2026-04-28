// port-lint: source src/vecMap.rs
package io.github.kotlinmania.starlarkmap.vecmap

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

internal typealias IntoIter<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.IntoIter<K, V>
internal typealias IntoIterHashed<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.IntoIterHashed<K, V>
internal typealias Iter<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.Iter<K, V>
internal typealias IterHashed<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.IterHashed<K, V>
internal typealias Keys<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.Keys<K, V>
internal typealias Values<K, V> = io.github.kotlinmania.starlarkmap.vecmap.iter.Values<K, V>

/**
 * Vec-backed map implementation used by [io.github.kotlinmania.starlarkmap.smallmap.SmallMap].
 *
 * deterministic iteration order and API behaviour with a straightforward linear search.
 */
internal class VecMap<K, V> private constructor(
    private val keys: ArrayList<K>,
    private val values: ArrayList<V>,
    private val hashes: ArrayList<StarlarkHashValue>,
) {
    companion object {
        fun <K, V> new(): VecMap<K, V> = VecMap(ArrayList(), ArrayList(), ArrayList())

        fun <K, V> withCapacity(n: Int): VecMap<K, V> =
            VecMap(ArrayList(n), ArrayList(n), ArrayList(n))
    }

    fun reserve(additional: Int) {
        // No-op in this Kotlin implementation.
    }

    fun capacity(): Int = keys.size

    fun len(): Int = keys.size

    fun isEmpty(): Boolean = keys.isEmpty()

    fun clear() {
        keys.clear()
        values.clear()
        hashes.clear()
    }

    fun getIndexOfHashedRaw(hash: StarlarkHashValue, eq: (K) -> Boolean): Int? {
        for (i in keys.indices) {
            if (hashes[i] == hash && eq(keys[i])) return i
        }
        return null
    }

    fun <Q> getIndexOfHashed(key: Hashed<Q>): Int? where Q : Equivalent<K> {
        val q = key.key()
        return getIndexOfHashedRaw(key.hash()) { k -> q.equivalent(k) }
    }

    fun getIndex(index: Int): Pair<K, V>? {
        val k = keys.getOrNull(index) ?: return null
        val v = values.getOrNull(index) ?: return null
        return Pair(k, v)
    }

    fun insertHashedUniqueUnchecked(key: Hashed<K>, value: V) {
        hashes.add(key.hash())
        keys.add(key.intoKey())
        values.add(value)
    }

    fun <Q> removeHashedEntry(key: Hashed<Q>): Pair<K, V>? where Q : Equivalent<K> {
        val index = getIndexOfHashed(key) ?: return null
        val k = keys.removeAt(index)
        val v = values.removeAt(index)
        hashes.removeAt(index)
        return Pair(k, v)
    }

    fun remove(index: Int): Pair<Hashed<K>, V> {
        val k = keys.removeAt(index)
        val v = values.removeAt(index)
        val h = hashes.removeAt(index)
        return Pair(Hashed.newUnchecked(h, k), v)
    }

    fun pop(): Pair<Hashed<K>, V>? {
        if (keys.isEmpty()) return null
        val i = keys.lastIndex
        val k = keys.removeAt(i)
        val v = values.removeAt(i)
        val h = hashes.removeAt(i)
        return Pair(Hashed.newUnchecked(h, k), v)
    }

    fun values(): Sequence<V> = values.asSequence()

    fun keys(): Sequence<K> = keys.asSequence()

    fun iter(): Sequence<Pair<K, V>> =
        keys.indices.asSequence().map { i -> Pair(keys[i], values[i]) }

    fun iterHashed(): Sequence<Pair<Hashed<K>, V>> =
        keys.indices.asSequence().map { i -> Pair(Hashed.newUnchecked(hashes[i], keys[i]), values[i]) }
}

