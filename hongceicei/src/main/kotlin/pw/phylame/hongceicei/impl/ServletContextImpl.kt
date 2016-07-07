/*
** * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

import org.slf4j.LoggerFactory
import pw.phylame.hongceicei.Server
import pw.phylame.hongceicei.emptyEnumeration
import pw.phylame.hongceicei.enumerate
import pw.phylame.hongceicei.requireNotEmpty
import java.io.File
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.servlet.*
import javax.servlet.descriptor.JspConfigDescriptor
import javax.servlet.http.HttpSessionAttributeListener
import javax.servlet.http.HttpSessionIdListener
import javax.servlet.http.HttpSessionListener

class ServletContextImpl(
        private val _contextPath: String,
        val name: String,
        val rootDir: String,
        val initParams: MutableMap<String, String?>,
        server: Server,
        val isWar: Boolean
) : AttributeProvider(), ServletContext {

    private val classLoader by lazy {
        val paths = ArrayList<File>()
        paths.add(Paths.get(rootDir).toFile())
        paths.add(Paths.get(rootDir, "WEB-INF/classes").toFile())
        val parent = Paths.get(rootDir, "WEB-INF/lib").toFile()
        paths.addAll(parent.listFiles() { dir, name -> name.endsWith(".jar") || name.endsWith(".zip") })
        return@lazy URLClassLoader(paths.map { it.toURI().toURL() }.toTypedArray())
    }

    private var server: WeakReference<Server> = WeakReference(server)

    private var initialized = false

    private val contextListeners = LinkedList<ServletContextListener>()
    private val contextAttributeListeners = LinkedList<ServletContextAttributeListener>()
    private val requestListeners = LinkedList<ServletRequestListener>()
    private val requestAttributeListeners = LinkedList<ServletRequestAttributeListener>()
    private val sessionListeners = LinkedList<HttpSessionListener>()
    private val sessionIdListeners = LinkedList<HttpSessionIdListener>()
    private val sessionAttributeListeners = LinkedList<HttpSessionAttributeListener>()

    private val servlets = LinkedHashMap<String, Servlet>()
    internal val servletMappings = LinkedHashMap<String, String>()
    internal val _servletRegistrations = HashMap<String, ServletRegistration.Dynamic>()

    private val filters = LinkedHashMap<String, Filter>()
    internal val urlFilterMappings = LinkedHashMap<String, String>()
    internal val servletFilterMappings = LinkedHashMap<String, String>()
    internal val _filterRegistrations = HashMap<String, FilterRegistration.Dynamic>()

    internal fun ensureNotInitialized() {
        check(initialized) { "Servlet context has already been initialized" }
    }

    // server & context information

    override fun getVirtualServerName(): String = server.get().name

    override fun getServerInfo(): String = server.get().info

    override fun getServletContextName(): String = name

    override fun getContextPath(): String = _contextPath

    override fun getEffectiveMajorVersion(): Int = majorVersion

    override fun getMajorVersion(): Int = 3

    override fun getEffectiveMinorVersion(): Int = minorVersion

    override fun getMinorVersion(): Int = 0

    override fun getContext(uripath: String): ServletContext? = server.get().getContext(uripath)

    override fun getClassLoader(): ClassLoader = classLoader

    // init parameters

    override fun getInitParameterNames(): Enumeration<String> = initParams.keys.enumerate()

    override fun getInitParameter(name: String): String? = initParams[name]

    override fun setInitParameter(name: String, value: String?): Boolean {
        if (name in initParams) {
            return false
        }
        initParams[name] = value
        return true
    }

    // listener operations

    override fun addListener(className: String) {
        val clazz = Class.forName(className, true, classLoader)
        checkListenerClass(clazz)
        @Suppress("UNCHECKED_CAST")
        addListener(clazz as Class<out EventListener>, false)
    }

    override fun <T : EventListener?> addListener(t: T) {
        addListener(t, true)
    }

    private fun <T : EventListener?> addListener(t: T, check: Boolean = true) {
        ensureNotInitialized()
        if (check) {
            checkListenerClass((t as EventListener).javaClass)
        }
        when (t) {
            is ServletContextListener -> contextListeners.add(t)
            is ServletContextAttributeListener -> contextAttributeListeners.add(t)
            is ServletRequestListener -> requestListeners.add(t)
            is ServletRequestAttributeListener -> requestAttributeListeners.add(t)
            is HttpSessionListener -> sessionListeners.add(t)
            is HttpSessionIdListener -> sessionIdListeners.add(t)
            is HttpSessionAttributeListener -> sessionAttributeListeners.add(t)
        }
    }

    override fun addListener(listenerClass: Class<out EventListener>) {
        addListener(listenerClass, true)
    }

    private fun addListener(listenerClass: Class<out EventListener>, check: Boolean = true) {
        if (check) {
            checkListenerClass(listenerClass)
        }
        addListener(createListener(listenerClass), false)
    }

    override fun <T : EventListener?> createListener(clazz: Class<T>): T = clazz.newInstance()

    // filter operations

    override fun getFilterRegistration(filterName: String): FilterRegistration? = _filterRegistrations[filterName]

    override fun getFilterRegistrations(): Map<String, FilterRegistration> = _filterRegistrations

    override fun addFilter(filterName: String, className: String): FilterRegistration.Dynamic {
        val clazz = Class.forName(className, true, classLoader)
        require(Filter::class.java.isAssignableFrom(clazz)) { "Specified filter class $className is not a filter" }
        @Suppress("UNCHECKED_CAST")
        return addFilter(filterName, clazz as Class<out Filter>)
    }

    override fun addFilter(filterName: String, filter: Filter): FilterRegistration.Dynamic {
        ensureNotInitialized()
        return if (filterName in _filterRegistrations) {
            _filterRegistrations[filterName]!!
        } else {
            filters[filterName] = filter
            val dynamic = FilterRegistrationImpl.newDynamic(filterName, filter.javaClass.name, HashMap(), this)
            _filterRegistrations[filterName] = dynamic
            return dynamic
        }
    }

    override fun addFilter(filterName: String, filterClass: Class<out Filter>): FilterRegistration.Dynamic {
        return addFilter(filterName, createFilter(filterClass))
    }

    override fun <T : Filter?> createFilter(clazz: Class<T>): T = clazz.newInstance()

    // session operations

    override fun getSessionCookieConfig(): SessionCookieConfig {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setSessionTrackingModes(sessionTrackingModes: Set<SessionTrackingMode>) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getDefaultSessionTrackingModes(): MutableSet<SessionTrackingMode>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getEffectiveSessionTrackingModes(): MutableSet<SessionTrackingMode>? {
        throw UnsupportedOperationException("not implemented")
    }

    // servlet operations

    override fun getServletRegistration(servletName: String): ServletRegistration ? = _servletRegistrations[servletName]

    override fun getServletRegistrations(): Map<String, ServletRegistration> = _servletRegistrations

    override fun addServlet(servletName: String, className: String): ServletRegistration.Dynamic {
        val clazz = Class.forName(className, true, classLoader)
        require(Servlet::class.java.isAssignableFrom(clazz)) { "Specified class name $className is not a servlet" }
        @Suppress("UNCHECKED_CAST")
        return addServlet(servletName, clazz as Class<out Servlet>)
    }

    override fun addServlet(servletName: String, servlet: Servlet): ServletRegistration.Dynamic {
        ensureNotInitialized()
        requireNotEmpty(servletName) { "Name of servlet cannot be empty" }
        return if (servletName in _servletRegistrations) {
            _servletRegistrations[servletName]!!
        } else {
            servlets[servletName] = servlet
            val dynamic = ServletRegistrationImpl.newDynamic(servletName, servlet.javaClass.name, HashMap(), this)
            _servletRegistrations[servletName] = dynamic
            return dynamic
        }
    }

    override fun addServlet(servletName: String, servletClass: Class<out Servlet>): ServletRegistration.Dynamic {
        return addServlet(servletName, createServlet(servletClass))
    }

    override fun <T : Servlet?> createServlet(clazz: Class<T>): T = clazz.newInstance()

    override fun getServlet(name: String?): Servlet? = null

    override fun getServlets(): Enumeration<Servlet> = emptyEnumeration()

    override fun getServletNames(): Enumeration<String> = emptyEnumeration()

    // path & resource operations

    private fun pathFor(path: String): Path = Paths.get(rootDir, path).normalize()

    override fun getRealPath(path: String): String = pathFor(path).toString()

    override fun getResource(path: String): URL = classLoader.getResource(path)

    override fun getResourceAsStream(path: String): InputStream = classLoader.getResourceAsStream(path)

    override fun getResourcePaths(path: String): MutableSet<String>? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getMimeType(file: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    // utilities

    override fun getNamedDispatcher(name: String): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        throw UnsupportedOperationException("not implemented")
    }

    override fun declareRoles(vararg roleNames: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun log(msg: String) {
        logger.debug(msg)
    }

    override fun log(exception: Exception, msg: String) {
        log(msg, exception)
    }

    override fun log(message: String, throwable: Throwable) {
        logger.debug(message, throwable)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServletContextImpl::class.java)

        private val listenerClasses = arrayOf(
                ServletContextListener::class.java,
                ServletContextAttributeListener::class.java,
                ServletRequestListener::class.java,
                ServletRequestAttributeListener::class.java,
                HttpSessionListener::class.java,
                HttpSessionIdListener::class.java,
                HttpSessionAttributeListener::class.java
        )

        fun <T> checkListenerClass(clazz: Class<T>) {
            require(!listenerClasses.any { it.isAssignableFrom(clazz) }) { "Unsupported listener class: $clazz" }
        }
    }
}
