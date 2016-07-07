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

import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.FilterRegistration
import javax.servlet.Registration

class FilterRegistrationImpl(
        name: String,
        className: String,
        initParams: MutableMap<String, String>,
        context: ServletContextImpl
) : AbstractRegistration(name, className, initParams, context), FilterRegistration {

    override fun getServletNameMappings(): Collection<String> = context.get().servletFilterMappings.keys

    override fun addMappingForServletNames(dispatcherTypes: EnumSet<DispatcherType>?,
                                           isMatchAfter: Boolean, vararg servletNames: String) {
        context.get().ensureNotInitialized()
        val types = dispatcherTypes ?: setOf(DispatcherType.REQUEST)
        throw UnsupportedOperationException("not implemented")
    }

    override fun getUrlPatternMappings(): Collection<String> = context.get().urlFilterMappings.keys

    override fun addMappingForUrlPatterns(dispatcherTypes: EnumSet<DispatcherType>?,
                                          isMatchAfter: Boolean, vararg urlPatterns: String) {
        context.get().ensureNotInitialized()
        val types = dispatcherTypes ?: setOf(DispatcherType.REQUEST)
        throw UnsupportedOperationException("not implemented")
    }

    class FilterDynamic(reg: FilterRegistrationImpl) : AbstractDynamic(reg), FilterRegistration.Dynamic, FilterRegistration by reg {

    }

    companion object {
        fun newDynamic(name: String, className: String, initParams: MutableMap<String, String>,
                       context: ServletContextImpl): FilterDynamic =
                FilterDynamic(FilterRegistrationImpl(name, className, initParams, context))
    }
}