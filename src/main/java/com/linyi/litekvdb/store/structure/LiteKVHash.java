package com.linyi.litekvdb.store.structure;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.linyi.litekvdb.common.Constant.TYPE_HASH;

/**
 * @Author: linyi
 * @Date: 2025/5/13
 * @ClassName: LiteKVHash
 * @Version: 1.0
 * @Description: 哈希结构体
 */
public class LiteKVHash extends LiteKVObject{

    private Map<String, String> map = new HashMap<>();

    public LiteKVHash() {
        this.type = TYPE_HASH;
    }

    public void put(String field, String value) {
        map.put(field, value);
    }

    public String get(String field) {
        return map.get(field);
    }

    public Map<String, String> getAll() {
        return map;
    }

    public boolean remove(String field) {
        return map.remove(field) != null;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeByte(type);
            dos.writeLong(ttl);
            dos.writeInt(map.size());
            for (Map.Entry<String, String> e : map.entrySet()) {
                byte[] k = e.getKey().getBytes(StandardCharsets.UTF_8);
                byte[] v = e.getValue().getBytes(StandardCharsets.UTF_8);
                dos.writeInt(k.length);
                dos.write(k);
                dos.writeInt(v.length);
                dos.write(v);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deserialize(byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            this.type = dis.readByte();
            this.ttl = dis.readLong();
            int size = dis.readInt();
            map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                int klen = dis.readInt();
                byte[] kb = new byte[klen];
                dis.readFully(kb);
                String key = new String(kb, StandardCharsets.UTF_8);
                int vlen = dis.readInt();
                byte[] vb = new byte[vlen];
                dis.readFully(vb);
                String val = new String(vb, StandardCharsets.UTF_8);
                map.put(key, val);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
