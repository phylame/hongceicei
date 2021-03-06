/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.hongceicei

import java.text.SimpleDateFormat
import java.util.*

val Int.digits: Int
    get() =
    if (this < 10) 1
    else if (this < 100) 2
    else if (this < 1000) 3
    else if (this < 10000) 4
    else if (this < 100000) 5
    else if (this < 1000000) 6
    else if (this < 10000000) 7
    else 8

private fun formatForSize(size: Int): String = "%${size.digits}s"

fun <E> printList(tip: String, c: Collection<E>, prefix: String = "", from: Int = 0, echo: ((E) -> Unit)? = null) {
    val fmt = formatForSize(c.size + from)
    if (tip.isNotEmpty()) {
        println("$prefix$tip")
    }
    c.mapIndexed { i, e ->
        print("$prefix  ${fmt.format(i + 1 + from)}: ")
        echo?.invoke(e) ?: println(e)
    }
}

fun printMap(tip: String, map: Map<String, String>, prefix: String = "", from: Int = 0) {
    val len = map.keys.reduce { s1, s2 -> if (s1.length > s2.length) s1 else s2 }.length
    val fmt = "%${len}s"
    if (tip.isNotEmpty()) {
        println("$prefix$tip")
    }
    map.entries.mapIndexed { i, e ->
        println("$prefix  ${i + 1 + from}: ${fmt.format(e.key)} -> ${e.value}")
    }
}

class IteratorEnumeration<E>(private val iterator: Iterator<E>) : Enumeration<E> {
    override fun nextElement(): E = iterator.next()

    override fun hasMoreElements(): Boolean = iterator.hasNext()
}

fun <E> Iterator<E>.enumerate(): Enumeration<E> = IteratorEnumeration(this)

fun <E> Iterable<E>.enumerate(): Enumeration<E> = this.iterator().enumerate()

fun <E> Array<E>.enumerate(): Enumeration<E> = this.iterator().enumerate()

inline fun <E> Enumeration<E>.forEach(operation: (E) -> Unit) {
    this.iterator().forEach(operation)
}

fun <T> T.iif(condition: Boolean, ok: (T) -> T): T = if (condition) ok(this) else this

fun CharSequence.toPair(sep: Char, ignoreCase: Boolean = false, doTrim: Boolean = false): Pair<String, String> {
    val parts = split(sep, ignoreCase = ignoreCase, limit = 2)
    return parts[0].iif(doTrim, String::trimEnd) to if (parts.size == 2) parts[1].iif(doTrim, String::trimStart) else ""
}

fun CharSequence.toPair(sep: String, ignoreCase: Boolean = false, doTrim: Boolean = false): Pair<String, String> {
    val parts = split(sep, ignoreCase = ignoreCase, limit = 2)
    return parts[0].iif(doTrim, String::trimEnd) to if (parts.size == 2) parts[1].iif(doTrim, String::trimStart) else ""
}

fun <K, V> MutableMap<K, MutableCollection<V>>.add(name: K, value: V) {
    val values = this[name]
    if (values != null) {
        values.add(value)
    } else {
        this[name] = mutableListOf(value)
    }
}

fun <K, V> MutableMap<K, MutableCollection<V>>.add(pair: Pair<K, V>) {
    add(pair.first, pair.second)
}

operator fun <K, V> MutableMap<K, MutableCollection<V>>.set(name: K, value: V) {
    this[name] = mutableListOf(value)
}

fun <K, V> MutableMap<K, MutableCollection<V>>.set(pair: Pair<K, V>) {
    this[pair.first] = pair.second
}

fun <K, V, M : MutableMap<K, V>> M.put(pair: Pair<K, V>): V? = put(pair.first, pair.second)

fun Date.gmtString(): String = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US).format(this)

fun <T : CharSequence> requireNotEmpty(value: T): T = requireNotEmpty(value) { "Required value is empty" }

inline fun <T : CharSequence> requireNotEmpty(value: T, lazyMessage: () -> Any): T {
    if (value.isEmpty()) {
        throw IllegalArgumentException(lazyMessage().toString())
    } else {
        return value
    }
}

inline fun <reified E> emptyEnumeration(): Enumeration<E> = emptyList<E>().enumerate()

