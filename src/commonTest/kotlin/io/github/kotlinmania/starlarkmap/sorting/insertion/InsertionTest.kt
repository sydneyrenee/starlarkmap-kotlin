package io.github.kotlinmania.starlarkmap.sorting.insertion

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
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

import kotlin.test.Test
import kotlin.test.assertEquals

class InsertionTest {
    @Test
    fun testFindInsertionPoint() {
        fun findInsertionPointInts(slice: MutableList<Int>): Int {
            return findInsertionPoint(slice, slice.size - 1) { values, i, j ->
                values[i] < values[j]
            }
        }

        assertEquals(0, findInsertionPointInts(mutableListOf(2, 4, 6, 0)))
        assertEquals(0, findInsertionPointInts(mutableListOf(2, 4, 6, 1)))
        assertEquals(1, findInsertionPointInts(mutableListOf(2, 4, 6, 2)))
        assertEquals(1, findInsertionPointInts(mutableListOf(2, 4, 6, 3)))
        assertEquals(2, findInsertionPointInts(mutableListOf(2, 4, 6, 4)))
        assertEquals(2, findInsertionPointInts(mutableListOf(2, 4, 6, 5)))
        assertEquals(3, findInsertionPointInts(mutableListOf(2, 4, 6, 6)))
    }

    @Test
    fun testInsertionSort() {
        fun insertionSortInts(slice: MutableList<Int>) {
            insertionSort(
                slice,
                slice.size,
                { values, i, j ->
                    values[i] / 100 < values[j] / 100
                },
                ::sliceSwapShift,
            )
        }

        val sorted = mutableListOf(200, 400, 600)
        insertionSortInts(sorted)
        assertEquals(listOf(200, 400, 600), sorted)

        val unsorted = mutableListOf(600, 200, 400)
        insertionSortInts(unsorted)
        assertEquals(listOf(200, 400, 600), unsorted)

        val stable = mutableListOf(202, 402, 602, 201, 401, 601)
        insertionSortInts(stable)
        assertEquals(listOf(202, 201, 402, 401, 602, 601), stable)
    }
}
