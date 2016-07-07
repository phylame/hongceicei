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

import pw.phylame.hongceicei.add
import pw.phylame.hongceicei.emptyEnumeration
import pw.phylame.hongceicei.enumerate
import pw.phylame.hongceicei.toPair
import java.io.InputStream
import java.net.Socket
import java.security.Principal
import java.util.*
import javax.servlet.ServletContext
import javax.servlet.ServletInputStream
import javax.servlet.http.*

class HttpServletRequestImpl
private constructor(context: ServletContext) : AbstractServletRequest(context), HttpServletRequest {

    private lateinit var method: String

    private lateinit var path: String

    private lateinit var query: String

    private val headers: MutableMap<String, MutableCollection<String>> = HashMap()

    private val locales: Collection<Locale> by lazy {
        headers["Accept-Language"]?.map(Locale::forLanguageTag) ?: listOf(Locale.getDefault())
    }

    private var cookies = LinkedList<Cookie>()

    internal var contextPath = ""
    internal var servletPath = ""

    override fun getLocale(): Locale = locales.first()

    override fun getLocales(): Enumeration<Locale> = locales.enumerate()

    override fun getInputStream(): ServletInputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPathInfo(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCookies(): Array<Cookie> = cookies.toTypedArray()

    // path & query

    override fun getRequestURI(): String = path

    override fun getRequestURL(): StringBuffer = StringBuffer(if (isSecure) "https://" else "http://").append(requestURI)

    override fun getQueryString(): String = query

    override fun getContextPath(): String = contextPath

    override fun getRequestedSessionId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletPath(): String = servletPath

    override fun getSession(create: Boolean): HttpSession {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getSession(): HttpSession {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : HttpUpgradeHandler?> upgrade(handlerClass: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isRequestedSessionIdFromCookie(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPart(name: String?): Part {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRemoteUser(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMethod(): String = method

    override fun isRequestedSessionIdFromURL(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isRequestedSessionIdFromUrl(): Boolean = isRequestedSessionIdFromURL

    override fun isRequestedSessionIdValid(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getUserPrincipal(): Principal {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParts(): MutableCollection<Part>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isUserInRole(role: String?): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun login(username: String?, password: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun logout() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAuthType(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun authenticate(response: HttpServletResponse?): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPathTranslated(): String {
        throw UnsupportedOperationException("not implemented")
    }

    // HTTP headers

    override fun getHeaderNames(): Enumeration<String> = headers.keys.enumerate()

    override fun getHeaders(name: String): Enumeration<String> = headers[name]?.enumerate() ?: emptyEnumeration()

    override fun getHeader(name: String): String ? = headers[name]?.first()

    override fun getIntHeader(name: String): Int = getHeader(name)?.toInt() ?: -1

    override fun getDateHeader(name: String): Long = getHeader(name)?.toLong() ?: -1

    override fun changeSessionId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun parseSocket(socket: Socket) {
        super.parseSocket(socket)
        parseHttpStream(socket.inputStream)
    }

    private fun parseHttpStream(stream: InputStream) {
        stream.bufferedReader().lineSequence().forEachIndexed { no, line ->
            if (line.isEmpty()) {
                return@forEachIndexed
            } else if (no == 0) {
                val parts = line.split(" ")
                method = parts[0]
                val pair = parts[1].toPair('?')
                path = pair.first
                query = pair.second
                query.splitToSequence('&').forEach {
                    params.add(it.toPair('='))
                }
                _protocol = parts[2]
            } else {
                line.toPair(':', doTrim = true).apply {
                    when (first) {
                        "Cookie" -> parseCookies(second)
                        else -> headers.add(this)
                    }
                }
            }
        }
    }

    fun parseCookies(text: String) {
        text.splitToSequence("; ").forEach {
            it.trim().toPair('=', doTrim = true).apply {
                val cookie = Cookie(first, second)
                cookies.add(cookie)
            }
        }
    }

    companion object {
        fun forSocket(context: ServletContext, socket: Socket): HttpServletRequest =
                HttpServletRequestImpl(context).apply {
                    parseSocket(socket)
                }
    }
}
