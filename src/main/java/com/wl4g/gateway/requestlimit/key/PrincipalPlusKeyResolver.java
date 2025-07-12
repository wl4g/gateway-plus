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

import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.requestlimit.config.PlusRequestLimiterProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link PrincipalPlusKeyResolver}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2022-04-20
 * @see {@link org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver}
 */
public class PrincipalPlusKeyResolver extends AbstractPlusKeyResolver<PrincipalPlusKeyResolver.PrincipalKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.Principal;
    }

    /**
     * {@link com.wl4g.gateway.security.sign.SimpleSignAuthingFilterFactory#bindSignedToContext()}
     */
    @Override
    public Mono<String> resolve(PrincipalKeyResolverStrategy strategy, ServerWebExchange exchange) {
        return exchange.getPrincipal().flatMap(p -> Mono.justOrEmpty(p.getName()));
    }

    @Getter
    @Setter
    @ToString
    @Validated
    public static class PrincipalKeyResolverStrategy extends PlusKeyResolver.KeyResolverStrategy {
        @Override
        public void applyDefaultIfNecessary(PlusRequestLimiterProperties config) {
            // Ignore
        }
    }

}
