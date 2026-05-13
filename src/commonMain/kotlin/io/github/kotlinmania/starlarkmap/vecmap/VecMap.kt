// port-lint: source vec_map.rs
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
import io.github.kotlinmania.starlarkmap.vec2.Vec2
import io.github.kotlinmania.starlarkmap.vecmap.simd.findHashInArray

internal class VecMap<K, V> private constructor(
    internal val buckets: Vec2<Pair<K, V>, StarlarkHashValue>,
) {
    companion object {
        fun <K, V> new(): VecMap<K, V> = VecMap(Vec2.new())

        fun <K, V> default(): VecMap<K, V> = new()

        fun <K, V> withCapacity(n: Int): VecMap<K, V> = VecMap(Vec2.withCapacity(n))
    }

    fun reserve(additional: Int) {
        buckets.reserve(additional)
    }

    fun capacity(): Int = buckets.capacity()

    fun len(): Int = buckets.len()

    fun isEmpty(): Boolean = buckets.isEmpty()

    fun clear() {
        buckets.clear()
    }

    fun getIndexOfHashedRaw(hash: StarlarkHashValue, eq: (K) -> Boolean): Int? {
        val hashes = buckets.secondElements()
        val pairs = buckets.firstElements()
        var i = 0
        while (i < hashes.size) {
            i = findHashInArray(hashes, hash, i) ?: return null
            if (i >= pairs.size) return null
            if (eq(pairs[i].first)) return i
            i += 1
        }
        return null
    }

    fun <Q> getIndexOfHashed(key: Hashed<Q>): Int? where Q : Equivalent<K> {
        val q = key.key()
        return getIndexOfHashedRaw(key.hash()) { k -> q.equivalent(k) }
    }

    fun getIndex(index: Int): Pair<K, V>? {
        val (pair, _hash) = buckets.get(index) ?: return null
        return pair
    }

    fun getUnchecked(index: Int): Pair<Hashed<K>, V> {
        val (pair, hash) = buckets.getUnchecked(index)
        return Pair(Hashed.newUnchecked(hash, pair.first), pair.second)
    }

    fun getUncheckedMut(index: Int): Pair<Hashed<K>, V> = getUnchecked(index)

    fun insertHashedUniqueUnchecked(key: Hashed<K>, value: V) {
        buckets.push(Pair(key.intoKey(), value), key.hash())
    }

    /** Replace the value at `index`, keeping the existing key/hash. */
    fun setValue(index: Int, value: V) {
        val pairs = buckets.firstElementsMut()
        val key = pairs[index].first
        pairs[index] = Pair(key, value)
    }

    /** Read the value at `index` without bounds checking — caller must guarantee `index < len()`. */
    fun valueAt(index: Int): V = buckets.firstElements()[index].second

    /** Read the key at `index` without bounds checking. */
    fun keyAt(index: Int): K = buckets.firstElements()[index].first

    /** Read the [Hashed] key at `index` without bounds checking. */
    fun hashedKeyAt(index: Int): Hashed<K> {
        val (k, _) = buckets.firstElements()[index]
        val h = buckets.secondElements()[index]
        return Hashed.newUnchecked(h, k)
    }

    fun <Q> removeHashedEntry(key: Hashed<Q>): Pair<K, V>? where Q : Equivalent<K> {
        val index = getIndexOfHashed(key) ?: return null
        val (pair, _) = buckets.remove(index)
        return pair
    }

    fun remove(index: Int): Pair<Hashed<K>, V> {
        val (pair, hash) = buckets.remove(index)
        return Pair(Hashed.newUnchecked(hash, pair.first), pair.second)
    }

    fun pop(): Pair<Hashed<K>, V>? {
        val (pair, hash) = buckets.pop() ?: return null
        return Pair(Hashed.newUnchecked(hash, pair.first), pair.second)
    }

    fun values(): Sequence<V> = buckets.firstElements().asSequence().map { it.second }

    /**
     * Porting compatibility with Rust's `values_mut`.
     *
     * Kotlin does not have Rust-style mutable reference iterators, so this method
     * intentionally returns the same sequence as [values].
     */
    fun valuesMut(): Sequence<V> = values()

    fun keys(): Sequence<K> = buckets.firstElements().asSequence().map { it.first }

    fun intoIter(): Iterator<Pair<K, V>> = iter().iterator()

    fun iter(): Sequence<Pair<K, V>> = buckets.firstElements().asSequence()

    fun iterHashed(): Sequence<Pair<Hashed<K>, V>> = buckets.iter().map { (pair, hash) ->
        Pair(Hashed.newUnchecked(hash, pair.first), pair.second)
    }

    fun intoIterHashed(): Iterator<Pair<Hashed<K>, V>> = buckets.intoIter().asSequence().map { (pair, hash) ->
        Pair(Hashed.newUnchecked(hash, pair.first), pair.second)
    }.iterator()

    fun iterMut(): Sequence<Pair<K, V>> = buckets.firstElementsMut().asSequence()

    /** Compatibility alias: currently equivalent to [iterMut]. */
    fun iterMutUnchecked(): Sequence<Pair<K, V>> = iterMut()

    /** Equal if entries are equal in the iterator order. */
    fun eqOrdered(other: VecMap<K, V>): Boolean {
        // Compare hashes before keys/values: hash mismatch short-circuits faster than
        // walking equal pairs.
        return buckets.secondElements() == other.buckets.secondElements() && buckets.firstElements() == other.buckets.firstElements()
    }

    /** Hash entries in the iterator order. */
    fun hashOrdered(): Int {
        var result = 1
        for ((hashedKey, value) in iterHashed()) {
            result = 31 * result + hashedKey.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
        }
        return result
    }

    fun reverse() {
        buckets.firstElementsMut().reverse()
        buckets.secondElementsMut().reverse()
    }

    fun retain(f: (K, V) -> Boolean) {
        buckets.retain { pair, _ -> f(pair.first, pair.second) }
    }
}

internal fun <K : Comparable<K>, V> VecMap<K, V>.sortKeys() {
    buckets.sortBy { (a, _), (b, _) -> a.first.compareTo(b.first) }
}

internal fun <K : Comparable<K>, V> VecMap<K, V>.isSortedByKey(): Boolean {
    // Check if all consecutive pairs are in sorted order.
    return buckets.firstElements().asSequence().windowed(2).all { (a, b) -> a.first <= b.first }
}
