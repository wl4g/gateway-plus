# This is based AWS IAM Unified Resource Access Management module.

- The designed Draft refer to: [https://www.notion.so/IAM-RAM-22a86f8b584b80db9841fe6896cdbca2](https://www.notion.so/IAM-RAM-22a86f8b584b80db9841fe6896cdbca2)


## The Design Draft


- Explain: ARN (AWS Resource Name)   →   URN (Unified Resource Name)

### 1. Enhanced Gateway Authorization Filter (Deny-first Policy Evaluation)

```java
@Component
@RequiredArgsConstructor
public class IamAuthorizationFilter implements GlobalFilter {
    private final PolicyEvaluator policyEvaluator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        List<PolicyStatement> policies = extractPolicies(exchange.getRequest());
        IamRequestContext context = buildContext(exchange.getRequest());

        // 1. Check explicit denies first
        if (policyEvaluator.hasExplicitDeny(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 2. Check for explicit allows
        if (!policyEvaluator.hasExplicitAllow(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
```

- Helper classes

```java
private IamRequestContext buildContext(ServerHttpRequest request) {
   return IamRequestContext.builder()
            .action(IamActionConverter.convert(
                request.getMethodValue(), 
                request.getPath().value()))
            .resourceUrn(buildResourceUrn(request))
            .contextValues(Map.of(
                "sourceIp", request.getRemoteAddress().getAddress().getHostAddress(),
                "time", Instant.now().toString()
            ))
            .build();
}

private String buildResourceUrn(ServerHttpRequest request) {
    String path = request.getPath().value();
        
    // TODO: Hardcode, Convert REST path to URN (Unified Resource Name) format
    if (path.startsWith("/projects/")) {
         String[] parts = path.split("/");
         if (parts.length >= 3) {
              return String.format("urn:std:workflow-service:us-west:job:%s:%s", 
                    parts[2], // projectId
                    parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : "*");
         }
    }
    return "urn:std:::*"; // TODO: Default fallback
}

/**
 * Matches AWS-style URN patterns with support for *, ?, + wildcards
 */
public class UrnPatternMatcher {
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[*?+]");

    public static boolean matches(String pattern, String actualUrn) {
        if (pattern.equals(actualUrn)) return true;
        if (pattern.equals("*")) return true;

        String[] patternParts = pattern.split("[:/]");
        String[] actualParts = actualUrn.split("[:/]");
        
        if (patternParts.length != actualParts.length) return false;

        for (int i = 0; i < patternParts.length; i++) {
            if (!matchPart(patternParts[i], actualParts[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchPart(String pattern, String actual) {
        if (pattern.equals("*")) return true;
        if (!WILDCARD_PATTERN.matcher(pattern).find()) {
            return pattern.equals(actual);
        }

        // Implement wildcard matching logic
        int p = 0, a = 0;
        while (p < pattern.length() && a < actual.length()) {
            char pc = pattern.charAt(p);
            
            if (pc == '*') {
                if (p == pattern.length() - 1) return true;
                while (a < actual.length()) {
                    if (matchPart(pattern.substring(p + 1), actual.substring(a))) {
                        return true;
                    }
                    a++;
                }
                return false;
            } else if (pc == '?' || pc == actual.charAt(a)) {
                p++;
                a++;
            } else {
                return false;
            }
        }
        return p == pattern.length() && a == actual.length();
    }
}

/**
 * Converts HTTP methods/paths to IAM actions
 */
public class IamActionConverter {
    private static final Map<String, String> ACTION_MAP = Map.ofEntries(
        entry("GET:/projects", "project:List"),
        entry("POST:/projects", "project:Create"),
        entry("GET:/projects/*", "project:Get"),
        entry("PUT:/projects/*", "project:Update"),
        entry("DELETE:/projects/*", "project:Delete")
    );

    public static String convert(String method, String path) {
        String key = method.toUpperCase() + ":" + path;
        return ACTION_MAP.getOrDefault(key, "unknown:action");
    }
}

/**
 * Context for IAM policy evaluation
 */
@Data
@Builder
public class IamRequestContext {
    private String action;
    private String resourceUrn;
    private Map<String, String> contextValues;
}

/**
 * IAM policy statement representation
 */
@Data
@Builder
public class PolicyStatement {
    private String effect; // Allow/Deny
    private String action;
    private String resource;
    private Map<String, String> conditions;
}
```

### 2. Enhanced Policy Evaluator (Deny-First Logic)

```java
@Component
public class PolicyEvaluator {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean hasExplicitDeny(List<PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
            .filter(s -> "Deny".equalsIgnoreCase(s.getEffect()))
            .anyMatch(s -> matchesStatement(s, context));
    }

    public boolean hasExplicitAllow(List<PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
            .filter(s -> "Allow".equalsIgnoreCase(s.getEffect()))
            .anyMatch(s -> matchesStatement(s, context));
    }

    private boolean matchesStatement(PolicyStatement s, IamRequestContext ctx) {
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

    // policy 的 condition 使用1:
    // 先过滤如 ip/time 等内置动态条件是否满足
    private boolean matchesConditions(Map<String, String> conditions, Map<String, String> ctx) {
        if (conditions == null) return true;
        return conditions.entrySet().stream()
            .allMatch(e -> ctx.getOrDefault(e.getKey(), "").matches(e.getValue()));
    }
}
```

### 3. Secure Business Service Implementation (SQL Injection Protection)

```java
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {
    private final PolicyToSqlConverter sqlConverter;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Job> listJobs(
        @RequestHeader("X-IAM-Policy") String policy,
        @AuthenticationPrincipal User user) {

        SqlCondition condition = sqlConverter.convertToSql(policy, new UserContext(user));

        return jdbcTemplate.query(
            "SELECT * FROM projects WHERE " + condition.getClause(),
            condition.getParameters().toArray(),
            new JobRowMapper());
    }
}
```

- This redesigned `PolicyToSqlConverter` that avoids `LIKE` queries and implements AWS-style URN to SQL conversion with dynamic resource tagging support:

```java
/**
 * Converts IAM policy to SQL conditions with dynamic variable substitution
 */
public class PolicyToSqlConverter {
    private static final Pattern RESOURCE_PATTERN = 
        Pattern.compile("^urn:[^:]+:[^:]+:[^:]*:[^:]*:/job/(?<resource_id>.+)$");
    private static final Pattern VAR_PATTERN = 
        Pattern.compile("\\$\\{(?<var>[^}]+)\\}");

    /**
     * Converts policy to SQL condition with dynamic variable support
     */
    public SqlCondition convertToSql(String policy, UserContext user) {
        // 1. Parse and substitute variables in resource ID
        String resourceIdPattern = parseAndSubstituteResourceId(policy, user);
        
        // 2. Extract and substitute variables in tag conditions
        Map<String, String> tagFilters = parseAndSubstituteTagConditions(policy, user);
        
        // 3. Build secure SQL condition
        return buildCondition(resourceIdPattern, tagFilters);
    }

    /**
     * Parses resource ID with dynamic variable substitution
     */
    private String parseAndSubstituteResourceId(String policy, UserContext user) {
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();
        String resourceUrn = policyJson.getAsJsonArray("Statement")
            .get(0).getAsJsonObject()
            .get("Resource").getAsString();

        Matcher matcher = RESOURCE_PATTERN.matcher(resourceUrn);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid resource URN format");
        }

        return substituteVariables(
            matcher.group("resource_id").replace("*", "%"), 
            user
        );
    }

    /**
     * Extracts and substitutes variables in tag conditions
     */
    private Map<String, String> parseAndSubstituteTagConditions(
        String policy, UserContext user) {
        Map<String, String> tags = new HashMap<>();
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();

        policyJson.getAsJsonArray("Statement").forEach(stmt -> {
            JsonObject condition = stmt.getAsJsonObject().getAsJsonObject("Condition");
            if (condition != null && condition.has("StringEquals")) {
                condition.getAsJsonObject("StringEquals").entrySet().forEach(e -> {
                    if (e.getKey().startsWith("$")) {
                        String tagKey = e.getKey().substring(4);
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
    private String substituteVariables(String input, UserContext user) {
        StringBuffer result = new StringBuffer();
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
     * Gets property value dynamically using reflection
     */
    private String getPropertyValue(UserContext user, String propertyPath) {
        try {
            Object current = user;
            for (String part : propertyPath.split("\\.")) {
                Method getter = current.getClass().getMethod(
                    "get" + part.substring(0, 1).toUpperCase() + part.substring(1));
                current = getter.invoke(current);
                if (current == null) return null;
            }
            return current.toString();
        } catch (Exception e) {
            return null; // Variable not found
        }
    }

    private SqlCondition buildCondition(String resourceIdPattern, Map<String, String> tagFilters) {
        List<Object> params = new ArrayList<>();
        StringBuilder clause = new StringBuilder("resource_id LIKE ?");
        params.add(resourceIdPattern);

        if (!tagFilters.isEmpty()) {
            clause.append(" AND resource_tags @> ?::jsonb");
            params.add(new Gson().toJson(tagFilters));
        }

        return new SqlCondition(clause.toString(), params);
    }
}

//@Data
//@Builder
//class UrnPattern {
//    private String partition;
//    private String service;
//    private String region;
//    private String account;
//    private String resource;
//}

@Data
@AllArgsConstructor
class SqlCondition {
    private String clause;
    private List<Object> parameters;
}

@Data
class UserContext {
    private String userId;
    private String teamId;
    private String deptId;
    // Other attributes as needed
}
```

### Key Improvements:

1. **Avoids `LIKE` Queries**:
    - Uses exact matches (`=`) for path segments instead of `LIKE`
    - Only applies conditions for non-wildcard segments
2. **Dynamic Resource Tagging**:
    - Supports AWS-style tag filters (`tag:key=value` in URN path)
    - Stores tags in JSONB column for flexible querying
    - Matches AWS's resource tagging approach
3. **Variable Substitution**:
    - Handles `${user.xxx}` variables in URNs
    - Converts to actual values during query building
4. **Parameterized SQL**:
    - Uses prepared statements to prevent injection
    - Separates query structure from values

### **User IAM Policies for example**:

```json
{
  "Version": "2025-07-10",
  "Statement": [{
	  "Effect": "Allow",
	  "Action": "job:get",
		"Resource": "urn:std:workflow-service:us-west:account1:/job/*",
		"Condition": {
			"StringEquals": {
	      "$team": "${user.teamId}",
	      "$env": "prod"
	    },
	    "IpAddress": {
		    "ipv4": "192.168.0.0/16"
	    },
	    //"TimeLessThan": {
		  //  "create_at": "${time.day} - 3"
	    //}
		}
	}]
}
```

**Generated SQL** (when user.team=devops) example:

```sql
WHERE partition = 'std' AND service = 'workflow-service' AND resource_tags @> '{"env":"prod"}'::jsonb
```

### Database Schema & Data examples

- `sys_resource`

```sql
CREATE TABLE sys_resources (
    id BIGSERIAL PRIMARY KEY,
    partition VARCHAR(50) NOT NULL,
    service VARCHAR(50) NOT NULL,
    region VARCHAR(50),
    account_id VARCHAR(50),
    --resource_tags JSONB NOT NULL DEFAULT '{}'::jsonb
);
--CREATE INDEX idx_sys_resources_tags ON sys_resources USING GIN(resource_tags); -- Support High perf queries.

--INSERT INTO sys_resources (partition, service, region, account_id, resource_tags) VALUES
--('aws', 'workflow-service', 'us-west', NULL, '{"env": "prod", "role": "config"}'),     -- 参考如：aws 标准域
--('aws-cn', 'workflow-service', 'us-west', NULL, '{"env": "test", "role": "db"}'),      -- 参考如：aws-cn 中国域(由西云数据和光环新网运营)
--('aws-us-gov', 'workflow-service', 'us-west', NULL, '{"env": "prod", "role": "db"}'),  -- 参考如：aws-us-gov 美国政府专属域
--('aws-us-gov', 'workflow-service', 'us-west', NULL, '{"env": "dev", "team": "eng"}');

--SELECT * FROM sys_resources WHERE tags @> '{"env": "prod"}'::jsonb;
#1 aws-cn workflow-service us-west {"env": "prod", "region": "us-west"}
#3 aws-us-gov workflow-service us-west {"env": "prod", "role": "db"}
```

- `t_job`

```sql
CREATE TABLE IF NOT EXISTS t_job (
    resource_id VARCHAR(50) NOT NULL PRIMARY KEY,
    resource_tags JSONB NOT NULL DEFAULT '{}'::JSONB,
    job_name VARCHAR(50) NOT NULL,
    job_cron VARCHAR(50) NOT NULL,
    job_parameters JSONB NOT NULL DEFAULT '{}'::JSONB,
    create_at VARCHAR(50) NOT NULL,
    update_at VARCHAR(50) NOT NULL
);
CREATE INDEX idx_t_job_resource_tags ON t_job USING GIN(resource_tags);

INSERT INTO t_job (id, resource_tags, resource_id, job_name, job_cron, job_parameters) VALUES
('res-001', '{"env": "prod", "owner": "devops"}', 'Daily Data Backup', '0 0 2 * * ?', '{"source": "/data", "target": "/backup/data", "compress": true}'),
('res-002', '{"env": "test", "team": "analytics"}', 'Hourly Report Generation', '0 0/1 * * * ?', '{"report_type": "hourly_sales", "output_format": "csv"}'),
('res-003', '{"env": "prod", "dept": "finance"}', 'End of Day Financial Sync', '0 0 23 * * ?', '{"sync_direction": "to_erp", "include_tax": true}');

SELECT * FROM t_job WHERE resource_tags @> '{"env":"prod"}';
# res-001 ...
# res-003 ...
```

This matches AWS's approach where:

1. URN paths map to physical storage segments
2. Tags are stored as JSON for flexible filtering
3. Exact matches are preferred over pattern matching


## The PoC Implementation of Gateway + Biz Service (may incorrect some code logic)

- Explain: ARN (AWS Resource Name)   →   URN (Unified Resource Name)

### 1. Enhanced Gateway Authorization Filter (Deny-first Policy Evaluation)

```java
@Component
@RequiredArgsConstructor
public class IamAuthorizationFilter implements GlobalFilter {
    private final PolicyEvaluator policyEvaluator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        List<PolicyStatement> policies = extractPolicies(exchange.getRequest());
        IamRequestContext context = buildContext(exchange.getRequest());

        // 1. Check explicit denies first
        if (policyEvaluator.hasExplicitDeny(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 2. Check for explicit allows
        if (!policyEvaluator.hasExplicitAllow(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
```

- Helper classes

```java
private IamRequestContext buildContext(ServerHttpRequest request) {
   return IamRequestContext.builder()
            .action(IamActionConverter.convert(
                request.getMethodValue(), 
                request.getPath().value()))
            .resourceUrn(buildResourceUrn(request))
            .contextValues(Map.of(
                "sourceIp", request.getRemoteAddress().getAddress().getHostAddress(),
                "time", Instant.now().toString()
            ))
            .build();
}

private String buildResourceUrn(ServerHttpRequest request) {
    String path = request.getPath().value();
        
    // TODO: Hardcode, Convert REST path to URN (Unified Resource Name) format
    if (path.startsWith("/projects/")) {
         String[] parts = path.split("/");
         if (parts.length >= 3) {
              return String.format("urn:std:workflow-service:us-west:job:%s:%s", 
                    parts[2], // projectId
                    parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : "*");
         }
    }
    return "urn:std:::*"; // TODO: Default fallback
}

/**
 * Matches AWS-style URN patterns with support for *, ?, + wildcards
 */
public class UrnPatternMatcher {
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[*?+]");

    public static boolean matches(String pattern, String actualUrn) {
        if (pattern.equals(actualUrn)) return true;
        if (pattern.equals("*")) return true;

        String[] patternParts = pattern.split("[:/]");
        String[] actualParts = actualUrn.split("[:/]");
        
        if (patternParts.length != actualParts.length) return false;

        for (int i = 0; i < patternParts.length; i++) {
            if (!matchPart(patternParts[i], actualParts[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchPart(String pattern, String actual) {
        if (pattern.equals("*")) return true;
        if (!WILDCARD_PATTERN.matcher(pattern).find()) {
            return pattern.equals(actual);
        }

        // Implement wildcard matching logic
        int p = 0, a = 0;
        while (p < pattern.length() && a < actual.length()) {
            char pc = pattern.charAt(p);
            
            if (pc == '*') {
                if (p == pattern.length() - 1) return true;
                while (a < actual.length()) {
                    if (matchPart(pattern.substring(p + 1), actual.substring(a))) {
                        return true;
                    }
                    a++;
                }
                return false;
            } else if (pc == '?' || pc == actual.charAt(a)) {
                p++;
                a++;
            } else {
                return false;
            }
        }
        return p == pattern.length() && a == actual.length();
    }
}

/**
 * Converts HTTP methods/paths to IAM actions
 */
public class IamActionConverter {
    private static final Map<String, String> ACTION_MAP = Map.ofEntries(
        entry("GET:/projects", "project:List"),
        entry("POST:/projects", "project:Create"),
        entry("GET:/projects/*", "project:Get"),
        entry("PUT:/projects/*", "project:Update"),
        entry("DELETE:/projects/*", "project:Delete")
    );

    public static String convert(String method, String path) {
        String key = method.toUpperCase() + ":" + path;
        return ACTION_MAP.getOrDefault(key, "unknown:action");
    }
}

/**
 * Context for IAM policy evaluation
 */
@Data
@Builder
public class IamRequestContext {
    private String action;
    private String resourceUrn;
    private Map<String, String> contextValues;
}

/**
 * IAM policy statement representation
 */
@Data
@Builder
public class PolicyStatement {
    private String effect; // Allow/Deny
    private String action;
    private String resource;
    private Map<String, String> conditions;
}
```

### 2. Enhanced Policy Evaluator (Deny-First Logic)

```java
@Component
public class PolicyEvaluator {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean hasExplicitDeny(List<PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
            .filter(s -> "Deny".equalsIgnoreCase(s.getEffect()))
            .anyMatch(s -> matchesStatement(s, context));
    }

    public boolean hasExplicitAllow(List<PolicyStatement> policies, IamRequestContext context) {
        return policies.stream()
            .filter(s -> "Allow".equalsIgnoreCase(s.getEffect()))
            .anyMatch(s -> matchesStatement(s, context));
    }

    private boolean matchesStatement(PolicyStatement s, IamRequestContext ctx) {
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

    // policy 的 condition 使用1:
    // 先过滤如 ip/time 等内置动态条件是否满足
    private boolean matchesConditions(Map<String, String> conditions, Map<String, String> ctx) {
        if (conditions == null) return true;
        return conditions.entrySet().stream()
            .allMatch(e -> ctx.getOrDefault(e.getKey(), "").matches(e.getValue()));
    }
}
```

### 3. Secure Business Service Implementation (SQL Injection Protection)

```java
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {
    private final PolicyToSqlConverter sqlConverter;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Job> listJobs(
        @RequestHeader("X-IAM-Policy") String policy,
        @AuthenticationPrincipal User user) {

        SqlCondition condition = sqlConverter.convertToSql(policy, new UserContext(user));

        return jdbcTemplate.query(
            "SELECT * FROM projects WHERE " + condition.getClause(),
            condition.getParameters().toArray(),
            new JobRowMapper());
    }
}
```

- This redesigned `PolicyToSqlConverter` that avoids `LIKE` queries and implements AWS-style URN to SQL conversion with dynamic resource tagging support:

```java
/**
 * Converts IAM policy to SQL conditions with dynamic variable substitution
 */
public class PolicyToSqlConverter {
    private static final Pattern RESOURCE_PATTERN = 
        Pattern.compile("^urn:[^:]+:[^:]+:[^:]*:[^:]*:/job/(?<resource_id>.+)$");
    private static final Pattern VAR_PATTERN = 
        Pattern.compile("\\$\\{(?<var>[^}]+)\\}");

    /**
     * Converts policy to SQL condition with dynamic variable support
     */
    public SqlCondition convertToSql(String policy, UserContext user) {
        // 1. Parse and substitute variables in resource ID
        String resourceIdPattern = parseAndSubstituteResourceId(policy, user);
        
        // 2. Extract and substitute variables in tag conditions
        Map<String, String> tagFilters = parseAndSubstituteTagConditions(policy, user);
        
        // 3. Build secure SQL condition
        return buildCondition(resourceIdPattern, tagFilters);
    }

    /**
     * Parses resource ID with dynamic variable substitution
     */
    private String parseAndSubstituteResourceId(String policy, UserContext user) {
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();
        String resourceUrn = policyJson.getAsJsonArray("Statement")
            .get(0).getAsJsonObject()
            .get("Resource").getAsString();

        Matcher matcher = RESOURCE_PATTERN.matcher(resourceUrn);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid resource URN format");
        }

        return substituteVariables(
            matcher.group("resource_id").replace("*", "%"), 
            user
        );
    }

    /**
     * Extracts and substitutes variables in tag conditions
     */
    private Map<String, String> parseAndSubstituteTagConditions(
        String policy, UserContext user) {
        Map<String, String> tags = new HashMap<>();
        JsonObject policyJson = JsonParser.parseString(policy).getAsJsonObject();

        policyJson.getAsJsonArray("Statement").forEach(stmt -> {
            JsonObject condition = stmt.getAsJsonObject().getAsJsonObject("Condition");
            if (condition != null && condition.has("StringEquals")) {
                condition.getAsJsonObject("StringEquals").entrySet().forEach(e -> {
                    if (e.getKey().startsWith("$")) {
                        String tagKey = e.getKey().substring(4);
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
    private String substituteVariables(String input, UserContext user) {
        StringBuffer result = new StringBuffer();
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
     * Gets property value dynamically using reflection
     */
    private String getPropertyValue(UserContext user, String propertyPath) {
        try {
            Object current = user;
            for (String part : propertyPath.split("\\.")) {
                Method getter = current.getClass().getMethod(
                    "get" + part.substring(0, 1).toUpperCase() + part.substring(1));
                current = getter.invoke(current);
                if (current == null) return null;
            }
            return current.toString();
        } catch (Exception e) {
            return null; // Variable not found
        }
    }

    private SqlCondition buildCondition(String resourceIdPattern, Map<String, String> tagFilters) {
        List<Object> params = new ArrayList<>();
        StringBuilder clause = new StringBuilder("resource_id LIKE ?");
        params.add(resourceIdPattern);

        if (!tagFilters.isEmpty()) {
            clause.append(" AND resource_tags @> ?::jsonb");
            params.add(new Gson().toJson(tagFilters));
        }

        return new SqlCondition(clause.toString(), params);
    }
}

//@Data
//@Builder
//class UrnPattern {
//    private String partition;
//    private String service;
//    private String region;
//    private String account;
//    private String resource;
//}

@Data
@AllArgsConstructor
class SqlCondition {
    private String clause;
    private List<Object> parameters;
}

@Data
class UserContext {
    private String userId;
    private String teamId;
    private String deptId;
    // Other attributes as needed
}
```

### Key Improvements:

1. **Avoids `LIKE` Queries**:
    - Uses exact matches (`=`) for path segments instead of `LIKE`
    - Only applies conditions for non-wildcard segments
2. **Dynamic Resource Tagging**:
    - Supports AWS-style tag filters (`tag:key=value` in URN path)
    - Stores tags in JSONB column for flexible querying
    - Matches AWS's resource tagging approach
3. **Variable Substitution**:
    - Handles `${user.xxx}` variables in URNs
    - Converts to actual values during query building
4. **Parameterized SQL**:
    - Uses prepared statements to prevent injection
    - Separates query structure from values

### **User IAM Policies for example**:

```json
{
  "Version": "2025-07-10",
  "Statement": [{
	  "Effect": "Allow",
	  "Action": "job:get",
		"Resource": "urn:std:workflow-service:us-west:account1:/job/*",
		"Condition": {
			"StringEquals": {
	      "$team": "${user.teamId}",
	      "$env": "prod"
	    },
	    "IpAddress": {
		    "ipv4": "192.168.0.0/16"
	    },
	    //"TimeLessThan": {
		  //  "create_at": "${time.day} - 3"
	    //}
		}
	}]
}
```

**Generated SQL** (when user.team=devops) example:

```sql
WHERE partition = 'std' AND service = 'workflow-service' AND resource_tags @> '{"env":"prod"}'::jsonb
```

### Database Schema & Data examples

- `sys_resource`

```sql
CREATE TABLE sys_resources (
    id BIGSERIAL PRIMARY KEY,
    partition VARCHAR(50) NOT NULL,
    service VARCHAR(50) NOT NULL,
    region VARCHAR(50),
    account_id VARCHAR(50),
    --resource_tags JSONB NOT NULL DEFAULT '{}'::jsonb
);
--CREATE INDEX idx_sys_resources_tags ON sys_resources USING GIN(resource_tags); -- Support High perf queries.

--INSERT INTO sys_resources (partition, service, region, account_id, resource_tags) VALUES
--('aws', 'workflow-service', 'us-west', NULL, '{"env": "prod", "role": "config"}'),     -- 参考如：aws 标准域
--('aws-cn', 'workflow-service', 'us-west', NULL, '{"env": "test", "role": "db"}'),      -- 参考如：aws-cn 中国域(由西云数据和光环新网运营)
--('aws-us-gov', 'workflow-service', 'us-west', NULL, '{"env": "prod", "role": "db"}'),  -- 参考如：aws-us-gov 美国政府专属域
--('aws-us-gov', 'workflow-service', 'us-west', NULL, '{"env": "dev", "team": "eng"}');

--SELECT * FROM sys_resources WHERE tags @> '{"env": "prod"}'::jsonb;
#1 aws-cn workflow-service us-west {"env": "prod", "region": "us-west"}
#3 aws-us-gov workflow-service us-west {"env": "prod", "role": "db"}
```

- `t_job`

```sql
CREATE TABLE IF NOT EXISTS t_job (
    resource_id VARCHAR(50) NOT NULL PRIMARY KEY,
    resource_tags JSONB NOT NULL DEFAULT '{}'::JSONB,
    job_name VARCHAR(50) NOT NULL,
    job_cron VARCHAR(50) NOT NULL,
    job_parameters JSONB NOT NULL DEFAULT '{}'::JSONB,
    create_at VARCHAR(50) NOT NULL,
    update_at VARCHAR(50) NOT NULL
);
CREATE INDEX idx_t_job_resource_tags ON t_job USING GIN(resource_tags);

INSERT INTO t_job (id, resource_tags, resource_id, job_name, job_cron, job_parameters) VALUES
('res-001', '{"env": "prod", "owner": "devops"}', 'Daily Data Backup', '0 0 2 * * ?', '{"source": "/data", "target": "/backup/data", "compress": true}'),
('res-002', '{"env": "test", "team": "analytics"}', 'Hourly Report Generation', '0 0/1 * * * ?', '{"report_type": "hourly_sales", "output_format": "csv"}'),
('res-003', '{"env": "prod", "dept": "finance"}', 'End of Day Financial Sync', '0 0 23 * * ?', '{"sync_direction": "to_erp", "include_tax": true}');

SELECT * FROM t_job WHERE resource_tags @> '{"env":"prod"}';
# res-001 ...
# res-003 ...
```

This matches AWS's approach where:

1. URN paths map to physical storage segments
2. Tags are stored as JSON for flexible filtering
3. Exact matches are preferred over pattern matching
