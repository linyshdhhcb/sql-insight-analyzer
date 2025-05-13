package com.linyi.litekvdb.core.network;

import com.linyi.litekvdb.core.command.CommandExecutor;
import com.linyi.litekvdb.core.protocol.RespDecoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: Reactor
 * @Version: 1.0
 * @Description: 一个基于NIO的Reactor网络模型，用于处理客户端请求
 */
public class Reactor {
    private final Selector selector;
    private final CommandExecutor executor;

    public Reactor(CommandExecutor executor) throws IOException {
        this.selector = Selector.open();
        this.executor = executor;
    }

    /**
     * 启动服务器
     * @param port 端口号
     * @throws IOException
     */
    public void start(int port) throws IOException {
        // 打开服务器的 SocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        // 非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 注册到 selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("LiteKV-DB 正在启动，监听端口：" + port);

        // 事件循环
        while (true) {
            // 阻塞，等待事件
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    accept(key);
                }

                if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    /**
     * 接受客户端连接
     * @param key SelectionKey
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        // 注册为可读事件
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * 处理客户端请求
     * @param key SelectionKey
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);

        if (read == -1) {
            // 客户端断开连接
            clientChannel.close();
            return;
        }

        // 切换到读取模式
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // 获取请求的字符串
        String request = new String(bytes);

        // 解析命令
        String[] args = RespDecoder.decode(request);
        if (args != null && args.length > 0) {
            // 使用 CommandExecutor 执行命令
            String response = executor.executeCommand(args[0], args[1], args);
            // 将结果写回客户端
            clientChannel.write(ByteBuffer.wrap(response.getBytes()));
        }
    }
}