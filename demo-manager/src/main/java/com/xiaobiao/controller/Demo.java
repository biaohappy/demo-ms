package com.xiaobiao.controller;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author wuxiaobiao
 * @create 2017-12-19 9:15
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
public class Demo {

    @Test
    public void demo1() throws IOException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/config/spring-data-redis.xml");
        RedisTemplate redisTemplate = (RedisTemplate) applicationContext.getBean("redisTemplate");
        JedisConnectionFactory js = (JedisConnectionFactory)applicationContext.getBean("jedisConnectionFactory");
        System.out.println(js);

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("172.16.9.249", 3697), 2000);

        JedisConnection jc = js.getConnection();
        Jedis jedis = jc.getNativeConnection();

        ValueOperations<String, String> redis = redisTemplate.opsForValue();
        redis.set("name1","123456");
        String name = redis.get("name1");
        System.out.println(name);
    }
}
