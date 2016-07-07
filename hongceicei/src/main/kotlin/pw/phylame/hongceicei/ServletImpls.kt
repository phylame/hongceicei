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

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.lang.ref.WeakReference
import java.net.Socket
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.security.Principal
import java.util.*
import javax.servlet.*
import javax.servlet.descriptor.JspConfigDescriptor
import javax.servlet.http.*

open class BaseImpl {
    private val attributes = HashMap<String?, Any?>()

    private var locale = Locale.getDefault()

    fun getAttributeNames(): Enumeration<String?> = attributes.keys.enumerate()

    fun getAttribute(name: String?): Any? = attributes[name]

    fun setAttribute(name: String?, value: Any?) {
        attributes[name] = value
    }

    fun removeAttribute(name: String?): Unit {
        attributes.remove(name)
    }

    fun setLocale(locale: Locale) {
        this.locale = locale
    }

    fun getLocale(): Locale = locale
}

class ServletContextImpl(val initParams: Map<String, String>, val root: String, val webapp: WeakReference<WebApp>) :
        BaseImpl(), ServletContext {
    companion object {
        private val logger = LoggerFactory.getLogger(ServletContextImpl::class.java)
    }

    private val classLoader by lazy {
        val paths = ArrayList<File>()
        paths.add(Paths.get(root, "WEB-INF/classes").toFile())
        val parent = Paths.get(root, "WEB-INF/lib").toFile()
        paths.addAll(parent.listFiles() { dir, name -> name.endsWith(".jar") || name.endsWith(".zip") })
        return@lazy URLClassLoader(paths.map { it.toURI().toURL() }.toTypedArray())
    }

    override fun getEffectiveMinorVersion(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addListener(className: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : EventListener?> addListener(t: T) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addListener(listenerClass: Class<out EventListener>?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getClassLoader(): ClassLoader = classLoader

    override fun log(msg: String?) {
        logger.debug(msg)
    }

    override fun log(exception: Exception?, msg: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun log(message: String?, throwable: Throwable?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getFilterRegistration(filterName: String?): FilterRegistration {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setSessionTrackingModes(sessionTrackingModes: MutableSet<SessionTrackingMode>?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getNamedDispatcher(name: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getFilterRegistrations(): MutableMap<String, out FilterRegistration>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletNames(): Enumeration<String> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun declareRoles(vararg roleNames: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Filter?> createFilter(clazz: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getInitParameter(name: String?): String? = initParams[name]

    override fun getContextPath(): String = webapp.get().contentPath

    override fun getSessionCookieConfig(): SessionCookieConfig {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContext(uripath: String?): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletRegistration(servletName: String?): ServletRegistration {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : EventListener?> createListener(clazz: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, className: String?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, servlet: Servlet?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, servletClass: Class<out Servlet>?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServlets(): Enumeration<Servlet> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletRegistrations(): MutableMap<String, out ServletRegistration>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServerInfo(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getEffectiveSessionTrackingModes(): MutableSet<SessionTrackingMode>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServlet(name: String?): Servlet {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Servlet?> createServlet(clazz: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getEffectiveMajorVersion(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getResource(path: String?): URL {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMajorVersion(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setInitParameter(name: String?, value: String?): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getResourceAsStream(path: String?): InputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDefaultSessionTrackingModes(): MutableSet<SessionTrackingMode>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMimeType(file: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMinorVersion(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletContextName(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, className: String?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, filter: Filter?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, filterClass: Class<out Filter>?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getVirtualServerName(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getResourcePaths(path: String?): MutableSet<String>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getInitParameterNames(): Enumeration<String> = initParams.keys.enumerate()
}

class HttpSessionImpl : BaseImpl(), HttpSession {
    override fun putValue(name: String?, value: Any?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setMaxInactiveInterval(interval: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun removeValue(name: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLastAccessedTime(): Long {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMaxInactiveInterval(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getValue(name: String?): Any {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCreationTime(): Long {
        throw UnsupportedOperationException("not implemented")
    }

    override fun invalidate() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletContext(): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isNew(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getValueNames(): Array<out String> {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getSessionContext(): HttpSessionContext {
        throw UnsupportedOperationException("not implemented")
    }
}

abstract class ServletRequestImpl : BaseImpl(), ServletRequest {
    private lateinit var remoteAddr: String

    private lateinit var remoteHost: String

    private var remotePort = -1

    private lateinit var localAddr: String

    private lateinit var localHost: String

    private var localPort = -1

    open internal fun parseSocket(socket: Socket) {
        socket.inetAddress.apply {
            remoteHost = hostName
            remoteAddr = hostAddress
        }
        remotePort = socket.port

        socket.localAddress.apply {
            localHost = hostName
            localAddr = hostAddress
        }
        localPort = socket.localPort
    }

    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setCharacterEncoding(env: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletContext(): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDispatcherType(): DispatcherType {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getReader(): BufferedReader = inputStream.bufferedReader()

    override fun isAsyncStarted(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentLengthLong(): Long {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isAsyncSupported(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCharacterEncoding(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentType(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAsyncContext(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRemoteAddr(): String = remoteAddr

    override fun getRemoteHost(): String = remoteHost

    override fun getRemotePort(): Int = remotePort

    override fun getLocalAddr(): String = localAddr

    override fun getLocalName(): String = localHost

    override fun getLocalPort(): Int = localPort
}

class HttpServletRequestImpl : ServletRequestImpl(), HttpServletRequest {
    private lateinit var method: String

    private lateinit var path: String

    private lateinit var query: String

    private val params = LinkedHashMap<String, MutableCollection<String>>()

    private lateinit var protocol: String

    private val headers = LinkedHashMap<String, MutableCollection<String>>()

    private var cookies = LinkedList<Cookie>()

    internal var contextPath = ""

    override fun parseSocket(socket: Socket) {
        super.parseSocket(socket)
        parseStream(socket.inputStream)
    }

    fun parseStream(input: InputStream) {
        val br = input.bufferedReader()
        var no = 0
        var line: String? = br.readLine()
        while (line != null) {
            if (line.isEmpty()) {
                break
            }
            if (no == 0) {
                val parts = line.split(" ")
                method = parts[0]
                val pair = parts[1].toPair('?')
                path = pair.first
                query = pair.second
                query.splitToSequence('&').forEach {
                    params.add(it.toPair('='))
                }
                protocol = parts[2]
            } else {
                line.toPair(':', doTrim = true).apply {
                    when (first) {
                        "Cookie" -> parseCookies(second)
                        else -> headers.add(this)
                    }
                }
            }
            line = br.readLine()
            no++
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

    override fun isUserInRole(role: String?): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPathInfo(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCookies(): Array<out Cookie> = cookies.toTypedArray()

    override fun getRequestURL(): StringBuffer = StringBuffer(if (isSecure) "https://" else "http://").append(path)

    override fun login(username: String?, password: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContextPath(): String = contextPath

    override fun isRequestedSessionIdValid(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDateHeader(name: String?): Long = getHeader(name)?.toLong() ?: -1

    override fun getRequestedSessionId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServletPath(): String = path.removePrefix(contextPath)

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

    override fun isRequestedSessionIdFromUrl(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getQueryString(): String = query

    override fun getHeaders(name: String): Enumeration<String>? = headers[name]?.enumerate()

    override fun getUserPrincipal(): Principal {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParts(): MutableCollection<Part>? {
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

    override fun getHeader(name: String?): String? = headers[name]?.first()

    override fun getIntHeader(name: String?): Int = getHeader(name)?.toInt() ?: -1

    override fun changeSessionId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestURI(): String = path

    override fun getHeaderNames(): Enumeration<String> = headers.keys.enumerate()

    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getProtocol(): String = protocol

    override fun getParameterMap(): MutableMap<String, Array<String>>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setCharacterEncoding(env: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameterValues(name: String?): Array<out String>? = params[name]?.toTypedArray()

    override fun isAsyncStarted(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentLengthLong(): Long {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocales(): Enumeration<Locale> =
            headers["Accept-Language"]
                    ?.map(Locale::forLanguageTag)?.enumerate()
                    ?: arrayOf(locale).enumerate()

    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getServerPort(): Int = getHeader("Host")!!.split('/', limit = 2).last().toInt()

    override fun getServerName(): String = getHeader("Host")!!.split('/', limit = 2).first()

    override fun isSecure(): Boolean = protocol.startsWith("HTTPS")

    override fun getServletContext(): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDispatcherType(): DispatcherType {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getScheme(): String = protocol.toPair('/').first.toLowerCase()

    override fun getInputStream(): ServletInputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isAsyncSupported(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCharacterEncoding(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameterNames(): Enumeration<String> = params.keys.enumerate()

    override fun getContentLength(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentType(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAsyncContext(): AsyncContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getParameter(name: String?): String? = params[name]?.first()

}

class HttpServletResponseImpl(val socket: Socket) : BaseImpl(), HttpServletResponse {
    companion object {
        fun forSocket(socket: Socket): HttpServletResponseImpl {
            return HttpServletResponseImpl(socket)
        }
    }

    internal val headers = LinkedHashMap<String?, MutableCollection<String?>?>()

    private var status: Int = 200

    internal val cookies = LinkedList<Cookie>()

    internal val stream = ServletOutputStreamImpl(socket)

    override fun encodeURL(url: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun encodeUrl(url: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addIntHeader(name: String?, value: Int) {
        addHeader(name, value.toString())
    }

    override fun addCookie(cookie: Cookie) {
        cookies.add(cookie)
    }

    override fun encodeRedirectUrl(url: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun encodeRedirectURL(url: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun sendRedirect(location: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun sendError(sc: Int, msg: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun sendError(sc: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addDateHeader(name: String?, date: Long) {
        addHeader(name, date.toString())
    }

    override fun getHeaders(name: String?): MutableCollection<String?>? = headers[name]

    override fun addHeader(name: String?, value: String?) {
        val set = headers[name]
        if (set == null) {
            headers[name] = mutableListOf(value)
        } else {
            set.add(value)
        }
    }

    override fun setDateHeader(name: String?, date: Long) {
        setHeader(name, date.toString())
    }

    override fun getStatus(): Int = status

    override fun setStatus(sc: Int) {
        status = sc
    }

    override fun setStatus(sc: Int, sm: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getHeader(name: String?): String? = headers[name]?.first()

    override fun containsHeader(name: String?): Boolean = headers[name]?.isNotEmpty() ?: false

    override fun setIntHeader(name: String?, value: Int) {
        setHeader(name, value.toString())
    }

    override fun getHeaderNames(): MutableCollection<String?>? = headers.keys

    override fun setHeader(name: String?, value: String?) {
        headers[name] = mutableListOf(value)
    }

    override fun flushBuffer() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setBufferSize(size: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setContentLengthLong(len: Long) {
        setHeader("Content-Length", len.toString())
    }

    override fun setCharacterEncoding(charset: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setContentLength(len: Int) {
        setIntHeader("Content-Length", len)
    }

    override fun getBufferSize(): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun resetBuffer() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun reset() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCharacterEncoding(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isCommitted(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContentType(): String? = getHeader("Content-Type")

    override fun getWriter(): PrintWriter = PrintWriter(stream)

    override fun getOutputStream(): ServletOutputStream = stream

    override fun setContentType(type: String?) {
        setHeader("Content-Type", type)
    }
}

class ServletOutputStreamImpl(val socket: Socket) : ServletOutputStream() {
    override fun write(b: Int) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isReady(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setWriteListener(writeListener: WriteListener?) {
        throw UnsupportedOperationException("not implemented")
    }

}