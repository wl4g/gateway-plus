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

import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.requestlimit.config.PlusRequestLimiterProperties;
import com.wl4g.gateway.requestlimit.key.HeaderPlusKeyResolver.HeaderKeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.HostPlusKeyResolver.HostKeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.PlusKeyResolver.KeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.IntervalPlusKeyResolver.IntervalKeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.IpRangePlusKeyResolver.IpRangeKeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.PathPlusKeyResolver.PathKeyResolverStrategy;
import com.wl4g.gateway.requestlimit.key.PrincipalPlusKeyResolver.PrincipalKeyResolverStrategy;
import com.wl4g.infra.common.framework.operator.Operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * {@link PlusKeyResolver}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-04-20
 */
public interface PlusKeyResolver<C extends KeyResolverStrategy> extends Operator<PlusKeyResolver.KeyResolverProvider> {

    Mono<String> resolve(C strategy, ServerWebExchange exchange);

    @Getter
    @AllArgsConstructor
    public static enum KeyResolverProvider {
        Host(HostKeyResolverStrategy.class),

        IpRange(IpRangeKeyResolverStrategy.class),

        Principal(PrincipalKeyResolverStrategy.class),

        Path(PathKeyResolverStrategy.class),

        Header(HeaderKeyResolverStrategy.class),

        Interval(IntervalKeyResolverStrategy.class);

        private final Class<? extends KeyResolverStrategy> strategyClass;
    }

    // @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include =
    // JsonTypeInfo.As.PROPERTY, property = "privoder")
    // @JsonSubTypes({ @Type(value = HostKeyResolverStrategy.class,name="Host"),
    // @Type(value = PrincipalKeyResolverStrategy.class, name = "Principal"),
    // @Type(value = PathKeyResolverStrategy.class, name = "Path"),
    // @Type(value = HeaderKeyResolverStrategy.class, name = "Header"),
    // @Type(value = IntervalKeyResolverStrategy.class, name = "Interval"),
    // @Type(value = IpRangeKeyResolverStrategy.class, name = "IpRange")})
    public static abstract class KeyResolverStrategy {
        public void applyDefaultIfNecessary(PlusRequestLimiterProperties config) {
        }
    }

}
