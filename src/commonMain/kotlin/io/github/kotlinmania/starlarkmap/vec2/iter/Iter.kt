// port-lint: source src/vec2/iter.rs
package io.github.kotlinmania.starlarkmap.vec2.iter

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

/**
 * Iterator over [Vec2] elements.
 *
 * both `A` and `B` arrays. In Kotlin, [Vec2] uses two parallel lists, so this
 * iterator simply tracks an index into both lists.
 *
 * Implements [Iterator], [ExactSizeIterator], and [DoubleEndedIterator] semantics.
 *
 */
internal class Iter<A, B>(
    private val aaa: List<A>,
    private val bbb: List<B>,
) : Iterator<Pair<A, B>> {
    private var front: Int = 0
    private var back: Int = aaa.size

    override fun hasNext(): Boolean = front < back

    override fun next(): Pair<A, B> {
        if (front >= back) throw NoSuchElementException()
        val a = aaa[front]
        val b = bbb[front]
        front++
        return Pair(a, b)
    }

    /** Returns the next element from the back of the iterator. */
    fun nextBack(): Pair<A, B>? {
        if (front >= back) return null
        back--
        return Pair(aaa[back], bbb[back])
    }

    /** Returns the number of remaining elements. */
    fun len(): Int = back - front

    /** Returns the size hint as a pair of (lower bound, upper bound). */
    fun sizeHint(): Pair<Int, Int> {
        val rem = len()
        return Pair(rem, rem)
    }
}

/**
 * Iterator which consumes the [Vec2].
 *
 * owned `(A, B)` pairs, deallocating on drop. In Kotlin, there is no
 * ownership transfer — this iterator simply iterates over the lists.
 *
 * Implements [Iterator], [ExactSizeIterator], and [DoubleEndedIterator] semantics.
 *
 */
internal class IntoIter<A, B>(
    private val aaa: List<A>,
    private val bbb: List<B>,
) : Iterator<Pair<A, B>> {
    private var front: Int = 0
    private var back: Int = bbb.size

    override fun hasNext(): Boolean = front < back

    override fun next(): Pair<A, B> {
        if (front >= back) throw NoSuchElementException()
        val a = aaa[front]
        val b = bbb[front]
        front++
        return Pair(a, b)
    }

    /** Returns the next element from the back of the iterator. */
    fun nextBack(): Pair<A, B>? {
        if (front >= back) return null
        back--
        val newLen = len()
        val a = aaa[newLen]
        val b = bbb[back]
        return Pair(a, b)
    }

    /** Returns the number of remaining elements. */
    fun len(): Int = back - front

    /** Returns the size hint as a pair of (lower bound, upper bound). */
    fun sizeHint(): Pair<Int, Int> {
        val rem = len()
        return Pair(rem, rem)
    }
}
