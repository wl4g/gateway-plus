package com.wl4g.gateway.security.ram;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

// TODO: testing
@Component
@Slf4j
@RequiredArgsConstructor
public class IamAuthorizationFilter implements GlobalFilter, Ordered {
    private final PolicyEvaluator policyEvaluator;
    private final JwtParser jwtParser = Jwts.parserBuilder()
            // .setSigningKey("yourSigningKey") // TODO: using configuration & add login API to success response jwt with this singning key
            .requireIssuer("GWP") // Gateway-Plus
            //.requireAudience("GWP")
            .setAllowedClockSkewSeconds(60)
            //.setSigningKeyResolver(new SigningKeyResolverAdapter())
            .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (isNull(token) || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String jwt = token.substring(7);
        Claims claims;
        try {
            claims = jwtParser.parseClaimsJws(jwt).getBody();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract the UID,GID from JWT claims.
        String uid = claims.get("uid", String.class);
        String gid = claims.get("gid", String.class);

        log.info("Extracted the JWT uid: {}, gid: {}", uid, gid);

        List<IamRequestContext.PolicyStatement> policies = loadCombinePolicy(uid, gid);
        IamRequestContext context = buildContext(exchange.getRequest());

        if (policyEvaluator.hasExplicitDeny(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        if (!policyEvaluator.hasExplicitAllow(policies, context)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    /**
     * Loads and combines policies for both user and user group
     *
     * @param uid User ID from request header
     * @param gid User Group ID from request header
     * @return Combined list of policy statements
     */
    private List<IamRequestContext.PolicyStatement> loadCombinePolicy(String uid, String gid) {
        // TODO: Implement actual policy loading logic from storage (e.g. PG,Redis)
        // This should combine both user policies and user group policies
        return Collections.emptyList();
    }

    private IamRequestContext buildContext(ServerHttpRequest request) {
        return IamRequestContext.builder()
                .action(IamActionConverter.convert(
                        request.getMethodValue(),
                        request.getPath().value()))
                .resourceUrn(buildResourceUrn(request))
                .contextValues(Map.of(
                        "sourceIp", requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress(),
                        "time", Instant.now().toString()
                ))
                .build();
    }

    /**
     * Converts REST path to URN (Unified Resource Name) format
     * Example: /projects/123/jobs/daily → urn:std:workflow-service:us-west:job:123:daily
     */
    private String buildResourceUrn(ServerHttpRequest request) {
        String path = request.getPath().value();

        if (path.startsWith("/projects/")) {
            String[] parts = path.split("/");
            if (parts.length >= 3) {
                return String.format("urn:std:workflow-service:us-west:job:%s:%s",
                        parts[2], // projectId
                        parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : "*");
            }
        } else if (path.startsWith("/api/")) {
            // Handle API paths
            String[] parts = path.split("/");
            if (parts.length >= 3) {
                return String.format("urn:std:api-service:global:resource:%s:%s",
                        parts[2], // apiName
                        parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : "*");
            }
        } else if (path.startsWith("/services/")) {
            // Handle service paths
            String[] parts = path.split("/");
            if (parts.length >= 3) {
                return String.format("urn:std:internal-service:private:component:%s:%s",
                        parts[2], // serviceName
                        parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : "*");
            }
        }

        // Default fallback for unrecognized paths
        return String.format("urn:std:::*:%s", UUID.randomUUID());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}