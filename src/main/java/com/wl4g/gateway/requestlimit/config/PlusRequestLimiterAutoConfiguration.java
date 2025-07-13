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
package com.wl4g.gateway.requestlimit.config;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.wl4g.gateway.constant.GatewayPlusConstants;
import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;
import com.wl4g.gateway.requestlimit.PlusRequestLimiterFilterFactory;
import com.wl4g.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.gateway.requestlimit.configurer.RedisLimiterStrategyConfigurer;
import com.wl4g.gateway.requestlimit.event.DefaultRedisRequestLimitEventRecorder;
import com.wl4g.gateway.requestlimit.key.HeaderPlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.HostPlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.PlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.PlusKeyResolver.KeyResolverProvider;
import com.wl4g.gateway.requestlimit.key.PlusKeyResolver.KeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.IntervalPlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.IpRangePlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.PathPlusKeyResolver;
import com.wl4g.gateway.requestlimit.key.PrincipalPlusKeyResolver;
import com.wl4g.gateway.requestlimit.limiter.PlusRequestLimiter;
import com.wl4g.gateway.requestlimit.limiter.PlusRequestLimiter.RequestLimiterPrivoder;
import com.wl4g.gateway.requestlimit.limiter.quota.RedisQuotaPlusRequestLimiter;
import com.wl4g.gateway.requestlimit.limiter.rate.RedisRatePlusRequestLimiter;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link PlusRequestLimiterAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2021-10-13
 */
@Slf4j
public class PlusRequestLimiterAutoConfiguration {

    //
    // IAM rate limiter configuration.
    //

    @Bean
    @ConfigurationProperties(prefix = GatewayPlusConstants.CONF_PREFIX_REQUESTLIMIT)
    public PlusRequestLimiterProperties iamRequestLimiterProperties() {
        return new PlusRequestLimiterProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public LimiterStrategyConfigurer redisLimiterStrategyConfigurer() {
        return new RedisLimiterStrategyConfigurer();
    }

    //
    // IAM Key resolver.
    //

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> hostIamKeyResolver() {
        return new HostPlusKeyResolver();
    }

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> ipRangeIamKeyResolver() {
        return new IpRangePlusKeyResolver();
    }

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> headerIamKeyResolver() {
        return new HeaderPlusKeyResolver();
    }

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> pathIamKeyResolver() {
        return new PathPlusKeyResolver();
    }

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> principalNameIamKeyResolver() {
        return new PrincipalPlusKeyResolver();
    }

    @Bean
    public PlusKeyResolver<? extends KeyResolverStrategy> intervalIamKeyResolver() {
        return new IntervalPlusKeyResolver();
    }

    @Bean
    public GenericOperatorAdapter<KeyResolverProvider, PlusKeyResolver<? extends KeyResolverStrategy>> iamKeyResolverAdapter(
            List<PlusKeyResolver<? extends KeyResolverStrategy>> resolvers) {
        return new GenericOperatorAdapter<KeyResolverProvider, PlusKeyResolver<? extends KeyResolverStrategy>>(resolvers) {
        };
    }

    //
    // IAM request limiter.
    //

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public RedisRateLimiter warningDeprecatedRedisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            ConfigurationService configurationService) {
        return new WarningDeprecatedRedisRateLimiter(redisTemplate, redisScript, configurationService);
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public PlusRequestLimiter redisRatePlusRequestLimiter(
            RedisScript<List<Long>> redisScript,
            PlusRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            GatewayPlusMetricsFacade metricsFacade) {
        return new RedisRatePlusRequestLimiter(redisScript, requestLimiterConfig, configurer, redisTemplate, eventBus,
                metricsFacade);
    }

    @Bean
    public PlusRequestLimiter redisQuotaPlusRequestLimiter(
            PlusRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            GatewayPlusMetricsFacade metricsFacade) {
        return new RedisQuotaPlusRequestLimiter(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
    }

    @Bean
    public GenericOperatorAdapter<RequestLimiterPrivoder, PlusRequestLimiter> iamRequestLimiterAdapter(
            List<PlusRequestLimiter> rqeuestLimiters) {
        return new GenericOperatorAdapter<RequestLimiterPrivoder, PlusRequestLimiter>(rqeuestLimiters) {
        };
    }

    /**
     * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#requestRateLimiterGatewayFilterFactory}
     */
    @Bean
    public PlusRequestLimiterFilterFactory iamRequestLimiterFilterFactory(
            PlusRequestLimiterProperties requsetLimiterConfig,
            GenericOperatorAdapter<KeyResolverProvider, PlusKeyResolver<? extends KeyResolverStrategy>> keyResolverAdapter,
            GenericOperatorAdapter<RequestLimiterPrivoder, PlusRequestLimiter> requestLimiterAdapter) {
        return new PlusRequestLimiterFilterFactory(requsetLimiterConfig, keyResolverAdapter, requestLimiterAdapter);
    }

    //
    // IAM limiter event.
    //

    @Bean(name = BEAN_REDIS_RATELIMITE_EVENTBUS, destroyMethod = "close")
    public EventBusSupport redisRateLimiteEventBusSupport(PlusRequestLimiterProperties requestLimiteConfig) {
        return new EventBusSupport(requestLimiteConfig.getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public DefaultRedisRequestLimitEventRecorder redisRateLimiteEventRecoder(
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus) {
        DefaultRedisRequestLimitEventRecorder recorder = new DefaultRedisRequestLimitEventRecorder();
        eventBus.register(recorder);
        return recorder;
    }

    class WarningDeprecatedRedisRateLimiter extends RedisRateLimiter implements ApplicationRunner {
        private @Lazy @Autowired RouteDefinitionLocator routeLocator;

        public WarningDeprecatedRedisRateLimiter(ReactiveStringRedisTemplate redisTemplate, RedisScript<List<Long>> script,
                ConfigurationService configurationService) {
            super(redisTemplate, script, configurationService);
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
            boolean useDefaultRedisRateLimiter = routeLocator.getRouteDefinitions().collectList().block().stream().anyMatch(
                    r -> safeList(r.getFilters()).stream().anyMatch(f -> StringUtils.equals(f.getName(), "RequestRateLimiter")));
            if (useDefaultRedisRateLimiter) {
                log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            }
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            return Mono.empty(); // Ignore
        }
    }

    public static final String BEAN_REDIS_RATELIMITE_EVENTBUS = "redisRateLimiteEventBusSupport";
    public static final String LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER = "\n[WARNING]: The default redisRateLimiter is deprecated, please use the IAM rate limiter with the configuration key prefix: 'spring.iam.gateway.ratelimit'\n";

}
