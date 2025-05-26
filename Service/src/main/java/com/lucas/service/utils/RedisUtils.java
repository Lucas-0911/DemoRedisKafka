package com.lucas.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Log4j2
@Component
public class RedisUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void setObject(String key, Object value) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void setObject(String key, Object value, long timeout) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public <T> T getObject(String key, Class<T> targetClass) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }

        try {
            if (result instanceof Map) {
                return objectMapper.convertValue(result, targetClass);
            }

            if (result instanceof String) {
                return objectMapper.readValue((String) result, targetClass);
            }

            return null;
        } catch (Exception e) {
            log.error("Error converting Redis value to object: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
