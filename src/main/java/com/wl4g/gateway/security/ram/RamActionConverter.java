package com.wl4g.gateway.security.ram;

import java.util.HashMap;
import java.util.Map;

public class RamActionConverter {
    private static final Map<String, String> HARDCODE_ACTION_MAP;

    static {
        HARDCODE_ACTION_MAP = new HashMap<>();
        HARDCODE_ACTION_MAP.put("POST:/jobs", "job:Create");
        HARDCODE_ACTION_MAP.put("GET:/jobs", "job:List");
        HARDCODE_ACTION_MAP.put("GET:/jobs/*", "job:Get");
        HARDCODE_ACTION_MAP.put("DELETE:/jobs/*", "job:Delete");
    }

    public static String convert(String method, String path) {
        String key = method.toUpperCase() + ":" + path;
        return HARDCODE_ACTION_MAP.getOrDefault(key, "unknown:action");
    }
}
