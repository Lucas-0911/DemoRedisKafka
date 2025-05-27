package com.lucas.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class RedisUtils {

    private static final Logger log = LogManager.getLogger(RedisUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setObject(String key, Object value) {
        if (key == null) return;

        Jedis jedis = null;
        try {
            jedis = RedisConnection.getConnection();
            String jsonValue = objectMapper.writeValueAsString(value);
            jedis.set(key, jsonValue);
        } catch (Exception e) {
            log.error("Redis setObject error: {}", ExceptionUtils.getStackTrace(e));
        } finally {
            RedisConnection.closeConnection(jedis);
        }
    }

    public void setObject(String key, Object value, long timeoutSeconds) {
        if (key == null) return;

        Jedis jedis = null;
        try {
            jedis = RedisConnection.getConnection();
            String jsonValue = objectMapper.writeValueAsString(value);
            jedis.setex(key, (int) timeoutSeconds, jsonValue);
        } catch (Exception e) {
            log.error("Redis setObject with timeout error: {}", ExceptionUtils.getStackTrace(e));
        } finally {
            RedisConnection.closeConnection(jedis);
        }
    }

    public static <T> T getObject(String key, Class<T> targetClass) {
        if (key == null) return null;

        Jedis jedis = null;
        try {
            jedis = RedisConnection.getConnection();
            String jsonValue = jedis.get(key);
            if (jsonValue == null) return null;
            Object result = objectMapper.readValue(jsonValue, targetClass);
            if (result instanceof Map) {
                return objectMapper.convertValue(result, targetClass);
            }

            if (result instanceof String) {
                return objectMapper.readValue((String) result, targetClass);
            }
            return null;
        } catch (Exception e) {
            log.error("Redis getObject error: {}", ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            RedisConnection.closeConnection(jedis);
        }
    }

    public static void delete(String key) {
        if (key == null) return;
        Jedis jedis = null;
        try {
            jedis = RedisConnection.getConnection();
            jedis.del(key);
        } catch (Exception e) {
            log.error("Redis delObject error: {}", ExceptionUtils.getStackTrace(e));
        } finally {
            RedisConnection.closeConnection(jedis);
        }
    }
}
