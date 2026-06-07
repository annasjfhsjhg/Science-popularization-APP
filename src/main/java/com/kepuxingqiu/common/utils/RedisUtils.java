package com.kepuxingqiu.common.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private final StringRedisTemplate redisTemplate;

    public RedisUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    /** 递增，返回递增后的值 */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /** 有序集合：添加或更新分数 */
    public void zAdd(String key, String member, double score) {
        redisTemplate.opsForZSet().add(key, member, score);
    }

    /** 有序集合：按分数倒序获取排名（0-based） */
    public Long zReverseRank(String key, String member) {
        return redisTemplate.opsForZSet().reverseRank(key, member);
    }

    // -------- Hash 操作（用于 user:level:{userId} 缓存）--------

    public void hset(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    public void hmset(String key, java.util.Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void hdelete(String key, String... fields) {
        redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }
}
