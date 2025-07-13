package com.wl4g.gateway.security.ram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

// TODO: testing
@Data
@Builder
public class RamRequestContext {
    private String action;
    private String resourceUrn;
    private Map<String, String> contextValues;

    @Data
    @Builder
    public static class PolicyStatement {
        private String effect;
        private String action;
        private String resource;
        private Map<String, String> conditions;
    }

    @Data
    @AllArgsConstructor
    public static class SqlCondition {
        private String clause;
        private List<Object> parameters;
    }

    @Data
    public static class UserContext {
        private String uid;
        private String gid;
        private String teamId;
        private String deptId;
    }

}