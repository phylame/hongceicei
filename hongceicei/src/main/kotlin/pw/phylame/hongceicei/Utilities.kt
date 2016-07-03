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
