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
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

class ServletConfigImpl(val name: String, val initParams: Map<String, String>, val context: ServletContext) :
        ServletConfig {
    override fun getServletName(): String = name

    override fun getServletContext(): ServletContext = context

    override fun getInitParameter(name: String): String? = initParams[name]

    override fun getInitParameterNames(): Enumeration<String> = initParams.keys.enumerate()
}