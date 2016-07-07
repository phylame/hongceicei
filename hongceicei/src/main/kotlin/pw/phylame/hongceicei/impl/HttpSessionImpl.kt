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

import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionContext

class HttpSessionImpl(
        context: ServletContext,
        val _id: String,
        var _maxInactiveInterval: Int
) : AttributeWithContext(context), HttpSession {

    private val creationTime = System.currentTimeMillis()

    internal var lastAccessedTime = System.currentTimeMillis()

    override fun getSessionContext(): HttpSessionContext? = null

    override fun getId(): String = _id

    override fun isNew(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun invalidate() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCreationTime(): Long = creationTime

    override fun getLastAccessedTime(): Long = lastAccessedTime

    override fun setMaxInactiveInterval(interval: Int) {
        _maxInactiveInterval = interval
    }

    override fun getMaxInactiveInterval(): Int = _maxInactiveInterval

    override fun putValue(name: String, value: Any) {
        setAttribute(name, value)
    }

    override fun getValueNames(): Array<out String> = attributes.keys.toTypedArray()

    override fun getValue(name: String): Any? = getAttribute(name)

    override fun removeValue(name: String) = removeAttribute(name)
}
