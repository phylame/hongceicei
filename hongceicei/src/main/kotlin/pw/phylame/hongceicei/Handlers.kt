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

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder

class HttpServerHandler : SimpleChannelInboundHandler<HttpObject>() {
    private var decoder: HttpPostRequestDecoder? = null
    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        messageReceived(ctx, msg);
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        decoder?.cleanFiles()
    }

    fun messageReceived(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is HttpRequest) {

        }
    }

}
