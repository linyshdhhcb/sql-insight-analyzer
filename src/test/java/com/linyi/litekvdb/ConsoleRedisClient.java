package com.linyi.litekvdb;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleRedisClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 6381;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PORT);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("已连接到 LiteKV-DB，请输入命令（如 SET name \"value\"），输入 quit 退出");

            while (true) {
                System.out.print("127.0.0.1> ");
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("quit")) {
                    break;
                }

                if (line.isEmpty()) continue;

                // 构造 RESP 命令
                String[] parts = line.split(" ");
                StringBuilder resp = new StringBuilder();
                resp.append("*").append(parts.length).append("\r\n");

                for (String part : parts) {
                    String value = part.startsWith("\"") && part.endsWith("\"")
                            ? part.substring(1, part.length() - 1)
                            : part;

                    resp.append("$").append(value.length()).append("\r\n");
                    resp.append(value).append("\r\n");
                }

                // 发送命令
                out.write(resp.toString().getBytes());
                out.flush();

                // 接收响应
                byte[] buffer = new byte[1024];
                int len = in.read(buffer);
                if (len > 0) {
                    System.out.println("Response: " + new String(buffer, 0, len));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}