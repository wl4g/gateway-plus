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
package com.wl4g.gateway.requestlimit.configurer;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.gateway.requestlimit.config.PlusRequestLimiterProperties;
import com.wl4g.gateway.requestlimit.limiter.quota.RedisQuotaRequestLimiterStrategy;
import com.wl4g.gateway.requestlimit.limiter.rate.RedisRateRequestLimiterStrategy;

import reactor.core.publisher.Mono;

/**
 * {@link RedisLimiterStrategyConfigurer}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2022-04-20
 */
public class RedisLimiterStrategyConfigurer implements LimiterStrategyConfigurer {

    private @Autowired PlusRequestLimiterProperties requestLimitConfig;
    private @Autowired ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<RedisRateRequestLimiterStrategy> loadRateStrategy(@NotBlank String routeId, @NotBlank String limitKey) {
        String prefix = requestLimitConfig.getLimiter().getRate().getConfigPrefix();
        String configKey = LimiterStrategyConfigurer.getConfigKey(routeId, limitKey);
        return getOperation().get(prefix, configKey).map(json -> parseJSON(json, RedisRateRequestLimiterStrategy.class));
    }

    @Override
    public Mono<RedisQuotaRequestLimiterStrategy> loadQuotaStrategy(@NotBlank String routeId, @NotBlank String limitKey) {
        String prefix = requestLimitConfig.getLimiter().getQuota().getConfigPrefix();
        String configKey = LimiterStrategyConfigurer.getConfigKey(routeId, limitKey);
        return getOperation().get(prefix, configKey).map(json -> parseJSON(json, RedisQuotaRequestLimiterStrategy.class));
    }

    private ReactiveHashOperations<String, String, String> getOperation() {
        return redisTemplate.opsForHash();
    }

}
