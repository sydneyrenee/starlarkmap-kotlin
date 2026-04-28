// port-lint: source src/smallSet.rs
package io.github.kotlinmania.starlarkmap.smallset

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
 * A set with deterministic iteration order.
 */
class SmallSet<T> private constructor(
    internal val entries: ArrayList<Hashed<T>>,
) {
    constructor() : this(ArrayList())

    companion object {
        fun <T> new(): SmallSet<T> = SmallSet(ArrayList())

        fun <T> withCapacity(n: Int): SmallSet<T> = SmallSet(ArrayList(n))

        fun <T> fromIterator(iter: Iterable<T>): SmallSet<T> {
            val set = SmallSet<T>()
            for (t in iter) {
                set.insert(t)
            }
            return set
        }
    }

    fun isEmpty(): Boolean = entries.isEmpty()

    fun len(): Int = entries.size

    fun clear() {
        entries.clear()
    }

    fun iter(): Sequence<T> = entries.asSequence().map { it.key() }

    fun iterHashed(): Sequence<Hashed<T>> = entries.asSequence()

    fun intoIterHashed(): Sequence<Hashed<T>> = iterHashed()

    /** Returns a reference to the first item. */
    fun first(): T? = entries.firstOrNull()?.key()

    /** Returns a reference to the last item. */
    fun last(): T? = entries.lastOrNull()?.key()

    /** Get an element by index. */
    fun getIndex(index: Int): T? = entries.getOrNull(index)?.key()

    /** Get the index of an element in the set. */
    fun getIndexOf(value: T): Int? {
        for ((i, e) in entries.withIndex()) {
            if (e.key() == value) return i
        }
        return null
    }

    /** Get the index of an element using [Equivalent]. */
    fun <Q> getIndexOf(value: Q): Int? where Q : Equivalent<T> {
        for ((i, e) in entries.withIndex()) {
            if (value.equivalent(e.key())) return i
        }
        return null
    }

    /** Return a reference to the value stored in the set, if it is present. */
    fun get(value: T): T? {
        for (e in entries) {
            if (e.key() == value) return e.key()
        }
        return null
    }

    /** Return a reference to the value stored in the set using [Equivalent]. */
    fun <Q> get(value: Q): T? where Q : Equivalent<T> {
        for (e in entries) {
            if (value.equivalent(e.key())) return e.key()
        }
        return null
    }

    /** Query the set by a prehashed value. */
    fun <Q> getHashed(value: Hashed<Q>): T? where Q : Equivalent<T> {
        val q = value.key()
        for (e in entries) {
            if (q.equivalent(e.key())) return e.key()
        }
        return null
    }

    /** Check if the set contains an element. */
    fun contains(value: T): Boolean = get(value) != null

    /** Check if the set contains an element using [Equivalent]. */
    fun <Q> contains(value: Q): Boolean where Q : Equivalent<T> = get(value) != null

    fun addAll(values: Iterable<Hashed<T>>) {
        for (v in values) {
            insertHashed(v)
        }
    }

    fun containsHashed(key: Hashed<T>): Boolean {
        return entries.any { it == key }
    }

    fun <Q> containsHashed(key: Hashed<Q>): Boolean where Q : Equivalent<T> {
        val q = key.key()
        return entries.any { q.equivalent(it.key()) }
    }

    /** Insert the element into the set. Return `true` iff the element was inserted. */
    fun insert(value: T): Boolean {
        return insertHashed(Hashed.new(value))
    }

    /** Insert the element into the set without checking for a duplicate entry. */
    fun insertUniqueUnchecked(value: T) {
        insertHashedUniqueUnchecked(Hashed.new(value))
    }

    fun insertHashed(value: Hashed<T>): Boolean {
        if (containsHashed(value)) return false
        entries.add(value)
        return true
    }

    fun insertHashedUniqueUnchecked(value: Hashed<T>) {
        entries.add(value)
    }

    /** Remove and return the last element, or null if empty. */
    fun pop(): T? {
        if (entries.isEmpty()) return null
        return entries.removeAt(entries.lastIndex).key()
    }

    /** Remove the element from the set if it is present, and return the removed element. */
    fun take(value: T): T? {
        val index = getIndexOf(value) ?: return null
        return entries.removeAt(index).key()
    }

    /** Remove the element using [Equivalent], and return the removed element. */
    fun <Q> take(value: Q): T? where Q : Equivalent<T> {
        val index = getIndexOf(value) ?: return null
        return entries.removeAt(index).key()
    }

    fun shiftRemoveHashed(value: Hashed<T>): Boolean {
        val index = entries.indexOfFirst { it == value }
        if (index < 0) return false
        entries.removeAt(index)
        return true
    }

    fun <Q> shiftRemoveHashed(value: Hashed<Q>): Boolean where Q : Equivalent<T> {
        val q = value.key()
        val index = entries.indexOfFirst { q.equivalent(it.key()) }
        if (index < 0) return false
        entries.removeAt(index)
        return true
    }

    /** Sort entries. */
    @Suppress("UNCHECKED_CAST")
    fun sort() {
        entries.sortWith(compareBy { it.key() as Comparable<Any> })
    }

    /** Reverse the iteration order of the set. */
    fun reverse() {
        entries.reverse()
    }

    /** Iterator over union of two sets. */
    fun union(other: SmallSet<T>): Sequence<T> {
        return iter() + other.iter().filter { !contains(it) }
    }

    /** Equal if entries are equal in iteration order. */
    fun eqOrdered(other: SmallSet<T>): Boolean {
        if (len() != other.len()) return false
        val thisIter = iter().iterator()
        val otherIter = other.iter().iterator()
        while (thisIter.hasNext()) {
            if (!otherIter.hasNext()) return false
            if (thisIter.next() != otherIter.next()) return false
        }
        return true
    }

    /** Extend with elements from an iterable. */
    fun extend(iter: Iterable<T>) {
        for (t in iter) {
            insert(t)
        }
    }

    operator fun iterator(): Iterator<T> = iter().iterator()
}

