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

package pw.phylame.hongceicei.impl

import pw.phylame.hongceicei.enumerate
import java.util.*

open class AttributeProvider {
    protected val attributes = HashMap<String, Any?>()

    fun getAttributeNames(): Enumeration<String> = attributes.keys.enumerate()

    fun getAttribute(name: String): Any? = attributes[name]

    fun setAttribute(name: String, value: Any?) {
        if (value == null) {
            removeAttribute(name)
        } else {
            attributes.put(name, value)?.apply { attributeReplaced(name, this) } ?: attributeAdded(name, value)
        }
    }

    fun removeAttribute(name: String) {
        if (name in attributes) {
            attributeRemoved(name, attributes.remove(name)!!)
        }
    }

    protected open fun attributeAdded(name: String, value: Any) {

    }

    protected open fun attributeRemoved(name: String, value: Any) {

    }

    protected open fun attributeReplaced(name: String, value: Any) {

    }
}