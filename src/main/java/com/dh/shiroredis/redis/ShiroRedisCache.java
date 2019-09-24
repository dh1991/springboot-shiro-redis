package com.dh.shiroredis.redis;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;

public class ShiroRedisCache<K,V> implements Cache<K,V> {
    private String prefix = "shiro_redis";
    private JedisConnectionFactory jedisConnectionFactory;
    private RedisSerializer valueSerializer = new ObjectRedisSerializer();
    private RedisSerializer keyRedisSerializer = new StringRedisSerializer();

    public String getPrefix() {
        return prefix+":";
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public ShiroRedisCache(String prefix,JedisConnectionFactory jedisConnectionFactory){
        this.prefix = prefix;
        this.jedisConnectionFactory = jedisConnectionFactory;
    }


    @Override
    public V get(K k) throws CacheException {
        if (k == null) {
            return null;
        }
        RedisConnection redisConnection = null;
        V v = null;
        try {
            redisConnection = jedisConnectionFactory.getConnection();
            byte[] bytes = getBytesKey(k);
            byte[] value = jedisConnectionFactory.getConnection().get(bytes);
            v  =(V)valueSerializer.deserialize(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(redisConnection!=null){
                redisConnection.close();
            }
        }
        return v;

    }

    @Override
    public V put(K k, V v) throws CacheException {
        if (k== null || v == null) {
            return null;
        }
        RedisConnection redisConnection = null;
        try {
            redisConnection = jedisConnectionFactory.getConnection();
            byte[] bytes = getBytesKey(k);
            byte[] value =valueSerializer.serialize(v);
            redisConnection.set(bytes,value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(redisConnection!=null){
                redisConnection.close();
            }
        }
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        if(k==null){
            return null;
        }
        RedisConnection redisConnection = null;
        V v = null;
        try {
            redisConnection = jedisConnectionFactory.getConnection();
            byte[] bytes = getBytesKey(k);
            v = (V) valueSerializer.deserialize(redisConnection.get(bytes));
            redisConnection.del(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(redisConnection!=null){
                redisConnection.close();
            }
        }
        return v;
    }

    @Override
    public void clear() throws CacheException {
        jedisConnectionFactory.getConnection().flushDb();
    }

    @Override
    public int size() {
        return jedisConnectionFactory.getConnection().dbSize().intValue();
    }

    @Override
    public Set<K> keys() {
        RedisConnection redisConnection = null;
        Set<K> sets = null;
        try {
            redisConnection = jedisConnectionFactory.getConnection();
            byte[] bytes = (getPrefix()+"*").getBytes();
            Set<byte[]> keys = redisConnection.keys(bytes);
            sets = new HashSet<>();
            for (byte[] key:keys) {
                sets.add((K)key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(redisConnection!=null){
                redisConnection.close();
            }
        }
        return sets;
    }

    @Override
    public Collection<V> values() {
        Set<K> keys = keys();
        List<V> values = new ArrayList<>(keys.size());
        for(K k :keys){
            values.add(get(k));
        }
        return values;
    }

    private byte[] getBytesKey(K key){
        String prekey = this.getPrefix() + key;
        return keyRedisSerializer.serialize(prekey);
    }

}
