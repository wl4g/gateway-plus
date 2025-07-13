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
package com.wl4g.gateway.requestlimit.limiter;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;
import com.wl4g.gateway.requestlimit.config.PlusRequestLimiterProperties;
import com.wl4g.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link AbstractRedisPlusRequestLimiter}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-04-21
 */
public abstract class AbstractRedisPlusRequestLimiter<S extends RequestLimiterStrategy> implements PlusRequestLimiter {
    protected final SmartLogger log = getLogger(getClass());

    protected final PlusRequestLimiterProperties requestLimiterConfig;
    protected final LimiterStrategyConfigurer configurer;
    protected final ReactiveStringRedisTemplate redisTemplate;
    protected final EventBusSupport eventBus;
    protected final GatewayPlusMetricsFacade metricsFacade;

    public AbstractRedisPlusRequestLimiter(PlusRequestLimiterProperties requestLimiterConfig, LimiterStrategyConfigurer configurer,
                                          ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus, GatewayPlusMetricsFacade metricsFacade) {
        this.requestLimiterConfig = notNullOf(requestLimiterConfig, "requestLimiterConfig");
        this.configurer = notNullOf(configurer, "configurer");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.eventBus = notNullOf(eventBus, "eventBus");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    }

}
