package com.linyi.litekvdb.core.network;

import com.linyi.litekvdb.core.command.CommandExecutor;
import com.linyi.litekvdb.core.protocol.RespDecoder;
import com.linyi.litekvdb.store.Database;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Reactor {
    private Selector selector;
    private final Database storage;

    public Reactor(Database storage) { // 新增构造函数 ✅
        this.storage = storage;
    }

    public void start(int port) throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("LiteKV-DB 已启动，监听端口：" + port);

        while (true) {
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

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);

        if (read == -1) {
            clientChannel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String request = new String(bytes);

        String[] args = RespDecoder.decode(request);
        if (args != null && args.length > 0) {
            // 使用注入的 storage
            String response = new CommandExecutor(this.storage).execute(args);
            clientChannel.write(ByteBuffer.wrap(response.getBytes()));
        }
    }
}