// port-lint: source equivalent (external crate)
package io.github.kotlinmania.starlarkmap

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
 * Allows heterogeneous key lookups in hash maps and sets. A type `Q`
 * implementing `Equivalent<K>` can be used to look up entries keyed by
 * `K`, without requiring `Q` and `K` to be the same type.
 */
fun interface Equivalent<in K> {
    /**
     * Compare this value with a key for equivalence.
     *
     * Returns `true` if this value is equivalent to the given key.
     */
    fun equivalent(key: K): Boolean
}
