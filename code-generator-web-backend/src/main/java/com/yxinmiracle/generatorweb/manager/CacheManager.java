package com.yxinmiracle.generatorweb.manager;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
public class CacheManager {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(100, TimeUnit.MINUTES)
            .build();

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        localCache.put(key, value);
        stringRedisTemplate.opsForValue().set(key, value, 100, TimeUnit.MINUTES);
    }

    public String get(String key) {
        // 先从本地缓存中获取
        String value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 从redis中获取
        value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void del(String key) {
        localCache.invalidate(key);
        stringRedisTemplate.delete(key);
    }


}
