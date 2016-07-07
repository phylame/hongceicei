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

import pw.phylame.hongceicei.bufferedReader
import pw.phylame.hongceicei.enumerate
import pw.phylame.hongceicei.toPair
import java.io.BufferedReader
import java.net.Socket
import java.util.*
import javax.servlet.*

abstract class AbstractServletRequest(context: ServletContext) : AttributeWithContext(context), ServletRequest {

    protected lateinit var _protocol: String

    protected var _contentType: String? = null

    protected var contentLength: Long = -1

    protected val params: MutableMap<String, MutableCollection<String>> = HashMap()

    protected lateinit var serverInfo: Pair<String, Int>

    protected var encoding: String? = null

    lateinit var localInfo: Triple<String, String, Int>

    lateinit var remoteInfo: Triple<String, String, Int>

    protected open fun parseSocket(socket: Socket) {
        socket.inetAddress.apply {
            remoteInfo = Triple(hostAddress, hostName, socket.port)
        }

        socket.localAddress.apply {
            localInfo = Triple(hostAddress, hostName, socket.localPort)
        }
    }

    // async support

    override fun isAsyncSupported(): Boolean = false

    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isAsyncStarted(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAsyncContext(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    // protocol etc

    override fun getProtocol(): String = _protocol

    override fun getScheme(): String = protocol.toPair('/').first.toLowerCase()

    override fun isSecure(): Boolean = _protocol.startsWith("HTTPS")

    // request body

    override fun getCharacterEncoding(): String? = encoding

    override fun setCharacterEncoding(env: String) {
        encoding = env
    }

    override fun getContentType(): String? = _contentType

    override fun getContentLength(): Int = if (contentLengthLong > Int.MAX_VALUE) -1 else contentLengthLong.toInt()

    override fun getContentLengthLong(): Long = contentLength

    override fun getReader(): BufferedReader =
            characterEncoding
                    ?.let { inputStream.bufferedReader(characterEncoding!!) }
                    ?: inputStream.bufferedReader()

    override fun getRealPath(path: String): String = servletContext.getRealPath(path)

    override fun getDispatcherType(): DispatcherType {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    // parameters

    override fun getParameter(name: String): String ? = params[name]?.first()

    override fun getParameterMap(): Map<String, Array<String>> =
            params.mapValues { it.value.toTypedArray() }

    override fun getParameterNames(): Enumeration<String> = params.keys.enumerate()

    override fun getParameterValues(name: String): Array<String>? = params[name]?.toTypedArray()

    // local and remote host & port

    override fun getServerName(): String = serverInfo.first

    override fun getServerPort(): Int = serverInfo.second

    override fun getRemoteAddr(): String = remoteInfo.first

    override fun getRemoteHost(): String = remoteInfo.second

    override fun getRemotePort(): Int = remoteInfo.third

    override fun getLocalAddr(): String = localInfo.first

    override fun getLocalName(): String = localInfo.second

    override fun getLocalPort(): Int = localInfo.third
}
