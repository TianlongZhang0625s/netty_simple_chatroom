package org.example.netty.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.netty.chat.client.clienthandler.ChatClientHandler;
import org.example.netty.chat.protocol.IMDecoder;

public class ChatClient {

    private ChatClientHandler clientHandler;
    private String host;
    private int port;

    public ChatClient(String nickName){
        this.clientHandler = new ChatClientHandler(nickName);
    }

    public void connect (String host, int port){
        this.host = host;
        this.port = port;
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new IMDecoder());
                    ch.pipeline().addLast(new IMDecoder());
                    ch.pipeline().addLast(clientHandler);
                }
            });
            ChannelFuture future = bootstrap.connect(this.host,this.port).sync();
            future.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient("HelloKitty").connect("127.0.0.1",8080);
        new ChatClient("HelloKitty1").connect("127.0.0.1",8080);

    }
}
