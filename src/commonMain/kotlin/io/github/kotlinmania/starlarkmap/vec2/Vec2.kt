// port-lint: source vec2.rs
package io.github.kotlinmania.starlarkmap.vec2

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

import io.github.kotlinmania.starlarkmap.sorting.insertion.insertionSort
import io.github.kotlinmania.starlarkmap.sorting.insertion.sliceSwapShift

/**
 * A `List<Pair<A, B>>`-like object which stores `A` and `B` separately.
 *
 * Kotlin commonMain does not provide low-level allocation APIs, so this port preserves the
 * same observable behaviour using two parallel [ArrayList]s.
 */
class Vec2<A, B> private constructor(
    private val a: ArrayList<A>,
    private val b: ArrayList<B>,
) {
    companion object {
        fun <A, B> new(): Vec2<A, B> = Vec2(ArrayList(), ArrayList())

        fun <A, B> withCapacity(n: Int): Vec2<A, B> = Vec2(ArrayList(n), ArrayList(n))
    }

    fun len(): Int = a.size

    fun isEmpty(): Boolean = a.isEmpty()

    fun capacity(): Int = a.size

    fun reserve(additional: Int) {
        // No-op in this Kotlin implementation.
    }

    fun clear() {
        a.clear()
        b.clear()
    }

    fun push(aValue: A, bValue: B) {
        a.add(aValue)
        b.add(bValue)
    }

    fun pop(): Pair<A, B>? {
        if (a.isEmpty()) return null
        val i = a.lastIndex
        val aa = a.removeAt(i)
        val bb = b.removeAt(i)
        return Pair(aa, bb)
    }

    fun remove(index: Int): Pair<A, B> {
        val aa = a.removeAt(index)
        val bb = b.removeAt(index)
        return Pair(aa, bb)
    }

    fun get(index: Int): Pair<A, B>? =
        if (index in 0 until len()) getUnchecked(index) else null

    fun firstElements(): List<A> = a

    fun firstElementsMut(): MutableList<A> = a

    fun secondElements(): List<B> = b

    fun secondElementsMut(): MutableList<B> = b

    /** Read entry at index without bounds checking — caller must ensure `index < len()`. */
    fun getUnchecked(index: Int): Pair<A, B> = Pair(a[index], b[index])

    /** Get the first element reference. */
    fun first(): Pair<A, B>? = get(0)

    /** Get the last element reference. */
    fun last(): Pair<A, B>? = get(len() - 1)

    /** If capacity exceeds length, shrink capacity to length. */
    fun shrinkToFit() {
        a.trimToSize()
        b.trimToSize()
    }

    /**
     * Truncate the vector to the given length.
     *
     * If the vector is already shorter than the given length, do nothing.
     */
    fun truncate(len: Int) {
        if (len >= a.size) return
        while (a.size > len) {
            a.removeAt(a.size - 1)
            b.removeAt(b.size - 1)
        }
    }

    /** Retains only the elements specified by the predicate. */
    fun retain(f: (A, B) -> Boolean) {
        var written = 0
        var next = 0
        while (next < a.size) {
            val aa = a[next]
            val bb = b[next]
            if (f(aa, bb)) {
                if (written != next) {
                    a[written] = aa
                    b[written] = bb
                }
                written += 1
            }
            next += 1
        }
        while (a.size > written) {
            a.removeAt(a.size - 1)
            b.removeAt(b.size - 1)
        }
    }

    fun iter(): Sequence<Pair<A, B>> = a.asSequence().zip(b.asSequence())

    /** Consuming iterator over (A, B) pairs. Mirrors Rust's `IntoIterator for Vec2`. */
    fun intoIter(): Iterator<Pair<A, B>> = iter().iterator()

    operator fun iterator(): Iterator<Pair<A, B>> = intoIter()

    internal fun sortInsertionBy(compare: (Pair<A, B>, Pair<A, B>) -> Int) {
        insertionSort(
            this,
            len(),
            { vec, i, j -> compare(vec.get(i)!!, vec.get(j)!!) < 0 },
            { vec, ai, bi ->
                sliceSwapShift(vec.a, ai, bi)
                sliceSwapShift(vec.b, ai, bi)
            },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vec2<*, *>) return false
        return a == other.a && b == other.b
    }

    override fun hashCode(): Int {
        var result = a.size
        for (i in a.indices) {
            result = 31 * result + (a[i]?.hashCode() ?: 0)
            result = 31 * result + (b[i]?.hashCode() ?: 0)
        }
        return result
    }

    override fun toString(): String =
        a.indices.joinToString(prefix = "[", postfix = "]", separator = ", ") { i -> "(${a[i]}, ${b[i]})" }

    /** Sort the elements using given comparator. */
    fun sortBy(compare: (Pair<A, B>, Pair<A, B>) -> Int) {
        // Constant from rust stdlib.
        val MAX_INSERTION = 20
        if (len() <= MAX_INSERTION) {
            sortInsertionBy(compare)
            return
        }

        val entries: MutableList<Pair<A, B>> = iter().toMutableList()
        entries.sortWith(Comparator { x, y -> compare(x, y) })
        clear()
        for ((aa, bb) in entries) {
            push(aa, bb)
        }
    }
}

