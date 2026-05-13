// port-lint: source vec2/iter.rs
package io.github.kotlinmania.starlarkmap.vec2.iter

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
 * This iterator tracks an index range into both parallel lists, supporting
 * bidirectional iteration.
 *
 * In addition to standard [Iterator] operations, provides [len], [sizeHint],
 * and [nextBack] for reverse iteration.
 */
class Iter<A, B>(
    private val firstElements: List<A>,
    private val secondElements: List<B>,
) : Iterator<Pair<A, B>> {
    private var front: Int = 0
    private var back: Int = firstElements.size

    override fun hasNext(): Boolean = front < back

    override fun next(): Pair<A, B> {
        if (front >= back) throw NoSuchElementException()
        val a = firstElements[front]
        val b = secondElements[front]
        front++
        return Pair(a, b)
    }

    /** Returns the next element from the back of the iterator. */
    fun nextBack(): Pair<A, B>? {
        if (front >= back) return null
        back--
        return Pair(firstElements[back], secondElements[back])
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
 * Yields pairs of elements from both parallel lists. The iterator consumes
 * the entire range of elements, supporting bidirectional iteration.
 *
 * In addition to standard [Iterator] operations, provides [len], [sizeHint],
 * and [nextBack] for reverse iteration.
 */
class IntoIter<A, B>(
    private val firstElements: List<A>,
    private val secondElements: List<B>,
) : Iterator<Pair<A, B>> {
    private var front: Int = 0
    private var back: Int = secondElements.size

    override fun hasNext(): Boolean = front < back

    override fun next(): Pair<A, B> {
        if (front >= back) throw NoSuchElementException()
        val a = firstElements[front]
        val b = secondElements[front]
        front++
        return Pair(a, b)
    }

    /** Returns the next element from the back of the iterator. */
    fun nextBack(): Pair<A, B>? {
        if (front >= back) return null
        back--
        val a = firstElements[back]
        val b = secondElements[back]
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
