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

import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.lang.ref.WeakReference
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface Container {
    fun start()

    fun stop()

    fun restart()

    val isStarted: Boolean

    val isStopped: Boolean
}

data class ObjectHolder(val path: String, val name: String = "", val initParams: Map<String, String> = emptyMap())

class PatternHolder(val regex: String) {
    val pattern: Pattern by lazy {
        Pattern.compile(regex)
    }

    fun matches(str: CharSequence): Boolean = pattern.matcher(str).matches()
}

class BadWebXmlException(msg: String, val root: String) : Exception("$msg: $root")

class WebApp(val root: String, val contentPath: String) {
    companion object {
        const val DEFAULT_VERSION = "3.1"

        private val logger = LoggerFactory.getLogger(WebApp::class.java)
    }

    lateinit var id: String
        private set
    lateinit var version: String
        private set
    var name: String = ""
        private set
    var description: String = ""
        private set

    internal val contextParams = HashMap<String, String>()

    val context: ServletContextImpl by lazy {
        ServletContextImpl(contextParams, root, WeakReference(this))
    }

    internal val listeners = LinkedList<ObjectHolder>()

    private val contextListener = LinkedList<ServletContextListener>()

    internal val servlets = HashMap<String, ObjectHolder>()
    private val servletCache = HashMap<String, Servlet>()
    private val servletMapping = HashMap<PatternHolder, String>()

    internal val filters = HashMap<String, ObjectHolder>()
    private val filterCache = HashMap<String, Filter>()
    private val urlFilterMapping = HashMap<PatternHolder, String>()
    private val servletFilterMapping = HashMap<PatternHolder, String>()

    private val errorCodeMapping = HashMap<String, String>()

    private val welcomeFiles = LinkedList<String>()

    init {
        load(root)
        val sce = ServletContextEvent(context)
        contextListener.forEach { it.contextInitialized(sce) }
    }

    fun load(input: InputStream) {
        val reader = SAXReader(false)
        parseWebXml(reader.read(input))
    }

    fun load(root: String) {
        val path = Paths.get(root, "WEB-INF", "web.xml")
        val file = path.toFile()
        if (file.exists()) {
            file.inputStream().use {
                load(it)
            }
        }
    }

    fun destroy() {
        servletCache.values.forEach(Servlet::destroy)
        servletCache.clear()
        filterCache.values.forEach(Filter::destroy)
        filterCache.clear()
        val sce = ServletContextEvent(context)
        contextListener.forEach { it.contextDestroyed(sce) }
    }

    private fun parseWebXml(doc: Document) {
        val root = doc.rootElement
        id = root.attributeValue("id") ?: "WebApp_ID${UUID.randomUUID()}"
        version = root.attributeValue("version") ?: DEFAULT_VERSION
        for (element in root.elementIterator()) {
            when ((element as Element).name) {
                "servlet" -> parseComponent(element, "servlet", servlets)
                "servlet-mapping" -> parseComponentMapping(element, "servlet", servlets, servletMapping)
                "filter" -> parseComponent(element, "filter", filters)
                "filter-mapping" -> parseFilterMapping(element)
                "listener" -> parseListener(element.elementText("listener-class"))
                "context-param" -> {
                    contextParams[element.elementText("param-name")] = element.elementText("param-value")
                }
                "error-page" -> parseErrorPage(element)
                "welcome-file-list" -> parseWelcomeFiles(element)
                "display-name" -> {
                    name = element.textTrim
                }
                "description" -> {
                    description = element.textTrim
                }
                else -> logger.debug("unknown element: $element")
            }
        }
    }

    private fun loadClass(path: String, initialize: Boolean = true): Class<*> =
            Class.forName(path, initialize, context.classLoader)

    private fun parseComponent(element: Element, tag: String, saver: HashMap<String, ObjectHolder>) {
        val name = element.elementText("$tag-name")
        val clazz = element.elementText("$tag-class")
        val params = HashMap<String, String>()
        for (param in element.elementIterator("init-param")) {
            if (param is Element) {
                params[param.elementText("param-name")] = param.elementText("param-value")
            }
        }
        saver[name] = ObjectHolder(clazz, name, params)
    }

    private fun parseListener(path: String) {
        val listener = loadClass(path).newInstance()
        if (listener is ServletContextListener) {
            contextListener.add(listener)
        } else {
            println(listener.javaClass)
        }
    }

    private fun transformMapping(pattern: String): String =
            if (pattern == "/") {
                pattern + ".*"
            } else {
                pattern.replace("*", ".*")
            }

    private fun parseComponentMapping(element: Element, tag: String, ref: Map<String, ObjectHolder>,
                                      saver: MutableMap<PatternHolder, String>): String {
        val name = element.elementText("$tag-name")
        if (name !in ref) {
            throw BadWebXmlException("No such $tag declared found in this XML", root)
        }
        for (up in element.elementIterator("url-pattern")) {
            saver[PatternHolder(transformMapping((up as Element).text))] = name
        }
        return name
    }

    private fun parseFilterMapping(element: Element) {
        val name = parseComponentMapping(element, "filter", filters, urlFilterMapping)
        for (sn in element.elementIterator("servlet-name")) {
            servletFilterMapping[PatternHolder((sn as Element).text)] = name
        }
    }

    private fun parseErrorPage(element: Element) {
        val errorCode = element.elementText("error-code")
        val location = element.elementText("location")

        if (errorCode != null) {
            errorCodeMapping[errorCode] = location
        }
    }

    private fun parseWelcomeFiles(element: Element) {
        welcomeFiles.add(element.elementText("welcome-file"))
    }

    fun processContextPath(contentPath: Path, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val path = contentPath.toString()
        val servlet = getMarchedServlet(path)
        return false
    }

    private fun getMatchedComponentKey(contentPath: String, mapping: Map<PatternHolder, String>): String? {
        var best: String? = null
        var key: String? = null
        for ((pattern, name) in mapping) {
            println("$contentPath, ${pattern.regex}, $name")
            if (pattern.matches(contentPath)) {
                println("matched")
                if (best == null || pattern.regex.length > best.length) {
                    best = pattern.regex
                    key = name
                }
            }
        }
        return key
    }

    private fun getMarchedServlet(contentPath: String): Servlet? {
        val servlet = servletCache[contentPath]
        if (servlet != null) {
            return servlet
        }
        val name = getMatchedComponentKey(contentPath, servletMapping)
        if (name != null) {
            createServlet(servlets[name]!!)
        }
        return servlet
    }

    private fun createServlet(holder: ObjectHolder) {
        val path = holder.path
        println(path)
        val servlet = loadClass(path)
    }

    fun list(prefix: String = "") {
        println("WebApp: id: $id, name: $name, version: $version, description: $description")
        if (contextParams.isNotEmpty()) {
            printMap("context params:", contextParams, prefix)
        }
        if (servlets.isEmpty()) {
            println("${prefix}no servlets declared")
        } else {
            printList("servlets:", servlets.values, prefix) {
                println("Servlet: name: ${it.name}, class: ${it.path}")
                if (it.initParams.isNotEmpty()) {
                    printMap("init params:", it.initParams, prefix + prefix + "   ")
                }
            }
            printMap("servlet mappings:", servletMapping.mapKeys { it.key.regex }, prefix)
        }
        if (filters.isEmpty()) {
            println("${prefix}no filters declared")
        } else {
            printList("filters:", filters.values, prefix) {
                println("Filter: name: ${it.name}, class: ${it.path}")
                if (it.initParams.isNotEmpty()) {
                    printMap("init params:", it.initParams, prefix + prefix + "   ")
                }
            }
            printMap("filter mappings:", urlFilterMapping.mapKeys { it.key.regex }, prefix)
            printMap("", servletFilterMapping.mapKeys { it.key.regex }, prefix, urlFilterMapping.size)
        }
        if (listeners.isEmpty()) {
            println("${prefix}no listeners declared")
        } else {
            printList("listeners:", listeners, prefix) {
                "Listener: class: ${it.path}"
            }
        }
        if (errorCodeMapping.isNotEmpty()) {
            printMap("error code mappings:", errorCodeMapping, prefix)
        }
        if (welcomeFiles.isNotEmpty()) {
            println("${prefix}index pages:")
            welcomeFiles.mapIndexed { i, index ->
                println("$prefix  ${i + 1}: $index")
            }
        }
    }
}

interface HttpDispatcher {
    fun handleHttp(request: HttpServletRequestImpl, response: HttpServletResponseImpl)
}

class Server
constructor(val name: String,
            val host: String,
            val port: Int,
            val connector: Connector,
            val version: String = "", base: String = "") :
        Container, HttpDispatcher {
    companion object {
        private val logger = LoggerFactory.getLogger(Server::class.java)
    }

    private val webapps = LinkedHashMap<Path, WebApp>()

    private val appCaches = HashMap<Path, MutableCollection<WebApp>>()

    val base: String by lazy {
        if (base.isNotEmpty())
            base
        else
            File(System.getenv("HONGCEICEI_HOME") ?: System.getProperty("hongceicei.home"), "webapps")
                    .normalize()
                    .path
    }

    init {
        connector.dispatcher = this
    }

    var unpackWar = true

    private var loaded = false

    private fun loadApps() {
        if (loaded) {
            return
        }
        println(base)
        File(base).listFiles().forEach {
            val name = it.name
            if (name.endsWith(".war") || name.endsWith(".jar") || name.endsWith(".zip")) {
                if (unpackWar) {
                    extractWar(it)
                }
            } else {
                val contextPath = "/$name".iif(name == "root" || name == "ROOT") { "" }
                logger.debug("add web app ${it.path} with context path: $contextPath")
                addApp(it.path, contextPath)
            }
        }
        loaded = true
    }

    private fun extractWar(file: File) {

    }

    private fun detectContextPath(root: String, contentPath: String): String =
            if (contentPath.isNotEmpty()) contentPath else root.removePrefix(base)

    fun addApp(root: String, contentPath: String = "") {
        val path = Paths.get(root).normalize()
        if (path in webapps) {
            throw IllegalStateException("Web app in $root already installed")
        }
        if (!File(root, "WEB-INF/web.xml").exists()) {
            logger.warn("web app without web.xml is currently supported")
            return
        }
        webapps[path] = WebApp(root, detectContextPath(root, contentPath))
        appCaches.clear()
    }

    fun getApp(root: String): WebApp? = webapps[Paths.get(root).normalize()]

    fun getContext(uripath: String): ServletContext? {
        throw NotImplementedError()
    }

    val info = "$name/$version"

    private var running = false

    override val isStarted: Boolean get() = running

    override val isStopped: Boolean get() = !running

    inner class ShutdownThread : Thread() {
        var stopped = false
        override fun run() {
            this@Server.stop()
            stopped = true
        }
    }

    private var shutdownThread: ShutdownThread? = null

    override fun start() {
        if (isStarted) {
            throw IllegalStateException("Server already started")
        }
        loadApps()
        logger.debug("Starting server $name...")
        if (shutdownThread?.stopped ?: true) {
            shutdownThread = ShutdownThread()
        }
        Runtime.getRuntime().addShutdownHook(shutdownThread)
        connector.bind(host, port)
        running = true
    }

    override fun stop() {
        if (!isStopped) {
            throw IllegalStateException("Server already stopped")
        }
        logger.debug("Stopping server $name...")
        webapps.values.forEach(WebApp::destroy)
        connector.close()
        running = false
    }

    override fun restart() {
        stop()
        start()
    }

    override fun handleHttp(request: HttpServletRequestImpl, response: HttpServletResponseImpl) {
        val contentPath = Paths.get(detectContextPath(request.requestURI))
        logger.debug("process request for content path: $contentPath")
        val apps = appCaches[contentPath]
        if (apps != null) {
            logger.debug("found cached apps: $apps")
            apps.forEach {
                it.processContextPath(contentPath, request, response)
            }
            return
        }
        var rootApp: WebApp? = null
        for ((path, app) in webapps) {
            if (contentPath == path) {
                logger.debug("Web app ${app.name} mapped for this request")
                appCaches[contentPath] = app
                app.processContextPath(contentPath, request, response)
                return
            }
            if (path.endsWith("root") || path.endsWith("ROOT")) {
                rootApp = app
            }
        }
        logger.debug("no app matched this context path: $contentPath")
        if (rootApp == null || !forwardToRootApp(rootApp, contentPath, request, response)) {
            forwardDefaultServlet(contentPath, request, response)
        }
    }

    fun forwardToRootApp(app: WebApp, contentPath: Path, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        logger.debug("try forward to root app for: $contentPath")
        return app.processContextPath(contentPath, request, response)
    }

    fun forwardDefaultServlet(contentPath: Path, request: HttpServletRequest, response: HttpServletResponse) {
        logger.debug("try forward to default servlet for: $contentPath")
    }

    fun detectContextPath(path: String): String {
        val com = path.split('/')[1]
        if (com.isEmpty() || '.' in com) {
            return "/"
        } else {
            return "/$com"
        }
    }

    fun list(prefix: String = "") {
        println("Server: name: $name, host: $host:$port, state: ${if (running) "running" else "stopped"}")
        webapps.values.mapIndexed { i, app ->
            print("${i + 1}: ")
            app.list(prefix + "  ")
        }
    }
}

object Hongceicei {

}

fun main(args: Array<String>) {
    val server = Server("hongceicei", "localhost", 8081, LegacyConnector(16, debug = true))
    server.start()
}
