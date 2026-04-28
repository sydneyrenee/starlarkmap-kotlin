// port-lint: source src/smallMap/iter.rs
package io.github.kotlinmania.starlarkmap.smallmap.iter

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

import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.vecmap.iter.IterHashed as VecMapIterHashed
import io.github.kotlinmania.starlarkmap.vecmap.iter.Iter as VecMapIter
import io.github.kotlinmania.starlarkmap.vecmap.iter.IntoIterHashed as VecMapIntoIterHashed
import io.github.kotlinmania.starlarkmap.vecmap.iter.IntoIter as VecMapIntoIter
import io.github.kotlinmania.starlarkmap.vecmap.iter.Keys as VecMapKeys
import io.github.kotlinmania.starlarkmap.vecmap.iter.Values as VecMapValues

/**
 * Iterator types for [SmallMap][starlarkmap.smallmap.SmallMap].
 *
 * `defIter()` and `defDoubleEndedIter()` macro expansions.
 * In Kotlin, they delegate to the corresponding vecMap iterators.
 */

internal class IterHashed<K, V>(
    internal val iter: VecMapIterHashed<K, V>,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = iter.len()
}

internal class Iter<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/**
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMut<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/**
 * Iterator over mutable entry references (unchecked key mutation).
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMutUnchecked<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

internal class IntoIterHashed<K, V>(
    internal val iter: VecMapIntoIterHashed<K, V>,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = iter.len()
}

internal class IntoIter<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

internal class Keys<K, V>(
    internal val iter: VecMapKeys<K, V>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next()
}

internal class Values<K, V>(
    internal val iter: VecMapValues<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next()
}

internal class IntoKeys<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next().first
    fun len(): Int = iter.len()
}

internal class IntoValues<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next().second
    fun len(): Int = iter.len()
}

/**
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Values].
 */
internal class ValuesMut<K, V>(
    internal val iter: VecMapValues<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next()
}
