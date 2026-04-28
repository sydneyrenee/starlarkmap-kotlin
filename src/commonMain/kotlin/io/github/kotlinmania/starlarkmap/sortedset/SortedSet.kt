// port-lint: source src/sortedSet.rs
package io.github.kotlinmania.starlarkmap.sortedset

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
import io.github.kotlinmania.starlarkmap.orderedset.OrderedSet
import io.github.kotlinmania.starlarkmap.smallset.SmallSet
import io.github.kotlinmania.starlarkmap.sortedvec.SortedVec

/**
 * An immutable [SmallSet] with values guaranteed to be sorted.
 *
 */
class SortedSet<T> internal constructor(
    private val inner: OrderedSet<T>,
) : Iterable<T> {

    companion object {
        /** Construct an empty [SortedSet]. */
        fun <T> new(): SortedSet<T> where T : Comparable<T> =
            SortedSet(OrderedSet.new())

        /** Construct without checking that the elements are sorted. */
        fun <T> newUnchecked(inner: OrderedSet<T>): SortedSet<T> = SortedSet(inner)

        fun <T> default(): SortedSet<T> where T : Comparable<T> = new()

        /**
         * Create a [SortedSet] from an iterable.
         */
        fun <T> fromIterator(iter: Iterable<T>): SortedSet<T> where T : Comparable<T> {
            val inner = OrderedSet.fromIterator(iter)
            inner.sort()
            return SortedSet(inner)
        }

        fun <T> from(inner: OrderedSet<T>): SortedSet<T> where T : Comparable<T> {
            inner.sort()
            return SortedSet(inner)
        }

        fun <T> from(inner: SmallSet<T>): SortedSet<T> where T : Comparable<T> =
            from(OrderedSet.from(inner))

        fun <T> from(inner: SortedVec<T>): SortedSet<T> where T : Comparable<T> =
            SortedSet(OrderedSet.fromIterator(inner))
    }

    /** Return the number of elements in the set. */
    fun len(): Int = inner.len()

    /** Check if the set is empty. */
    fun isEmpty(): Boolean = inner.isEmpty()

    /** Iterate over the elements. */
    fun iter(): Sequence<T> = inner.iter()

    /** Get the element in the set. */
    fun get(value: T): T? = inner.get(value)

    /** Get the element in the set using [Equivalent]. */
    fun <Q> get(value: Q): T? where Q : Equivalent<T> = inner.get(value)

    /** Check if the set contains the given value. */
    fun contains(value: T): Boolean = inner.contains(value)

    /** Check if the set contains the given value using [Equivalent]. */
    fun <Q> contains(value: Q): Boolean where Q : Equivalent<T> = inner.contains(value)

    /** Get the element at the given index. */
    fun getIndex(index: Int): T? = inner.getIndex(index)

    /** Iterate over the union of two sets. */
    fun union(other: SortedSet<T>): Sequence<T> =
        inner.union(other.inner)

    override fun iterator(): Iterator<T> = inner.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SortedSet<*>) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()
}
