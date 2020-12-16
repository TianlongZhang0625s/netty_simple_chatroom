package org.example.netty.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.example.netty.chat.protocol.IMDecoder;
import org.example.netty.chat.protocol.IMEncoder;
import org.example.netty.chat.server.serverhandler.HttpHandler;
import org.example.netty.chat.server.serverhandler.SocketHandler;
import org.example.netty.chat.server.serverhandler.WebSocketHandler;

public class IMChatServer {
    private int port = 8080;
    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).
                    channel(NioServerSocketChannel.class).
                    option(ChannelOption.SO_BACKLOG,1024).
                    childHandler(new ChannelInitializer<SocketChannel>(){

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            // 解析自定义协议
                            channelPipeline.addLast(new IMDecoder());
                            channelPipeline.addLast(new IMEncoder());
                            channelPipeline.addLast(new SocketHandler());

                            // 解析http请求
                            channelPipeline.addLast(new HttpServerCodec());
                            channelPipeline.addLast(new HttpObjectAggregator(64 * 1024));

                            channelPipeline.addLast(new ChunkedWriteHandler());
                             channelPipeline.addLast(new HttpHandler());

                            channelPipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            channelPipeline.addLast(new WebSocketHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(this.port).sync();
            System.out.println("服务已启动，监听端口：" + this.port);
            future.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new IMChatServer().start();
    }
}
