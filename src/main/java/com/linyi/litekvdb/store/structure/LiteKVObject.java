package com.linyi.litekvdb.store.structure;

import java.io.Serializable;

/**
 * @Author: linyi
 * @Date: 2025/5/10
 * @ClassName: LiteKVObject
 * @Version: 1.0
 * @Description: 数据类型抽象类
 */
public abstract class LiteKVObject implements Serializable {


    /**
     * 键值对类型
     */
    protected byte type;

    /**
     * 过期时间（-1是永不过期）
     */
    protected long ttl;

    /**
     * 判断是否过期
     * @return true/false
     */
    public boolean isExpired(){
        return ttl != -1 && System.currentTimeMillis() > ttl;
    }

    /**
     * 获取类型
     * @return byte
     */
    public byte getType() {
        return type;
    }

    /**
     * 序列化
     * @return byte[]
     */
    public abstract byte[] serialize();

    /**
     * 反序列化
     * @param data
     */
    public abstract void deserialize(byte[] data);
}
