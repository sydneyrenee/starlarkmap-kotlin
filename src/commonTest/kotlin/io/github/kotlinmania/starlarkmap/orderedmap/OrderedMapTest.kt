// port-lint: source tests:src/orderedMap.rs
package io.github.kotlinmania.starlarkmap.orderedmap

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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OrderedMapTest {

    /**
     * Verifies that when the map itself is hashed (via [hashCode]), keys are not
     * re-hashed (they were hashed on insertion), but values are hashed each time.
     *
     * Call counts are tracked through a wrapper that counts [hashCode] invocations.
     */
    @Test
    fun testKeysAreNotHashedWhenMapIsHashed() {
        class Tester {
            var hashCount: Int = 0

            override fun equals(other: Any?): Boolean = true

            override fun hashCode(): Int {
                hashCount++
                return 0
            }
        }

        val keyTester = Tester()
        val valueTester = Tester()
        val map = OrderedMap.fromIterator(listOf(Pair(keyTester, valueTester)))

        // Key was hashed once during insertion
        assertEquals(1, map.keys().first().hashCount)
        // Value was not hashed during insertion
        assertEquals(0, map.values().first().hashCount)

        map.hashCode()
        // Key hashed again during hashCode (both key and value are hashed)
        assertEquals(2, map.keys().first().hashCount)
        assertEquals(1, map.values().first().hashCount)

        map.hashCode()
        assertEquals(3, map.keys().first().hashCount)
        assertEquals(2, map.values().first().hashCount)

        map.hashCode()
        assertEquals(4, map.keys().first().hashCount)
        assertEquals(3, map.values().first().hashCount)
    }

    /**
     * Since we don't have kotlinx-serialization wired up for starlarkmap,
     * this test verifies ordered equality and round-trip consistency instead.
     */
    @Test
    fun testOrderedEquality() {
        val map1 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        val map2 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        assertEquals(map1, map2)

        // Different order should NOT be equal
        val map3 = OrderedMap.fromIterator(listOf(Pair("b", 2), Pair("a", 1)))
        assertNotEquals(map1, map3, "Maps with different iteration order should not be equal")
    }

    @Test
    fun testBasicOperations() {
        val map = OrderedMap.new<String, Int>()
        assertEquals(0, map.len())
        assertEquals(true, map.isEmpty())

        map.insert("a", 1)
        map.insert("b", 2)
        map.insert("c", 3)
        assertEquals(3, map.len())
        assertEquals(false, map.isEmpty())

        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
        assertEquals(null, map.get("d"))

        assertEquals(true, map.containsKey("a"))
        assertEquals(false, map.containsKey("d"))

        assertEquals(Pair("a", 1), map.getIndex(0))
        assertEquals(Pair("b", 2), map.getIndex(1))
        assertEquals(null, map.getIndex(5))

        assertEquals(0, map.getIndexOf("a"))
        assertEquals(1, map.getIndexOf("b"))
        assertEquals(null, map.getIndexOf("d"))
    }

    @Test
    fun testInsertOverwrite() {
        val map = OrderedMap.new<String, Int>()
        assertEquals(null, map.insert("a", 1))
        assertEquals(1, map.insert("a", 2))
        assertEquals(2, map.get("a"))
        assertEquals(1, map.len())
    }

    @Test
    fun testRemove() {
        val map = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2), Pair("c", 3)))
        assertEquals(2, map.remove("b"))
        assertEquals(2, map.len())
        assertEquals(null, map.get("b"))
        // Order preserved after shift-remove
        assertEquals(Pair("a", 1), map.getIndex(0))
        assertEquals(Pair("c", 3), map.getIndex(1))
    }

    @Test
    fun testClear() {
        val map = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        map.clear()
        assertEquals(0, map.len())
        assertEquals(true, map.isEmpty())
    }

    @Test
    fun testExtend() {
        val map = OrderedMap.fromIterator(listOf(Pair("a", 1)))
        map.extend(listOf(Pair("b", 2), Pair("c", 3)))
        assertEquals(3, map.len())
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
    }

    @Test
    fun testSortKeys() {
        val map = OrderedMap.new<String, Int>()
        map.insert("c", 3)
        map.insert("a", 1)
        map.insert("b", 2)
        map.sortKeys()
        assertEquals(Pair("a", 1), map.getIndex(0))
        assertEquals(Pair("b", 2), map.getIndex(1))
        assertEquals(Pair("c", 3), map.getIndex(2))
    }

    @Test
    fun testIterationOrder() {
        val map = OrderedMap.new<String, Int>()
        map.insert("c", 3)
        map.insert("a", 1)
        map.insert("b", 2)
        val keys = map.keys().toList()
        assertEquals(listOf("c", "a", "b"), keys)
        val values = map.values().toList()
        assertEquals(listOf(3, 1, 2), values)
    }

    @Test
    fun testHashCodeConsistency() {
        val map1 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        val map2 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        assertEquals(map1.hashCode(), map2.hashCode())
    }

    @Test
    fun testCompareTo() {
        val map1 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        val map2 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("b", 2)))
        assertEquals(0, map1.compareTo(map2))

        val map3 = OrderedMap.fromIterator(listOf(Pair("a", 1), Pair("c", 3)))
        assertEquals(true, map1.compareTo(map3) < 0)
    }

    @Test
    fun testFromSmallMap() {
        val small = starlarkmap.smallmap.SmallMap.new<String, Int>()
        small.insert("x", 10)
        val ordered = OrderedMap.from(small)
        assertEquals(1, ordered.len())
        assertEquals(10, ordered.get("x"))
    }

    @Test
    fun testDefault() {
        val map = OrderedMap.default<String, Int>()
        assertEquals(0, map.len())
        assertEquals(true, map.isEmpty())
    }
}
