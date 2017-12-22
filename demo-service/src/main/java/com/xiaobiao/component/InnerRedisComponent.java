package com.xiaobiao.component;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.xiaobiao.constant.CmsConstantRedis;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wuxiaobiao
 * @create 2017-12-18 11:06
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Component
public class InnerRedisComponent {

//    @Autowired
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 在Redis中放入值
     *
     * @param key
     * @param value
     */
    public void setRedis(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, CmsConstantRedis.REDIS_DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 在Redis中放入值，并指定有效时间
     *
     * @param key
     * @param value
     * @param expire
     * @param timeUnit
     */
    public void setRedis(String key, Object value, long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value);
        if(expire>0) {
            redisTemplate.expire(key, expire, timeUnit);
        }
    }

    /**
     * 删除Redis中的数据
     * @param key
     */
    public void removeRedis(String key){
        redisTemplate.delete(key);
    }

    /**
     * 获取Redis中值的有效期
     * @param key
     */
    public Long getRedisExpire(String key){
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 从Redis中获取值
     *
     * @param key
     * @return
     */
    public Object getRedis(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 从Redis中获取值，并重置有效期
     * @param key
     * @return
     */
    public Object getRedisResetExpire(String key) {
        Object sessionObj = redisTemplate.opsForValue().get(key);
        if (sessionObj != null) {
            //重置有效时间
            redisTemplate.expire(key, CmsConstantRedis.SESSION_DEFAULT_EXPIRE, TimeUnit.SECONDS);
        }
        return sessionObj;
    }


    /**
     * 根据表达式，获取所有键的集合
     * @param pattern 表达式格式，如：key_*
     * @return
     */
    public Set<String> getRedisKeys(String pattern) {
        //创建一个线程安全的Set，由ConcurrentHashMap的实例支持，因此进行了相同的并发性担保，与HashSet不同的是，这个Set不允许null元素，该Set是可序列化的。
        Set<String> keys = Sets.newConcurrentHashSet();
        Set<byte[]> byteKeys = redisTemplate.getConnectionFactory().getConnection().keys(pattern.getBytes());
        Iterator<byte[]> it = byteKeys.iterator();
        while (it.hasNext()) {
            byte[] data = it.next();
            keys.add(new String(data, 0, data.length));
        }
        return keys;
    }

    /**
     * 一次获取多个值
     * @param keys
     * @return
     */
    public Collection<Object> getRedisMulti(String... keys) {
        if (keys == null && keys.length == 0) {
            Collections.emptySet();
        }
        Collection<Object> collection = new ArrayList<Object>();
        for(int i = 0;i<keys.length;i++){
            collection.add(keys[i]);
        }
        return redisTemplate.opsForValue().multiGet(collection);
    }


    /**
     * 生成Redis中使用存放session的key 存放SessionId
     * @param sessionId
     * @return
     */
    private String genRedisSessionKey(String sessionId){
        String redisKey = CmsConstantRedis.REDIS_SESSION + sessionId;
        return redisKey;
    }


    /**
     * 在Redis的Session对象中赋值
     *
     * @param sessionId
     * @param sessionKey
     * @param sessionValue
     */
    public void setRedisSession(String sessionId, String sessionKey, Object sessionValue) {
        String redisKey = genRedisSessionKey(sessionId);
        Object sessionObj = getRedis(redisKey);
        Map<String, Object> sessionMap = new ConcurrentHashMap<String, Object>();
        if (sessionObj != null) {
            sessionMap = (ConcurrentHashMap) sessionObj;
        }
        sessionMap.put(sessionKey, sessionValue);
        //把用户的Session放入Redis,并设置有效时间
        setRedis(redisKey, sessionMap, CmsConstantRedis.SESSION_DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }


    /**
     * 获取Redis中的Session对象
     *
     * @param sessionId
     * @return Map ConcurrentHashMap
     */
    private Map getRedisSession(String sessionId) {
        String redisKey = genRedisSessionKey(sessionId);
        Object sessionObj = getRedis(redisKey);
        Map<String, Object> sessionMap = null;
        if (sessionObj != null) {
            //每次读取Session重置有效时间
            redisTemplate.expire(redisKey, CmsConstantRedis.SESSION_DEFAULT_EXPIRE, TimeUnit.SECONDS);
            sessionMap = (ConcurrentHashMap) sessionObj;
        }
        return sessionMap;
    }


    /**
     * 获取Redis中Session对象的值
     *
     * @param sessionId Session的ID
     * @param sessionKey Session中存放对应值得键
     * @return Map ConcurrentHashMap
     */
    public Object getRedisSessionValue(String sessionId, String sessionKey) {
        Map<String, Object> sessionMap = getRedisSession(sessionId);
        if (sessionMap != null) {
            return sessionMap.get(sessionKey);
        }
        return null;
    }

    /**
     * 移除Session中某个键值对
     *
     * @param sessionId
     * @param sessionKey
     */
    public void removeRedisSession(String sessionId, String sessionKey){
        String redisKey = genRedisSessionKey(sessionId);
        Map sessionMap = getRedisSession(sessionId);
        if (sessionMap != null) {
            sessionMap.remove(sessionKey);
            setRedis(redisKey, sessionMap, CmsConstantRedis.SESSION_DEFAULT_EXPIRE, TimeUnit.SECONDS);
        }
    }


    /**
     * 根据Key获取已经保存到Redis中所有的值,Session级别
     *
     * @param key 键
     * @return map数据
     */
    public Map<String, String> getAllMap(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        Set<Object> keys = redisTemplate.keys("*" + key);
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        if (keys != null && keys.size() > 0) {
            for (Object hashKey : keys) {
                String text = getValue(String.valueOf(hashKey), key);
                map.put(String.valueOf(hashKey), text);
            }
        }
        return map;
    }


    /**
     * 根据Key和HashKey获取一个值，返回String
     *
     * @param key     键
     * @param hashKey has键
     * @return String 缓存数据
     */
    public String getValue(String key, String hashKey) {
        Object obj = redisTemplate.opsForHash().get(key, hashKey);
        return obj == null ? null : obj.toString();
    }

    public Map<String, String> getHashMap(String hashKey) {
        if (Strings.isNullOrEmpty(hashKey)) {
            return new HashMap();
        }
        return  redisTemplate.boundHashOps(hashKey).entries();
    }
}
