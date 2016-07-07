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

import java.io.BufferedReader
import java.util.*
import javax.servlet.*

abstract class AbstractServletRequest : AttributeProvider(), ServletRequest {
    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getProtocol(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameterMap(): MutableMap<String, Array<String>>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocalPort(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setCharacterEncoding(env: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameterValues(name: String?): Array<out String> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRemoteAddr(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletContext(): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDispatcherType(): DispatcherType {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getReader(): BufferedReader {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isAsyncStarted(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getScheme(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentLengthLong(): Long {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getInputStream(): ServletInputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocalName(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isAsyncSupported(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCharacterEncoding(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameterNames(): Enumeration<String> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentLength(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentType(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAsyncContext(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServerPort(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameter(name: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRemotePort(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRemoteHost(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServerName(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocalAddr(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isSecure(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }
}