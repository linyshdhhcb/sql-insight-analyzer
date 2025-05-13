package com.linyi.litekvdb.core.protocol;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: RespDecoder
 * @Version: 1.0
 * @Description: RESP 解码器
 */
public class RespDecoder {
    public static String[] decode(String input) {
        if (input == null || input.isEmpty() || !input.startsWith("*")) {
            return null;
        }

        String[] lines = input.split("\r\n");
        if (lines.length < 1) return null;

        int argc;
        try {
            argc = Integer.parseInt(lines[0].substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        String[] args = new String[argc];
        int argIndex = 0;

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].startsWith("$")) {
                i++;
                if (i < lines.length) {
                    args[argIndex++] = lines[i];
                }
            }
        }

        return args.length == argc ? args : null;
    }
}