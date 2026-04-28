// port-lint: source src/vecMap/simd.rs
@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.starlarkmap.vecmap.simd

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
 * Find a hash value in an array of hashes.
 *
 * Kotlin does not have portable SIMD, so we import the scalar fallback.
 *
 */
internal fun findHashInArrayWithoutSimd(array: UIntArray, hash: UInt): Int? {
    var i = 0
    while (i < array.size) {
        if (array[i] == hash) {
            return i
        }
        i += 1
    }
    return null
}

/**
 * Find a hash value in an array of hashes.
 *
 * and falls back to scalar otherwise. In Kotlin, we always import the scalar path.
 */
internal fun findHashInArray(array: UIntArray, hash: UInt): Int? {
    return findHashInArrayWithoutSimd(array, hash)
}
