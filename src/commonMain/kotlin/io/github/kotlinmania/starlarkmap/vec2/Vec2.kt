// port-lint: source src/vec2.rs
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

    fun get(index: Int): Pair<A, B>? {
        val aa = a.getOrNull(index) ?: return null
        val bb = b.getOrNull(index) ?: return null
        return Pair(aa, bb)
    }

    fun aaa(): List<A> = a

    fun bbb(): List<B> = b

    fun iter(): Sequence<Pair<A, B>> = a.indices.asSequence().map { i -> Pair(a[i], b[i]) }
}

