package com.linyi.litekvdb.store.utils;

public class RamUsageEstimator {
    public static long sizeOf(String s) {
        return 2L * s.length(); // 简化为 UTF-16 字节数
    }
}