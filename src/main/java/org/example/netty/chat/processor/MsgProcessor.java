package org.example.netty.chat.processor;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.example.netty.chat.protocol.IMDecoder;
import org.example.netty.chat.protocol.IMEncoder;
import org.example.netty.chat.protocol.IMP;
import org.example.netty.chat.protocol.IMassage;
import com.alibaba.fastjson.JSONObject;

public class MsgProcessor {

    // 记录在线用户
    private static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipaddr");
    private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");

    // 自定义解码器
    private IMDecoder imDecoder = new IMDecoder();
    private IMEncoder imEncoder = new IMEncoder();

    public String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }

    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    public JSONObject getAttrs(Channel client) {
        try {
            return client.attr(ATTRS).get();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setAttrs(Channel client, String key, Object value) {
        try {
            JSONObject jsonObject = client.attr(ATTRS).get();
            jsonObject.put(key, value);
            client.attr(ATTRS).set(jsonObject);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, value);
            client.attr(ATTRS).set(jsonObject);
        }
    }

    public void logOut(Channel client) {
        if (getNickName(client) == null) {
            return;
        }
        for (Channel channel : onlineUsers) {
            IMassage request = new IMassage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(),
                    getNickName(client) + "；离开！");
            String content = imEncoder.encode(request);
            channel.writeAndFlush(new TextWebSocketFrame(content));
        }
        onlineUsers.remove(client);
    }

    public void sendMsg(Channel client, IMassage msg) {
        sendMsg(client, imEncoder.encode(msg));
    }

    public void sendMsg(Channel client, String msg) {
        IMassage request = imDecoder.decode(msg);
        if (null == request) {
            return;
        }

        String addr = getAddress(client);

        if (request.getCmd().equals(IMP.LOGIN.getName())) {
            client.attr(NICK_NAME).getAndSet(request.getSender());
            client.attr(IP_ADDR).getAndSet(addr);
            onlineUsers.add(client);

            for (Channel channel : onlineUsers) {
                if (channel != client) {
                    request = new IMassage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), getNickName(client) + "加入！");
                } else {
                    request = new IMassage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), "已和服务器建立连接!");
                }
                String content = imEncoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        } else if (request.getCmd().equals(IMP.CHAT.getName())) {
            for (Channel channel : onlineUsers) {
                if (channel == client) {
                    request.setSender("you~");
                } else {
                    request.setSender(getNickName(client));
                }
                request.setTime(sysTime());
                String content = imEncoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        } else if (request.getCmd().equals(IMP.FLOWER.getName())) {
            JSONObject attrs = getAttrs(client);
            long currTime = sysTime();
            if (null != attrs) {
                long lastTime = attrs.getLongValue("lastFlowerTime");
                //60秒之内不允许重复刷鲜花
                int secends = 10;
                long sub = currTime - lastTime;
                if (sub < 1000 * secends) {
                    request.setSender("you~");
                    request.setCmd(IMP.SYSTEM.getName());
                    request.setContent("您送鲜花太频繁," + (secends - Math.round(sub / 1000)) + "秒后再试");
                    String content = imEncoder.encode(request);
                    client.writeAndFlush(new TextWebSocketFrame(content));
                    return;
                }
            }

            //正常送花
            for (Channel channel : onlineUsers) {
                if (channel == client) {
                    request.setSender("you~");
                    request.setContent("你给大家送了一波鲜花雨");
                    setAttrs(client, "lastFlowerTime", currTime);
                } else {
                    request.setSender(getNickName(client));
                    request.setContent(getNickName(client) + "送来一波鲜花雨");
                }
                request.setTime(sysTime());

                String content = imEncoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    private Long sysTime() {
        return System.currentTimeMillis();
    }

}
