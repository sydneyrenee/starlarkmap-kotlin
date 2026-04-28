// port-lint: source src/orderedSet.rs
package io.github.kotlinmania.starlarkmap.orderedset

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
import io.github.kotlinmania.starlarkmap.smallset.SmallSet

/**
 * [SmallSet] wrapper, but equality and hash of self depends on iteration order.
 *
 * Two [OrderedSet]s are equal only when they contain the same elements
 * in the same iteration order.
 */
class OrderedSet<T> internal constructor(
    internal val inner: SmallSet<T>,
) : Iterable<T>, Comparable<OrderedSet<T>> {

    companion object {
        /** Create a new empty set. */
        fun <T> new(): OrderedSet<T> = OrderedSet(SmallSet())

        /** Create a new empty set with the specified capacity. */
        fun <T> withCapacity(capacity: Int): OrderedSet<T> =
            OrderedSet(SmallSet.withCapacity(capacity))

        fun <T> default(): OrderedSet<T> = new()

        /**
         * Create an [OrderedSet] from an iterable.
         */
        fun <T> fromIterator(iter: Iterable<T>): OrderedSet<T> =
            OrderedSet(SmallSet.fromIterator(iter))

        /**
         * Create an [OrderedSet] from a [SmallSet].
         */
        fun <T> from(set: SmallSet<T>): OrderedSet<T> = OrderedSet(set)
    }

    /** Get the number of elements in the set. */
    fun len(): Int = inner.len()

    /** Check if the set is empty. */
    fun isEmpty(): Boolean = inner.isEmpty()

    /**
     * Get an element from the set.
     */
    fun get(value: T): T? = inner.get(value)

    /** Get an element from the set using [Equivalent]. */
    fun <Q> get(value: Q): T? where Q : Equivalent<T> = inner.get(value)

    /**
     * Check if the set contains an element.
     */
    fun contains(value: T): Boolean = inner.contains(value)

    /** Check if the set contains an element using [Equivalent]. */
    fun <Q> contains(value: Q): Boolean where Q : Equivalent<T> = inner.contains(value)

    /** Get an element by index. */
    fun getIndex(index: Int): T? = inner.getIndex(index)

    /**
     * Get the index of an element in the set.
     */
    fun getIndexOf(value: T): Int? = inner.getIndexOf(value)

    /** Get the index of an element using [Equivalent]. */
    fun <Q> getIndexOf(value: Q): Int? where Q : Equivalent<T> = inner.getIndexOf(value)

    /**
     * Remove an element from the set and return it.
     */
    fun take(value: T): T? = inner.take(value)

    /** Remove an element using [Equivalent] and return it. */
    fun <Q> take(value: Q): T? where Q : Equivalent<T> = inner.take(value)

    /** Iterate over the elements. */
    fun iter(): Sequence<T> = inner.iter()

    /** Get the first element. */
    fun first(): T? = inner.first()

    /** Get the last element. */
    fun last(): T? = inner.last()

    /**
     * Insert an element into the set.
     * Returns `true` iff the element was inserted (was not already present).
     */
    fun insert(value: T): Boolean = inner.insert(value)

    /**
     * Insert an element into the set assuming it is not already present.
     */
    fun insertUniqueUnchecked(value: T) = inner.insertUniqueUnchecked(value)

    /**
     * Insert an element if it is not already present in the set.
     * Returns `null` if inserted successfully, or an [OccupiedError]
     * containing the value that was not inserted and the existing element.
     */
    fun tryInsert(value: T): OccupiedError<T>? {
        val hashed = Hashed.new(value)
        val existing = inner.getHashed(object : Equivalent<T> {
            override fun equivalent(key: T): Boolean = hashed.key() == key
        }.let { equiv ->
            Hashed.newUnchecked(hashed.hash(), equiv)
        })
        if (existing != null) {
            return OccupiedError(value, existing)
        }
        inner.insertHashedUniqueUnchecked(hashed)
        return null
    }

    /** Clear the set. */
    fun clear() = inner.clear()

    /**
     * Sort the set.
     */
    fun sort() = inner.sort()

    /**
     * Iterate over the union of two sets.
     */
    fun union(other: OrderedSet<T>): Sequence<T> =
        inner.union(other.inner)

    /**
     * Reverse the iteration order of the set.
     */
    fun reverse() = inner.reverse()

    /**
     * Extend the set with elements from an iterable.
     */
    fun extend(iter: Iterable<T>) = inner.extend(iter)

    override fun iterator(): Iterator<T> = inner.iterator()

    /**
     * Ordered equality: two [OrderedSet]s are equal iff they contain the same elements
     * in the same iteration order.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderedSet<*>) return false
        return inner.eqOrdered(@Suppress("UNCHECKED_CAST") (other.inner as SmallSet<T>))
    }

    /**
     * Hash based on ordered iteration of elements.
     */
    override fun hashCode(): Int {
        var result = 1
        for (t in iter()) {
            result = 31 * result + (t?.hashCode() ?: 0)
        }
        return result
    }

    /**
     * Compare two [OrderedSet]s lexicographically by their iteration order.
     */
    override fun compareTo(other: OrderedSet<T>): Int {
        val thisIter = iter().iterator()
        val otherIter = other.iter().iterator()
        while (thisIter.hasNext() && otherIter.hasNext()) {
            val t = thisIter.next()
            val o = otherIter.next()
            @Suppress("UNCHECKED_CAST")
            val cmp = (t as Comparable<T>).compareTo(o)
            if (cmp != 0) return cmp
        }
        return when {
            thisIter.hasNext() -> 1
            otherIter.hasNext() -> -1
            else -> 0
        }
    }

    override fun toString(): String {
        return iter().joinToString(", ", "{", "}")
    }
}

/**
 * Error returned by [OrderedSet.tryInsert] when the element is already present.
 */
class OccupiedError<T>(
    /** The value that was not inserted. */
    val value: T,
    /** The value that was already in the set. */
    val occupied: T,
)
