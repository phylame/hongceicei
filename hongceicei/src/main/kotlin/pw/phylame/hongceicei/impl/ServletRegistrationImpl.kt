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

import javax.servlet.MultipartConfigElement
import javax.servlet.ServletRegistration
import javax.servlet.ServletSecurityElement

class ServletRegistrationImpl(name: String,
                              className: String,
                              initParams: MutableMap<String, String>,
                              context: ServletContextImpl
) : AbstractRegistration(name, className, initParams, context), ServletRegistration {
    override fun getRunAsRole(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMappings(): Collection<String> = context.get().servletMappings.keys

    override fun addMapping(vararg urlPatterns: String): MutableSet<String> {
        context.get().ensureNotInitialized()
        throw UnsupportedOperationException("not implemented")
    }

    class ServletDynamic(reg: ServletRegistration) :
            AbstractDynamic(reg), ServletRegistration.Dynamic, ServletRegistration by reg {
        override fun setRunAsRole(roleName: String?) {
            throw UnsupportedOperationException("not implemented")
        }

        override fun setMultipartConfig(multipartConfig: MultipartConfigElement?) {
            throw UnsupportedOperationException("not implemented")
        }

        override fun setServletSecurity(constraint: ServletSecurityElement?): MutableSet<String>? {
            throw UnsupportedOperationException("not implemented")
        }

        override fun setLoadOnStartup(loadOnStartup: Int) {
            throw UnsupportedOperationException("not implemented")
        }
    }

    companion object {
        fun newDynamic(name: String, className: String, initParams: MutableMap<String, String>,
                       context: ServletContextImpl): ServletDynamic =
                ServletDynamic(ServletRegistrationImpl(name, className, initParams, context))
    }
}