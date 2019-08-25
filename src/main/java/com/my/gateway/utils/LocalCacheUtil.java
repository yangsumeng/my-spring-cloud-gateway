package com.my.gateway.utils;

import java.util.concurrent.ConcurrentHashMap;

public class LocalCacheUtil {
    private static ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<>();

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     */
    public static void setCache(String key, Object value) {
        cacheMap.put(key, value);
    }

    /**
     * 获取缓存
     *
     * @param key
     */
    public static Object getCache(String key) {
        return cacheMap.get(key);
    }
}
