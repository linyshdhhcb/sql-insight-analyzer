package com.linyi.litekvdb.store.structure;

import java.nio.charset.StandardCharsets;

import static com.linyi.litekvdb.common.Constant.TYPE_STRING;

/**
 * @Author: linyi
 * @Date: 2025/5/10
 * @ClassName: LiteKVString
 * @Version: 1.0
 * @Description: 字符串结构体
 */
public class LiteKVString extends LiteKVObject {

    // 字符串最大长度
    private static final int MAX_SHORT_LEN = 12;

    // 值
    private Object value;

    public LiteKVString() {
        this.type = TYPE_STRING;
    }

    public LiteKVString(String value) {
        this();
        setValue(value);
    }

    public LiteKVString(String value, String ttl){
        this();
        setValue(value,ttl);
    }

    /**
     * 设置值，并根据长度或是否为整数进行优化存储
     */
    public void setValue(String val) {
        if (val.length() <= MAX_SHORT_LEN) {
            //  短字符串
            this.value = val.getBytes(StandardCharsets.UTF_8);
            this.type = TYPE_STRING;
            this.ttl = -1;
        } else if (isInteger(val)) {
            this.value = Long.parseLong(val);
        } else {
            this.value = val.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 设置值，并根据长度或是否为整数进行优化存储
     * @param val 值
     * @param ttl 过期时间
     */
    public void setValue(String val, String ttl) {
        if (val.length() <= MAX_SHORT_LEN) {
            //  短字符串
            this.value = val.getBytes(StandardCharsets.UTF_8);
            this.type = TYPE_STRING;
            this.ttl = Long.parseLong(ttl)*1000;
        } else if (isInteger(val)) {
            this.value = Long.parseLong(val)*1000;
        } else{
            this.value = val.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String getValue() {
        if (isExpired()){
            return null;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        } else if (value instanceof Long) {
            return value.toString();
        }
        return null;
    }

    private boolean isInteger(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public byte[] serialize() {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            byte[] res = new byte[bytes.length + 2];
            res[0] = '$';
            res[1] = (byte) bytes.length;
            System.arraycopy(bytes, 0, res, 2, bytes.length);
            return res;
        } else if (value instanceof Long) {
            return ("+int:" + value).getBytes(StandardCharsets.UTF_8);
        }
        return "+null".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void deserialize(byte[] data) {
        if (data.length > 1 && data[0] == '$') {
            int len = data[1];
            byte[] b = new byte[len];
            System.arraycopy(data, 2, b, 0, len);
            this.value = b;
        } else {
            this.value = new String(data, StandardCharsets.UTF_8);
        }
    }

    /**
     * 设置过期时间（毫秒）
     */
    public void setTtl(long ms) {
        this.ttl = System.currentTimeMillis() + ms;
    }
}