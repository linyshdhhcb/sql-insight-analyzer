package com.linyi.litekvdb.store.structure;

import java.nio.charset.StandardCharsets;

public class LiteKVString extends LiteKVObject {
    private static final int MAX_SHORT_STR_LEN = 12;
    private Object value;

    public LiteKVString() {
        this.type = TYPE_STRING;
        this.ttl = -1;
    }

    public LiteKVString(String value) {
        this();
        setValue(value);
    }

    public void setValue(String value) {
        if (value.length() <= MAX_SHORT_STR_LEN) {
            this.value = value.getBytes(StandardCharsets.UTF_8);
        } else if (isInteger(value)) {
            this.value = Long.parseLong(value);
        } else {
            this.value = compress(value);
        }
    }

    public String getValue() {
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        } else if (value instanceof Long) {
            return String.valueOf(value);
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

    private byte[] compress(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] serialize() {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            byte[] result = new byte[bytes.length + 2];
            result[0] = '$';
            result[1] = (byte) bytes.length;
            System.arraycopy(bytes, 0, result, 2, bytes.length);
            return result;
        } else if (value instanceof Long) {
            return ("+int:" + value).getBytes(StandardCharsets.UTF_8);
        }
        return "+null".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void deserialize(byte[] data) {
        if (data.length > 0 && data[0] == '$') {
            int len = data[1];
            byte[] strBytes = new byte[len];
            System.arraycopy(data, 2, strBytes, 0, len);
            this.value = strBytes;
        } else {
            this.value = new String(data, StandardCharsets.UTF_8);
        }
    }

    public void setTtl(long expireMs) {
        this.ttl = System.currentTimeMillis() + expireMs;
    }
}
