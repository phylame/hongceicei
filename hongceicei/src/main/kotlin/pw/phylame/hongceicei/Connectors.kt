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

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.servlet.http.Cookie

interface Connector : Closeable {
    fun bind(host: String, port: Int)

    var dispatcher: HttpDispatcher
}

abstract class AbstractConnector(maxThreadCount: Int) : Connector {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(LegacyConnector::class.java)
    }

    protected val threadPool: ExecutorService = Executors.newFixedThreadPool(maxThreadCount)

    override lateinit var dispatcher: HttpDispatcher

    init {
        logger.debug("use fixed thread pool with size: $maxThreadCount")
    }
}

class LegacyConnector(maxThreadCount: Int, val debug: Boolean = false) : AbstractConnector(maxThreadCount) {
    private var closed = false

    override fun bind(host: String, port: Int) {
        val server = ServerSocket()
        server.bind(InetSocketAddress(host, port))
        logger.debug("${javaClass.simpleName} bound $host:$port")
        while (!closed) {
            val client = server.accept()
            if (!debug) {
                threadPool.submit {
                    handleSocket(client)
                    client.close()
                }
            } else {
                handleSocket(client)
                client.close()
            }
        }
        logger.debug("end bind and shutdown thread pool")
        threadPool.shutdown()
    }

    override fun close() {
        closed = true
    }

    fun handleSocket(socket: Socket) {
        val request = HttpServletRequestImpl().apply { parseSocket(socket) }
        val response = HttpServletResponseImpl.forSocket(socket)
        dispatcher.handleHttp(request, response)
        socket.shutdownInput()
        writeToSocket(response, socket)
        socket.shutdownOutput()
    }

    fun writeToSocket(response: HttpServletResponseImpl, socket: Socket) {
        PrintStream(socket.outputStream.buffered()).apply {
            println("HTTP/1.1 ${response.status} OK")
            if ("Data" !in response.headers) {
                println("Date: ${Date().gmtString()}")
            }
            if ("Server" !in response.headers) {
                println("Server: Hongceice")
            }
            response.cookies.forEach {
                println("Set-Cookie: ${renderCookie(it)}")
            }
            response.headers.forEach { name, values ->
                values?.forEach {
                    println("$name: $it")
                }
            }
            println()
            flush()
        }
    }

    fun renderCookie(cookie: Cookie): String {
        return cookie.toString()
    }
}

class NettyConnector(maxThreadCount: Int) : AbstractConnector(maxThreadCount) {
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup

    override fun bind(host: String, port: Int) {
        bossGroup = NioEventLoopGroup()
        workerGroup = NioEventLoopGroup()
        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast(HttpServerHandler())
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
            println("bind host=$host, port=$port")
            b.bind(host, port).sync().channel().closeFuture().sync()
        } finally {
            close()
        }
    }

    override fun close() {
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }

    inner class HttpServerHandler : SimpleChannelInboundHandler<HttpObject>() {
        private var decoder: HttpPostRequestDecoder? = null
        override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
            messageReceived(ctx, msg)
        }

        override fun channelInactive(ctx: ChannelHandlerContext?) {
            decoder?.cleanFiles()
            println("ctx = [${ctx}]")
        }

        fun messageReceived(ctx: ChannelHandlerContext, msg: HttpObject) {
            println("ctx = [${ctx}], msg = [${msg}]")
            if (msg is HttpRequest) {

            }
        }

    }

}
