package com.example.app

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.stream.ChunkedWriteHandler
import mu.KotlinLogging
import java.io.File

/**
 * Discards any incoming data.
 */
class NettyServer(private val host: String, private val port: Int) {
    private val log = KotlinLogging.logger {}
    fun run() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup() // (1)
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val b = ServerBootstrap() // (2)
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java) // (3)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(HttpServerCodec())
                            .addLast(ChunkedWriteHandler())
                            .addLast(HttpObjectAggregator(8192))
                            .addLast(WebSocketServerProtocolHandler("/" + "frpc-mananger"))
                            .addLast(
                                LoggingHandler(),
                                ServerHandler()
                            )
                    }
                })

            // Bind and start to accept incoming connections.
            b.bind(host, port).addListener {
                if (it.isSuccess) {
                    println("server started")
                } else {
                    println("server start failed")
                    workerGroup.shutdownGracefully()
                    bossGroup.shutdownGracefully()
                }
            }
        } catch (e: Exception) {
            log.debug { "server start failed" }
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}

class ServerHandler : SimpleChannelInboundHandler<TextWebSocketFrame>() {
    private val log = KotlinLogging.logger {}

    @Throws(java.lang.Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is ServerHandshakeStateEvent
            && evt == ServerHandshakeStateEvent.HANDSHAKE_COMPLETE
        ) {
            // Perform actions after WebSocket handshake is completed
            println("WebSocket handshake completed: " + ctx.channel().remoteAddress())

            // You can add your custom logic here
            // For example, send a welcome message to the connected client
            ctx.channel().writeAndFlush(TextWebSocketFrame("Welcome to the WebSocket server!"))
        }
        super.userEventTriggered(ctx, evt)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info { "connection in: ${ctx.channel().remoteAddress()}" }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame) {
        log.info { "server read: ${msg.text()}" }
        val text = msg.text()

        if(text == "frpc-process-log") {
            val file = File("startfrp.log")
            if(!file.exists())
            ctx.channel().writeAndFlush(TextWebSocketFrame("no log"))

            else {
                val content = file.readText()
                ctx.channel().writeAndFlush(TextWebSocketFrame(content))
            }
        }
    }
}
