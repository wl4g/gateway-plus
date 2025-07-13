package com.wl4g.gateway.security.ram;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.*;

// TODO: testing
public class PolicyToSqlConverter {
    private static final Pattern RESOURCE_PATTERN =
            Pattern.compile("^arn:[^:]+:[^:]+:[^:]*:[^:]*:[^/]+/(?<resourceId>.+)$");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(?<var>[^}]+)}");

    public RamRequestContext.SqlCondition convertToSql(String policy, RamRequestContext.UserContext user) {
        String resourceIdPattern = parseAndSubstituteResourceId(policy, user);
        Map<String, String> tagFilters = parseAndSubstituteTagConditions(policy, user);
        return buildCondition(resourceIdPattern, tagFilters);
    }

    // TODO: add support for multiple resource ids?
    private String parseAndSubstituteResourceId(String policy, RamRequestContext.UserContext user) {
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();
        JsonElement resourceElement = policyJson.getAsJsonArray("Statement")
                .get(0).getAsJsonObject()
                .get("Resource");

        String resourceUrn;
        if (resourceElement.isJsonArray()) {
            // Take the first resource if it's an array
            resourceUrn = resourceElement.getAsJsonArray().get(0).getAsString();
        } else {
            resourceUrn = resourceElement.getAsString();
        }

        Matcher matcher = RESOURCE_PATTERN.matcher(resourceUrn);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid resource URN format");
        }

        return substituteVariables(
                matcher.group("resourceId").replace("*", "%"),
                user
        );
    }

    /**
     * Extracts and substitutes variables in tag conditions
     */
    private Map<String, String> parseAndSubstituteTagConditions(String policy, RamRequestContext.UserContext user) {
        Map<String, String> tags = new HashMap<>();
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();

        policyJson.getAsJsonArray("Statement").forEach(stmt -> {
            JsonObject condition = stmt.getAsJsonObject().getAsJsonObject("Condition");
            if (condition != null && condition.has("StringEquals")) {
                condition.getAsJsonObject("StringEquals").entrySet().forEach(e -> {
                    if (e.getKey().startsWith("$")) {
                        String tagKey = e.getKey().substring(4); // Remove "$tag:" prefix
                        String tagValue = substituteVariables(e.getValue().getAsString(), user);
                        tags.put(tagKey, tagValue);
                    }
                });
            }
        });

        return tags;
    }

    /**
     * Generic variable substitution using reflection
     */
    private String substituteVariables(String input, RamRequestContext.UserContext user) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = VAR_PATTERN.matcher(input);

        while (matcher.find()) {
            String varName = matcher.group("var");
            String replacement = getPropertyValue(user, varName);
            matcher.appendReplacement(result, replacement != null ? replacement : "");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Builds the final SQL condition with parameterized queries
     */
    private RamRequestContext.SqlCondition buildCondition(String resourceIdPattern, Map<String, String> tagFilters) {
        List<Object> params = new ArrayList<>();
        StringBuilder clause = new StringBuilder("resource_id LIKE ?");
        params.add(resourceIdPattern);

        if (!tagFilters.isEmpty()) {
            clause.append(" AND resource_tags @> ?::jsonb");
            params.add(new Gson().toJson(tagFilters));
        }

        return new RamRequestContext.SqlCondition(clause.toString(), params);
    }

    /**
     * Gets property value dynamically using reflection
     */
    private String getPropertyValue(RamRequestContext.UserContext user, String propertyPath) {
        try {
            Object current = user;
            for (String part : propertyPath.split("\\.")) {
                Method getter = current.getClass().getMethod(
                        "get" + part.substring(0, 1).toUpperCase() + part.substring(1));
                current = getter.invoke(current);
                if (current == null) {
                    return null;
                }
            }
            return current.toString();
        } catch (Exception e) {
            return null; // Variable not found
        }
    }
}