package com.my.gateway.utils;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectUtils {

    public static Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        try {
            Class<?> clazz = obj.getClass();
            List<Field> fieldList = new ArrayList<Field>();
            while (clazz != null) {
                fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
                clazz = clazz.getSuperclass();
            }
            for (Field field : fieldList) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(obj);
                map.put(fieldName, value);
            }
        } catch (Exception e) {

        }

        return map;
    }

    public static String mapToString(Map<String, String[]> paramMap) {
        if (paramMap == null) {
            return "";
        }
        Map<String, Object> params = new HashMap<>(16);
        for (Map.Entry<String, String[]> param : paramMap.entrySet()) {

            String key = param.getKey();
            String paramValue = (param.getValue() != null && param.getValue().length > 0 ? param.getValue()[0] : "");
            if ("password".equalsIgnoreCase(param.getKey())) {
                params.put(key, "");
            } else {
                params.put(key, paramValue);
            }
        }
        return new Gson().toJson(params);
    }
}
