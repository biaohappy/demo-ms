package com.xiaobiao.constant;

/**
 * Created with IntelliJ IDEA.
 * User: LeonWong
 * Date: 2015-12-10
 * Time: 9:25
 * To change this template use File | Settings | Editor | File and Code Templates.
 */
public class CmsConstantRedis {
    //Redis默认过期时间（单位：秒）
    public static final long REDIS_DEFAULT_EXPIRE = 365 * 24 * 60 * 60;
    //Session默认过期时间（单位：秒）
    public static final long SESSION_DEFAULT_EXPIRE =365 *  24 * 60 * 60;


    //Redis中存放RedisSession的名称前缀（格式为：前缀_sessionId）
    public static final String REDIS_SESSION = "CMS_REDIS_SESSION_";
    //RedisSession中存放登陆用户信息的key
    public static final String SESSION_LOGIN_USER = "SESSION_LOGIN_USER";
    //RedisSession中存放登陆用户角色列表的key
    public static final String SESSION_LOGIN_USER_ROLE_LIST = "SESSION_LOGIN_USER_ROLE_LIST";
    //RedisSession中存放登陆用户所有菜单列表的key
    public static final String SESSION_LOGIN_USER_MENU_LIST = "SESSION_LOGIN_USER_MENU_LIST";

    //Redis中存放ShiroSession的名称前缀（格式为：前缀_sessionId）
    public static final String SHIRO_SESSION = "CMS_SHIRO_SESSION_";
    //Shiro框架默认的是否验证成功的key，键对应的值为boolean（该键为框架定义，不要修改）
    public static final String SHIRO_DEFAULT_AUTHENTICATED_KEY = "org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY";
    //Shiro框架默认的用户名key，键对应的值为String（该键为框架定义，不要修改）
    public static final String SHIRO_DEFAULT_PRINCIPALS = "org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY";
    //ShiroSession中用户拥有的功能权限URL信息的key
    public static final String SHIRO_FUNCTION_URL = "SHIRO_FUNCTION_URL";


}
