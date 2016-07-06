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

import java.lang.ref.WeakReference
import javax.servlet.Registration

open class AbstractRegistration(
        val _name: String,
        val _className: String,
        val initParams: MutableMap<String, String>,
        context: ServletContextImpl
) : Registration {

    protected val context: WeakReference<ServletContextImpl> = WeakReference(context)

    override fun getInitParameters(): Map<String, String> = initParams

    override fun getName(): String = _name

    override fun getClassName(): String = _className

    override fun setInitParameters(initParameters: Map<String, String>): Set<String> =
            initParameters.filter { !setInitParameter(it.key, it.value) }.keys

    override fun setInitParameter(name: String, value: String): Boolean {
        context.get().ensureNotInitialized()
        return if (name in initParams) {
            false
        } else {
            initParams[name] = value
            true
        }
    }

    override fun getInitParameter(name: String): String? = initParams[_name]

    open class AbstractDynamic(reg: Registration) : Registration.Dynamic, Registration by reg {

        override fun setAsyncSupported(isAsyncSupported: Boolean) {
            throw UnsupportedOperationException("not implemented")
        }
    }
}