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
package com.wl4g.gateway.requestlimit.key;

import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IntervalPlusKeyResolver}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2021-09-30
 */
public class IntervalPlusKeyResolver extends AbstractPlusKeyResolver<IntervalPlusKeyResolver.IntervalKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.Interval;
    }

    @Override
    public Mono<String> resolve(IntervalKeyResolverStrategy strategy, ServerWebExchange exchange) {
        return Mono.just(DateFormatUtils.format(currentTimeMillis(), strategy.getCycleDatePattern()));
    }

    // public void checkDatePattern() {
    // String datePattern =
    // limiterConfig.getRateLimitConfig().getDefaultIntervalKeyResolverDatePattern();
    // try {
    // DateFormatUtils.format(currentTimeMillis(), datePattern);
    // } catch (Exception e) {
    // throw new IllegalArgumentException(String.format("invalid date pattern. -
    // ", e.getMessage(), datePattern));
    // }
    // }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IntervalKeyResolverStrategy extends PlusKeyResolver.KeyResolverStrategy {

        /**
         * The date pattern of the key get by rate limiting according to the
         * date interval.
         */
        private String cycleDatePattern = "yyMMdd";
    }

}