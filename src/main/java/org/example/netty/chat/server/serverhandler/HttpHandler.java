package org.example.netty.chat.server.serverhandler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private URL baseUrl = HttpHandler.class.getProtectionDomain().getCodeSource().getLocation();

    private final String webroot = "webroot";

    // 获取文件资源
    private File getResources (String fileName) throws Exception{
        String path = baseUrl.toURI() + webroot + "/" + fileName;
        path = !path.contains("file:") ? path : path.substring(5);
        path = path.replaceAll("//", "/");
        return new File(path);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        RandomAccessFile file = null;
        try {
            String pageSource = uri.equals("/") ? "chat.html" : uri;
//            File tremp = getResources(pageSource);
            file = new RandomAccessFile(getResources(pageSource), "r");

        }catch (Exception e){
            ctx.fireChannelRead(request.retain());
            return;
        }

        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(),
                HttpResponseStatus.OK);
        String contextType = "text/html;";
        if (uri.endsWith(".css")){
            contextType = "text/css;";
        }else if (uri.endsWith(".js")){
            contextType = "text/javascript;";
        }else if (uri.toLowerCase().matches("(jpg|png|gif)$")){
            String ext = uri.substring(uri.lastIndexOf("."));
            contextType = "image/" + ext;
        }

        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,contextType + "charset=utf-8;");

        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        if (keepAlive){
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,file.length());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.write(response);
        ctx.write(new DefaultFileRegion(file.getChannel(),0,file.length()));

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (!keepAlive){
            future.addListener(ChannelFutureListener.CLOSE);
        }

        file.close();

    }


    public void exceptionCaught (ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception{
        Channel client = channelHandlerContext.channel();
        System.out.println("client: " + client.remoteAddress() + "异常");
        cause.printStackTrace();
        channelHandlerContext.close();
    }
}
