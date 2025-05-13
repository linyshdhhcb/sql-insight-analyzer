package com.linyi.litekvdb.core.protocol;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: RespDecoder
 * @Version: 1.0
 * @Description: RESP 解码器
 */
public class RespDecoder {

    /**
     * 解码 RESP 命令
     * @param input 输入的字符串
     * @return 解码后的命令数组
     */
    public static String[] decode(String input) {
        // 检查输入是否为空或格式不正确
        if (input == null || input.isEmpty() || !input.startsWith("*")) {
            return null;
        }

        // 按行分割输入字符串
        String[] lines = input.split("\r\n");
        // 检查行数是否正确
        if (lines.length < 1) return null;

        //  获取参数个数
        int argc;
        try {
            argc = Integer.parseInt(lines[0].substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        // 遍历每一行，解析参数
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

        //  检查参数个数是否正确
        return args.length == argc ? args : null;
    }
}