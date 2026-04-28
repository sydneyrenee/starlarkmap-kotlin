// port-lint: source src/vecMap/hint.rs
package io.github.kotlinmania.starlarkmap.vecmap.hint

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
 * Branch prediction hint.
 *
 * a branch prediction hint to the compiler. On stable Rust and in Kotlin,
 * this is a no-op identity function.
 *
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun likely(x: Boolean): Boolean = x
