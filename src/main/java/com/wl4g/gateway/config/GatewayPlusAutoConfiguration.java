/*
 * Copyright 2017 ~ 2035 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.gateway.circuitbreaker.config.PlusCircuitBreakerAutoConfiguration;
import com.wl4g.gateway.fault.config.FaultAutoConfiguration;
import com.wl4g.gateway.ipfilter.config.IpFilterAutoConfiguration;
import com.wl4g.gateway.loadbalance.config.CanaryLoadbalanceAutoConfiguration;
import com.wl4g.gateway.logging.config.LoggingAutoConfiguration;
import com.wl4g.gateway.metrics.config.GatewayPlusMetricsAutoConfiguration;
import com.wl4g.gateway.requestlimit.config.PlusRequestLimiterAutoConfiguration;
import com.wl4g.gateway.requestsize.config.PlusRequestSizeAutoConfiguration;
import com.wl4g.gateway.responsecache.config.ResponseCacheAutoConfiguration;
import com.wl4g.gateway.retry.config.PlusRetryAutoConfiguration;
import com.wl4g.gateway.route.config.RouteAutoConfiguration;
import com.wl4g.gateway.security.config.GatewayPlusSecurityAutoConfiguration;
import com.wl4g.gateway.server.config.GatewayWebServerAutoConfiguration;
import com.wl4g.gateway.trace.config.GrayTraceAutoConfiguration;
//import com.wl4g.gateway.trace.config.GrayTraceAutoConfiguration;
import com.wl4g.gateway.traffic.config.TrafficAutoConfiguration;

import reactor.core.publisher.Mono;

/**
 * IAM gateway autoconfiguration.
 * <p>
 * {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2018/9/16
 * @since v1.0
 */
@Configuration(proxyBeanMethods = false)
@Import({GatewayWebServerAutoConfiguration.class, IpFilterAutoConfiguration.class, PlusRequestSizeAutoConfiguration.class,
        PlusRequestLimiterAutoConfiguration.class, PlusCircuitBreakerAutoConfiguration.class, RouteAutoConfiguration.class,
        CanaryLoadbalanceAutoConfiguration.class, GatewayPlusSecurityAutoConfiguration.class, GrayTraceAutoConfiguration.class,
        LoggingAutoConfiguration.class, GatewayPlusMetricsAutoConfiguration.class, FaultAutoConfiguration.class,
        PlusRetryAutoConfiguration.class, TrafficAutoConfiguration.class, ResponseCacheAutoConfiguration.class})
public class GatewayPlusAutoConfiguration {

    @Bean
    public ReactiveByteArrayRedisTemplate reactiveByteArrayRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveByteArrayRedisTemplate(connectionFactory);
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return chain.filter(exchange);
            }
        };
    }

    // @Bean
    // public WebClient webClient() {
    // final int maxMemorySize = 256 * 1024 * 1024;
    // final ExchangeStrategies strategies = ExchangeStrategies.builder()
    // .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxMemorySize))
    // .build();
    // return WebClient.builder().exchangeStrategies(strategies).build();
    // }

    // @Bean
    // public WebFilter corsWebFilter() {
    // return (ServerWebExchange ctx, WebFilterChain chain) -> {
    // ServerHttpRequest request = ctx.getRequest();
    // if (!CorsUtils.isCorsRequest(request)) {
    // return chain.filter(ctx);
    // }
    //
    // HttpHeaders requestHeaders = request.getHeaders();
    // ServerHttpResponse response = ctx.getResponse();
    // HttpMethod requestMethod =
    // requestHeaders.getAccessControlRequestMethod();
    // HttpHeaders headers = response.getHeaders();
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
    // requestHeaders.getOrigin());
    // headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
    // requestHeaders.getAccessControlRequestHeaders());
    // if (requestMethod != null) {
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
    // requestMethod.name());
    // }
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    // headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
    // headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "18000L");
    // if (request.getMethod() == HttpMethod.OPTIONS) {
    // response.setStatusCode(HttpStatus.OK);
    // return Mono.empty();
    // }
    // return chain.filter(ctx);
    // };
    // }

}