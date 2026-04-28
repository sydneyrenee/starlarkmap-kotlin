// port-lint: source src/smallSet/iter.rs
package io.github.kotlinmania.starlarkmap.smallset.iter

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
import io.github.kotlinmania.starlarkmap.smallmap.iter.Iter as SmallMapIter
import io.github.kotlinmania.starlarkmap.smallmap.iter.IterMutUnchecked as SmallMapIterMutUnchecked
import io.github.kotlinmania.starlarkmap.smallmap.iter.IterHashed as SmallMapIterHashed
import io.github.kotlinmania.starlarkmap.smallmap.iter.IntoIter as SmallMapIntoIter
import io.github.kotlinmania.starlarkmap.smallmap.iter.IntoIterHashed as SmallMapIntoIterHashed

/**
 * Iterator types for [SmallSet][starlarkmap.smallset.SmallSet].
 *
 * discard the `()` value component. In Kotlin, they wrap the
 * corresponding smallMap iterators and extract just the key.
 */

internal class Iter<T>(
    internal val iter: SmallMapIter<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

/**
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMutUnchecked<T>(
    internal val iter: SmallMapIterMutUnchecked<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

internal class IterHashed<T>(
    internal val iter: SmallMapIterHashed<T, Unit>,
) : Iterator<Hashed<T>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Hashed<T> = iter.next().first
    fun len(): Int = iter.len()
}

internal class IntoIter<T>(
    internal val iter: SmallMapIntoIter<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

internal class IntoIterHashed<T>(
    internal val iter: SmallMapIntoIterHashed<T, Unit>,
) : Iterator<Hashed<T>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Hashed<T> = iter.next().first
    fun len(): Int = iter.len()
}
