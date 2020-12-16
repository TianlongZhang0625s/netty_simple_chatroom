package org.example.netty.chat.client.clienthandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.Scanner;

import org.example.netty.chat.protocol.IMP;
import org.example.netty.chat.protocol.IMassage;


/**
 * 聊天客户端逻辑实现
 * @author Tom
 *
 */
public class ChatClientHandler extends ChannelInboundHandlerAdapter{

    private ChannelHandlerContext ctx;
    private String nickName;
    public ChatClientHandler(String nickName){
        this.nickName = nickName;
    }

    /**启动客户端控制台*/
    private void session() throws IOException {
        new Thread(){
            public void run(){
                System.out.println(nickName + ",你好，请在控制台输入消息内容");
                IMassage message = null;
                Scanner scanner = new Scanner(System.in);
                do{
                    if(scanner.hasNext()){
                        String input = scanner.nextLine();
                        if("exit".equals(input)){
                            message = new IMassage(IMP.LOGOUT.getName(),System.currentTimeMillis(),nickName);
                        }else{
                            message = new IMassage(IMP.CHAT.getName(),System.currentTimeMillis(),nickName,input);
                        }
                    }
                }
                while (sendMsg(message));
                scanner.close();
            }
        }.start();
    }

    /**
     * tcp链路建立成功后调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        IMassage message = new IMassage(IMP.LOGIN.getName(),System.currentTimeMillis(),this.nickName);
        sendMsg(message);
        System.out.println("成功连接服务器,已执行登录动作");
        session();
    }
    /**
     * 发送消息
     * @param msg
     * @return
     * @throws IOException
     */
    private boolean sendMsg(IMassage msg){
        ctx.channel().writeAndFlush(msg);
        System.out.println("已发送至聊天面板,请继续输入");
        return msg.getCmd().equals(IMP.LOGOUT) ? false : true;
    }
    /**
     * 收到消息后调用
     * @throws IOException
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        IMassage m = (IMassage)msg;
        System.out.println(m);
    }
    /**
     * 发生异常时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("与服务器断开连接:"+cause.getMessage());
        ctx.close();
    }
}
