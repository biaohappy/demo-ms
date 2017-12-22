package com.xiaobiao.component;

import com.xiaobiao.constant.CmsConstantRedis;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author wuxiaobiao
 * @create 2017-12-18 11:06
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Component
@Slf4j
public class ShiroRedisSessionDao extends EnterpriseCacheSessionDAO {

    private String keyPrefix = CmsConstantRedis.SHIRO_SESSION; //用于存放CMS中shiro对应SESSION的KEY前缀
    private long timeToLiveSeconds = CmsConstantRedis.SESSION_DEFAULT_EXPIRE; // Redis中存放Session的有效时间，单位: 秒
    private String authenticatedKey = CmsConstantRedis.SHIRO_DEFAULT_AUTHENTICATED_KEY; //Shiro是否登陆的KEY

    @Autowired
    private InnerRedisComponent innerRedisComponent;

    /**
     * DefaultSessionManager 创建完 session 后会调用该方法。
     * 把 session 保持到 Redis。
     * 返回 Session ID；主要此处返回的 ID.equals(session.getId())
     */
    @Override
    public Serializable doCreate(Session session) {
        log.debug("=> Create session with ID [{}]", session.getId());
        // 创建一个Id并设置给Session
        Serializable sessionId = this.generateSessionId(session);
        assignSessionId(session, sessionId);
        // session 由 Redis 缓存失效决定
        String key = SerializationUtils.sessionKey(keyPrefix, session);
        String value = SerializationUtils.sessionToString(session);
        innerRedisComponent.setRedis(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
        return sessionId;
    }

    /**
     * 决定从本地 Cache 还是从 Redis 读取 Session.
     *
     * @param sessionId
     * @return
     * @throws UnknownSessionException
     */
    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        log.debug("read Session from ehcache!sessionId:{}",sessionId);
        //先获取缓存中的Session
        Session s = getCachedSession(sessionId);
        //如果缓存为空，或缓存的会话中不存在authenticatedKey为true（登陆成功时的标记），则从Redis中读取
        if (s == null || (s.getAttribute(authenticatedKey) != null && !(Boolean) s.getAttribute(authenticatedKey))) {
            log.debug("no session info in ehcache,now get it from redis!");
            s = doReadSession(sessionId);
        }
        if (s == null) {
            log.error("未能获取到session!sessionId:{}",sessionId);
            throw new UnknownSessionException("There is no session with id [" + sessionId + "]");
        }
        return s;
    }

    /**
     * 从 Redis 上读取 session，并缓存到本地 Cache.
     *
     * @param sessionId
     * @return
     */
    @Override
    protected Session doReadSession(Serializable sessionId) {
        log.debug("=> Read session from redis with ID [{}]", sessionId);
        //重置ShiroSession有效期
        Object value = innerRedisComponent.getRedisResetExpire(SerializationUtils.sessionKey(keyPrefix, sessionId));
        //重置RedisSession有效期
        innerRedisComponent.getRedisResetExpire(SerializationUtils.sessionKey(CmsConstantRedis.REDIS_SESSION, sessionId));
        // 例如 Redis 调用 flushdb 清空了所有的数据，读到的 session 就是空的
        if (value != null) {
            log.debug("session is cached by redis!now load it to ehcache!");
            Session session = SerializationUtils.sessionFromString(value.toString());
            super.cache(session, session.getId());
            return session;
        }
        return null;
    }

    /**
     * 更新 session 到 Redis.
     *
     * @param session
     */
    @Override
    public void doUpdate(Session session) {
        // 如果会话过期/停止，没必要再更新了
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            log.debug("=> Invalid session.");
            return;
        }
        log.debug("=> Update session with ID [{}]", session.getId());
        if(session instanceof SimpleShiroRedisSession){
            SimpleShiroRedisSession srs = (SimpleShiroRedisSession) session;
            if(!srs.isChanged()){
                return;
            }
            ((SimpleShiroRedisSession) session).setChanged(false);
        }
        String key = SerializationUtils.sessionKey(keyPrefix, session);
        String value = SerializationUtils.sessionToString(session);
        innerRedisComponent.setRedis(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
    }

    /**
     * 从 Redis 删除 session，并且发布消息通知其它 Server 上的 Cache 删除 session.
     *
     * @param session
     */
    @Override
    public void doDelete(Session session) {
        log.debug("remove session！sessionId:{}",session.getId());
        innerRedisComponent.removeRedis(SerializationUtils.sessionKey(keyPrefix, session));
    }

    /**
     * 取得所有有效的 session.
     *
     * @return
     */
    @Override
    public Collection<Session> getActiveSessions() {
        log.debug("=> Get active sessions");
        Set<String> keys = innerRedisComponent.getRedisKeys(CmsConstantRedis.SHIRO_SESSION + "*");
        Collection<Object> values = innerRedisComponent.getRedisMulti(keys.toArray(new String[0]));
        List<Session> sessions = new LinkedList<Session>();
        for (Object value : values) {
            Session valueSession = SerializationUtils.sessionFromString(value.toString());
            if (valueSession.getAttribute(authenticatedKey) != null && (Boolean) valueSession.getAttribute(authenticatedKey)) {
                sessions.add(valueSession);
            }
        }
        return sessions;
    }

}
