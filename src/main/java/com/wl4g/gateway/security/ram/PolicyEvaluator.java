package com.wl4g.gateway.security.ram;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

@Component
public class PolicyEvaluator {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean hasExplicitDeny(List<IamRequestContext.PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
                .filter(s -> "Deny".equalsIgnoreCase(s.getEffect()))
                .anyMatch(s -> matchesStatement(s, context));
    }

    public boolean hasExplicitAllow(List<IamRequestContext.PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
                .filter(s -> "Allow".equalsIgnoreCase(s.getEffect()))
                .anyMatch(s -> matchesStatement(s, context));
    }

    private boolean matchesStatement(IamRequestContext.PolicyStatement s, IamRequestContext ctx) {
        return matchesAction(s.getAction(), ctx.getAction())
                && matchesResource(s.getResource(), ctx.getResourceUrn())
                && matchesConditions(s.getConditions(), ctx.getContextValues());
    }

    private boolean matchesAction(String pattern, String action) {
        return pathMatcher.match(pattern.replace(":", "/"), action.replace(":", "/"));
    }

    private boolean matchesResource(String pattern, String resource) {
        return UrnPatternMatcher.matches(pattern, resource);
    }

    private boolean matchesConditions(Map<String, String> conditions, Map<String, String> ctx) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return conditions.entrySet().stream()
                .allMatch(e -> ctx.getOrDefault(e.getKey(), "").matches(e.getValue()));
    }
}
