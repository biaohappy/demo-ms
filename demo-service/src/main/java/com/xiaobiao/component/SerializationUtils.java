package com.xiaobiao.component;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;

import java.io.Serializable;

/**
 * @author wuxiaobiao
 * @create 2017-12-18 11:06
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
public class SerializationUtils {
    /**
     * 使用 sessionId 创建字符串的 key，用来在 Redis 里作为存储 Session 的 key.
     * @param prefix
     * @param sessionId
     * @return
     */
    public static String sessionKey(String prefix, Serializable sessionId) {
        return prefix + sessionId;
    }

    /**
     * 使用 session 创建字符串的 key，用来在 Redis 里作为存储 Session 的 key.
     * @param prefix
     * @param session
     * @return
     */
    public static String sessionKey(String prefix, Session session) {
        return prefix + session.getId();
    }

    /**
     * 把 session 序列化为 string，因为 Redis 的 key 和 value 必须同时为 string 或者 byte[].
     * @param session
     * @return
     */
    public static String sessionToString(Session session) {
        byte[] content = org.apache.commons.lang3.SerializationUtils.serialize((SimpleSession) session);
        return Base64.encodeToString(content);
        //byte[] content = org.apache.shiro.codec.Base64.decode(value);
        //return org.apache.commons.lang3.SerializationUtils.deserialize(content);
    }

    /**
     * 反序列化得到 session.
     * @param value
     * @return
     */
    public static Session sessionFromString(String value) {
        //byte[] content = org.apache.commons.lang3.SerializationUtils.serialize((SimpleSession) session);
        //return org.apache.shiro.codec.Base64.encodeToString(content);
        byte[] content = org.apache.shiro.codec.Base64.decode(value);
        return (Session)org.apache.commons.lang3.SerializationUtils.deserialize(content);
    }
}
