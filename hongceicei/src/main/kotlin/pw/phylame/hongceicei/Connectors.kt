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
import java.io.Closeable
import java.io.PrintStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletResponse

interface Connector : Closeable {
    fun bind(host: String, port: Int)

    var dispatcher: HttpDispatcher
}

abstract class AbstractConnector(maxThreadCount: Int) : Connector {
    protected val threadPool = Executors.newFixedThreadPool(maxThreadCount)

    override lateinit var dispatcher: HttpDispatcher
}

class LegacyConnector(maxThreadCount: Int) : AbstractConnector(maxThreadCount) {
    private var closed = false

    override fun bind(host: String, port: Int) {
        val server = ServerSocket()
        server.bind(InetSocketAddress(host, port))
        while (!closed) {
            val client = server.accept()
            threadPool.submit {
                processSocket(client)
            }
        }
        threadPool.shutdown()
    }

    override fun close() {
        closed = true
    }

    fun processSocket(socket: Socket) {
        val request = HttpServletRequestImpl.forSocket(socket)
        val response = HttpServletResponseImpl.forSocket(socket)
        dispatcher.handleHttp(request, response)
        socket.shutdownInput()
        writeToSocket(response, socket)
        socket.shutdownOutput()
    }

    fun writeToSocket(response: HttpServletResponse, socket: Socket) {
        val out = socket.outputStream.buffered()
        PrintStream(out).apply {
            println("HTTP/1.1 ${response.status} OK")
            println("Date: ${Date().gmtString()}")
            println("Server: Hongceice")
            println("Content-Length: 0")
            response.headerNames.forEach {
                println("$it: ${response.getHeader(it)}")
            }
            println()
        }.flush()
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
