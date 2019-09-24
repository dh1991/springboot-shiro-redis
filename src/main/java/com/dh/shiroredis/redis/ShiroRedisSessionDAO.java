package com.dh.shiroredis.redis;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ShiroRedisSessionDAO extends AbstractSessionDAO {
    private static Logger logger = LoggerFactory.getLogger(ShiroRedisSessionDAO.class);
    private String keyPrefix = "shiro:session:";
    private JedisConnectionFactory jedisConnectionFactory;
    private Long expire =0L;
    private RedisSerializer keySerializer = new StringRedisSerializer();
    private RedisSerializer valueSerializer = new ObjectRedisSerializer();
    public ShiroRedisSessionDAO() {
    }
    @Override
    public void update(Session session) throws UnknownSessionException {
        this.saveSession(session);
    }

    private void saveSession(Session session) throws UnknownSessionException {
        RedisConnection redisConnection = null;
        if (session != null && session.getId() != null) {
            try {
                byte[] key =  keySerializer.serialize(getByteKey(session.getId()));
                byte[] value = valueSerializer.serialize(session);
                redisConnection = jedisConnectionFactory.getConnection();
                Expiration expiration = Expiration.from(expire*1000L, TimeUnit.MILLISECONDS);
                session.setTimeout(expire*1000L);
                redisConnection.set(key, value,expiration, RedisStringCommands.SetOption.UPSERT);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(redisConnection!=null){
                    redisConnection.close();
                }
            }
        } else {
            logger.error("session or session id is null");
        }
    }
    @Override
    public void delete(Session session) {
        if (session != null && session.getId() != null) {
            RedisConnection redisConnection = null;
            try {
                redisConnection = jedisConnectionFactory.getConnection();
                redisConnection.del(keySerializer.serialize(getByteKey(session.getId())));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(redisConnection!=null){
                    redisConnection.close();
                }
            }
        } else {
            logger.error("session or session id is null");
        }
    }
    @Override
    public Collection<Session> getActiveSessions() {
        Set<Session> sessions = new HashSet();
        RedisConnection redisConnection = null;
        try {
            redisConnection = jedisConnectionFactory.getConnection();
            String sb = keyPrefix + "*";
            Set<byte[]> keys = redisConnection.keys(sb.getBytes("utf-8"));
            if (keys != null && keys.size() > 0) {
                Iterator i$ = keys.iterator();
                while(i$.hasNext()) {
                    byte[] key = (byte[])i$.next();
                    Session s = (Session)valueSerializer.deserialize(redisConnection.get(key));
                    sessions.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(redisConnection!=null){
                redisConnection.close();
            }
        }
        return sessions;
    }
    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }
    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            logger.error("session id is null");
            return null;
        } else {
            RedisConnection redisConnection = null;
            Session s = null;
            try {
                redisConnection = jedisConnectionFactory.getConnection();
                byte[] key = keySerializer.serialize(getByteKey(sessionId));
                s = (Session)valueSerializer.deserialize(redisConnection.get(key));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(redisConnection!=null){
                    redisConnection.close();
                }
            }
            return s;
        }
    }

    private String getByteKey(Serializable sessionId)  {
        String preKey = this.keyPrefix + sessionId;
        return preKey;
    }

    public JedisConnectionFactory getJedisConnectionFactory() {
        return jedisConnectionFactory;
    }

    public void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory) {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }
}
