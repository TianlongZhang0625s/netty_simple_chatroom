package org.example.netty.chat.server.serverhandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.netty.chat.processor.MsgProcessor;
import org.example.netty.chat.protocol.IMassage;

public class SocketHandler extends SimpleChannelInboundHandler<IMassage> {


    private MsgProcessor msgProcessor = new MsgProcessor();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMassage msg) throws Exception {
        msgProcessor.sendMsg(ctx.channel(),msg);
    }


    public void handlerAdded (ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端Handler 创建······");
        super.handlerAdded(ctx);
    }

    public void handlerRemoved (ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        msgProcessor.logOut(client);
        System.out.println("Socket Client : " + msgProcessor.getNickName(client) + "离开！");
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        System.out.println("Socket Client: 有客户端连接:" + msgProcessor.getAddress(ctx.channel()));
    }

    public void channelInactive (ChannelHandlerContext ctx) throws Exception{
        System.out.println("channelInactive!");
        super.channelInactive(ctx);
    }

    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) throws Exception{
        System.out.println("Socket Client : 与客户端断开连接：" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }


}
