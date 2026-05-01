// port-lint: source src/vec_map/iter.rs
package io.github.kotlinmania.starlarkmap.vecmap.iter

import io.github.kotlinmania.starlarkmap.Hashed

internal class IterHashed<K, V>(
    private val iter: Iterator<Pair<Hashed<K>, V>>,
    private val remaining: () -> Int,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = remaining()
}

internal class Iter<K, V>(
    private val iter: Iterator<Pair<K, V>>,
    private val remaining: () -> Int,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = remaining()
}

internal class IntoIterHashed<K, V>(
    private val iter: Iterator<Pair<Hashed<K>, V>>,
    private val remaining: () -> Int,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = remaining()
}

internal class IntoIter<K, V>(
    private val iter: Iterator<Pair<K, V>>,
    private val remaining: () -> Int,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = remaining()
}

internal class Keys<K, V>(
    private val iter: Iterator<K>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next()
}

internal class Values<K, V>(
    private val iter: Iterator<V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next()
}
