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

import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.nio.file.Paths
import java.util.*

interface Container {
    fun start()

    fun stop()

    fun restart()

    val isStarted: Boolean

    val isStopped: Boolean
}


fun loadWebXml(root: String) {
    val path = Paths.get(root, "WEB-INF", "web.xml")
    val reader = SAXReader(false)
    val doc = reader.read(path.toFile())
    val top = doc.rootElement
    for (node in top.elementIterator()) {
        if (node is Element)
            when (node.name) {
                "servlet" -> parseServlet(node)
                "filter" -> parseFilter(node)
                "servlet-mapping" -> parseServletMapping(node)
                "filter-mapping" -> parseFilterMapping(node)
                "error-page" -> parseErrorPage(node)
            }
    }
}

data class Holder(val name: String, val path: String, val initParams: Map<String, String>)

val servlets = HashMap<String, Holder>()

val servletMapping = HashMap<String, Holder>()

fun parseServlet(element: Element) {
    val name = element.elementText("servlet-name")
    val clazz = element.elementText("servlet-class")
    val params = HashMap<String, String>()
    for (param in element.elementIterator("init-param")) {
        if (param is Element) {
            params[param.elementText("param-name")] = param.elementText("param-value")
        }
    }
    servlets[name] = Holder(name, clazz, params)
}

val filters = HashMap<String, Holder>()

val filterMapping = HashMap<String, Holder>()

fun parseFilter(element: Element) {
    val name = element.elementText("filter-name")
    val clazz = element.elementText("filter-class")
    val params = HashMap<String, String>()
    for (param in element.elementIterator("init-param")) {
        if (param is Element) {
            params[param.elementText("param-name")] = param.elementText("param-value")
        }
    }
    filters[name] = Holder(name, clazz, params)
}

fun parseServletMapping(element: Element) {
    val name = element.elementText("servlet-name")
    val pattern = element.elementText("url-pattern")
    val servlet = servlets[name]
    if (servlet == null) {
        System.err?.println("no servlet $name found")
        System.exit(-1)
    }
    servletMapping[pattern] = servlet!!
}

fun parseFilterMapping(element: Element) {
    val name = element.elementText("filter-name")
    val pattern = element.elementText("url-pattern")
    val filter = filters[name]
    if (filter == null) {
        System.err?.println("no filter $name found")
        System.exit(-1)
    }
    if (pattern != null) {
        filterMapping[pattern] = filter!!
    }
}

val errorMapping = HashMap<String, String>()

fun parseErrorPage(element: Element) {
    val code = element.elementText("error-code")
    val location = element.elementText("location")
    errorMapping[code] = location
}

fun main(args: Array<String>) {
    val ssl = true
    val port = 8080
    loadWebXml("""D:\devel\web\tomcat-8.0.14\webapps\host-manager""")
    println(servlets)
    println(servletMapping)
    println(filters)
    println(filterMapping)
    println(errorMapping)
//    val bossGroup = NioEventLoopGroup(1);
//    val workerGroup = NioEventLoopGroup();
//    try {
//        val bootstrap = ServerBootstrap()
//                .group(bossGroup, workerGroup)
//                .channel(NioServerSocketChannel::class.java)
//                .childHandler(object : ChannelInitializer<SocketChannel>() {
//                    override fun initChannel(ch: SocketChannel) {
//                        val pipeline = ch.pipeline()
//                        if (ssl) {
//                            // TODO: ssl
//                        }
//                        pipeline.addLast("decoder", HttpRequestDecoder());
//
//                        pipeline.addLast("encoder", HttpResponseEncoder());
//
//                        pipeline.addLast("deflater", HttpContentCompressor());
//
//                        pipeline.addLast("handler", HttpServerHandler());
//                    }
//                })
//        val channel = bootstrap.bind(port).sync().channel()
//        channel.closeFuture().sync()
//    } finally {
//        bossGroup.shutdownGracefully()
//        workerGroup.shutdownGracefully()
//    }
}
