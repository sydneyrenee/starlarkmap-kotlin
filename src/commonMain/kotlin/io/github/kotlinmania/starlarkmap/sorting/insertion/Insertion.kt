// port-lint: source src/sorting/insertion.rs
package io.github.kotlinmania.starlarkmap.sorting.insertion

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
 * Generic insertion sort (sort arbitrary collections, not just slices).
 */

/**
 * Find the insertion point for an element.
 *
 * The array is sorted up to [nextUnsorted],
 * and the element to insert is at [nextUnsorted].
 */
private inline fun <T> findInsertionPoint(
    array: T,
    nextUnsorted: Int,
    less: (T, Int, Int) -> Boolean,
): Int {
    var i = nextUnsorted
    while (i > 0 && less(array, nextUnsorted, i - 1)) {
        i -= 1
    }
    return i
}

/**
 * [swapShift] operation for generic [insertionSort] implemented for [MutableList].
 *
 * Index [a] is strictly less than index [b], and [b] is less than list size.
 * Perform two operations simultaneously:
 * - Move the element at [b] to [a]
 * - Shift all elements in the range `a..<b` one position to the right
 *
 */
internal fun <T> sliceSwapShift(slice: MutableList<T>, a: Int, b: Int) {
    require(a < b)
    require(b < slice.size)
    val tmp = slice[b]
    // Shift elements a..<b one position to the right
    for (i in b downTo a + 1) {
        slice[i] = slice[i - 1]
    }
    slice[a] = tmp
}

/**
 * Insertion sort for generic collections (not just slices).
 *
 * @param array The collection to sort.
 * @param len The number of elements in [array].
 * @param less Comparison function: returns `true` if element at first index is less than element at second index.
 * @param swapShift Function that moves the element at index `b` to position `a`,
 *   and simultaneously shifts all elements in `a..<b` one position to the right.
 */
internal inline fun <A> insertionSort(
    array: A,
    len: Int,
    less: (A, Int, Int) -> Boolean,
    swapShift: (A, Int, Int) -> Unit,
) {
    for (i in 1 until len) {
        // At this point, the array is sorted up to `i`.
        val insertionPoint = findInsertionPoint(array, i, less)
        if (insertionPoint != i) {
            // Move the element at `i` to the insertion point.
            swapShift(array, insertionPoint, i)
        }
    }
}
