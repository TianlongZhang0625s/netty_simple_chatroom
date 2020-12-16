package org.example.netty.chat.server.serverhandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.example.netty.chat.processor.MsgProcessor;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private MsgProcessor msgProcessor = new MsgProcessor();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        msgProcessor.sendMsg(ctx.channel(),msg.text());
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
        Channel client = ctx.channel();
        String addr = msgProcessor.getAddress(client);
        System.out.println("WebSocket client : " + addr + "加入~");
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
        Channel client = ctx.channel();
        msgProcessor.logOut(client);
        System.out.println("WebSocket client : " + msgProcessor.getNickName(client) + "离开~");
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        Channel client = ctx.channel();
        String addr = msgProcessor.getAddress(client);
        System.out.println("WebSocket client : " + addr + "上线~");
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        String addr = msgProcessor.getAddress(client);
        System.out.println("WebSocket client : " + addr + "掉线~");
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        Channel client = ctx.channel();
        String addr = msgProcessor.getAddress(client);
        System.out.println("WebSocket Client : " + addr + "异常~");
        cause.printStackTrace();
        ctx.close();
    }
}
