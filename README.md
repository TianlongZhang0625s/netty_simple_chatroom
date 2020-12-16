# netty_simple_chatroom
a simple chat room implemented based on netty and MessagePack

一个基于netty实现的简易聊天室，序列化协议使用MessagePack
1. 自定义消息体，可支持自己的im协议
2. 实现了定制消息的序列化和反序列化
3. 可多个客户端同时登陆访问，在公共面板可看到全部消息。目前未支持历史消息查询。

### 实现效果
1. 访问绑定的服务端口，程序代码默认8080。
2. 进入聊天室，输入昵称
3. 开始聊天，可以同步看到群消息。