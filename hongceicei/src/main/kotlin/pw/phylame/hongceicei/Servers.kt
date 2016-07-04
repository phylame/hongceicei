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
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface Container {
    fun start()

    fun stop()

    fun restart()

    val isStarted: Boolean

    val isStopped: Boolean
}

data class Holder(val path: String, val name: String = "", val initParams: Map<String, String> = emptyMap())

class BadWebXmlException(msg: String, val root: String) : Exception(msg)

class WebApp(val root: String) {
    companion object {
        const val DEFAULT_VERSION = "3.1"
    }

    lateinit var id: String
        private set
    lateinit var version: String
        private set
    var name: String = ""
        private set
    var description: String = ""
        private set

    val contextParams = HashMap<String, String>()
    private val listeners = LinkedList<Holder>()
    private val servlets = HashMap<String, Holder>()
    private val servletMapping = HashMap<String, String>()
    private val filters = HashMap<String, Holder>()
    private val urlFilterMapping = HashMap<String, String>()
    private val servletFilterMapping = HashMap<String, String>()
    private val errorCodeMapping = HashMap<String, String>()

    private val welcomeFiles = LinkedList<String>()

    init {
        load(root)
    }

    fun load(input: InputStream) {
        val reader = SAXReader(false)
        parseWebXml(reader.read(input))
    }

    fun load(root: String) {
        val path = Paths.get(root, "WEB-INF", "web.xml")
        path.toFile().inputStream().use {
            load(it)
        }
    }

    private fun parseWebXml(doc: Document) {
        val root = doc.rootElement
        id = root.attributeValue("id") ?: "WebApp_ID${UUID.randomUUID()}"
        version = root.attributeValue("version") ?: DEFAULT_VERSION
        for (element in root.elementIterator()) {
            when ((element as Element).name) {
                "servlet" -> parseComponent(element, "servlet", servlets)
                "servlet-mapping" -> parseServletMapping(element)
                "filter" -> parseComponent(element, "filter", filters)
                "filter-mapping" -> parseFilterMapping(element)
                "listener" -> {
                    listeners.add(Holder(element.elementText("listener-class")))
                }
                "context-param" -> {
                    contextParams[element.elementText("param-name")] = element.elementText("param-value")
                }
                "error-page" -> parseErrorPage(element)
                "welcome-file-c" -> parseWelcomeFiles(element)
                "display-name" -> {
                    name = element.textTrim
                }
                "description" -> {
                    description = element.textTrim
                }
                else -> println("unknown element: $element")
            }
        }
    }

    private fun parseComponent(element: Element, tag: String, saver: HashMap<String, Holder>) {
        val name = element.elementText("$tag-name")
        val clazz = element.elementText("$tag-class")
        val params = HashMap<String, String>()
        for (param in element.elementIterator("init-param")) {
            if (param is Element) {
                params[param.elementText("param-name")] = param.elementText("param-value")
            }
        }
        saver[name] = Holder(clazz, name, params)
    }

    private fun parseServletMapping(element: Element) {
        val name = element.elementText("servlet-name")
        if (name !in servlets) {
            throw BadWebXmlException("No such servlet declared found in this XML", root)
        }
        servletMapping[element.elementText("url-pattern")] = name
    }

    private fun parseFilterMapping(element: Element) {
        val name = element.elementText("filter-name")
        if (name !in filters) {
            throw BadWebXmlException("No such filter declared found in this XML", root)
        }
        val url = element.elementText("url-pattern")
        if (url != null) {
            urlFilterMapping[url] = name
        } else {
            for (sub in element.elementIterator("servlet-name")) {
                servletFilterMapping[(sub as Element).name] = name
            }
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
            printMap("servlet mappings:", servletMapping, prefix)
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
            printMap("filter mappings:", urlFilterMapping, prefix)
            printMap("", servletFilterMapping, prefix, urlFilterMapping.size)
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
    fun handleHttp(request: HttpServletRequest, response: HttpServletResponse)
}

class Server
constructor(val name: String, val host: String, val port: Int, val connector: Connector) :
        Container, HttpDispatcher {
    private val webapps = LinkedHashMap<Path, WebApp>()

    init {
        connector.dispatcher = this
    }

    fun addApp(root: String) {
        val path = Paths.get(root).normalize()
        if (path in webapps) {
            throw IllegalStateException("Web app in $root already installed")
        }
        webapps[path] = WebApp(root)
    }

    fun getApp(root: String): WebApp? = webapps[Paths.get(root).normalize()]

    private var running = false

    override fun start() {
        if (running) {
            throw IllegalStateException("Server already started")
        }
        connector.bind(host, port)
        running = true
    }

    override fun stop() {
        if (!running) {
            throw IllegalStateException("Server already stopped")
        }
        connector.close()
        running = false
    }

    override fun restart() {
        stop()
        start()
    }

    override val isStarted: Boolean get() = running

    override val isStopped: Boolean get() = !running

    override fun handleHttp(request: HttpServletRequest, response: HttpServletResponse) {
        println("now, do filters and servlets")
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
    val server = Server("hongceicei", "localhost", 8081, LegacyConnector(16))
    val root = "/media/pw/azone"
    server.addApp("$root/devel/web/tomcat-8.0.14/webapps/root")
    server.addApp("$root/devel/web/tomcat-8.0.14/webapps/manager")
    server.start()
}
