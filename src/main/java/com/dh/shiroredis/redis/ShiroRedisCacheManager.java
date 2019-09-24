package com.dh.shiroredis.redis;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class ShiroRedisCacheManager extends AbstractCacheManager {
    private JedisConnectionFactory jedisConnectionFactory;

    public ShiroRedisCacheManager(JedisConnectionFactory jedisConnectionFactory){
        this.jedisConnectionFactory = jedisConnectionFactory;
    }
    @Override
    protected Cache createCache(String name) throws CacheException {
        return new ShiroRedisCache(name,jedisConnectionFactory);
    }

}
