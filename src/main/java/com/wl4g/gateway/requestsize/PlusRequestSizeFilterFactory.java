/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.gateway.requestsize;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.requestsize.config.PlusRequestSizeProperties;
import com.wl4g.gateway.requestsize.config.PlusRequestSizeProperties.RequestSizeProperties;
import com.wl4g.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.infra.common.bean.ConfigBeanUtils;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link PlusRequestSizeFilterFactory}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @date 2022-05-16 v1.0.0
 * @see {@link org.springframework.cloud.gateway.filter.factory.RequestSizeGatewayFilterFactory}
 * @since v1.0.0
 */
public class PlusRequestSizeFilterFactory extends AbstractGatewayFilterFactory<PlusRequestSizeFilterFactory.Config> {

    private static final String PREFIX = "kMGTPE";
    private static final String ERROR = "Request size is larger than permissible limit."
            + " Request size is %s where permissible limit is %s";

    private final PlusRequestSizeProperties requestSizeConfig;

    public PlusRequestSizeFilterFactory(PlusRequestSizeProperties requestSizeConfig) {
        super(PlusRequestSizeFilterFactory.Config.class);
        this.requestSizeConfig = notNullOf(requestSizeConfig, "requestSizeConfig");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private static String getErrorMessage(Long currentRequestSize, Long maxSize) {
        return String.format(ERROR, getReadableByteCount(currentRequestSize), getReadableByteCount(maxSize));
    }

    private static String getReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = Character.toString(PREFIX.charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public GatewayFilter apply(PlusRequestSizeFilterFactory.Config config) {
        applyDefaultToConfig(config);
        config.validate();
        return new GatewayPlusRequestSizeGatewayFilter(config);
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new Config(), config, requestSizeConfig.getRequestSize());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Config extends RequestSizeProperties {
    }

    @AllArgsConstructor
    class GatewayPlusRequestSizeGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_REQUEST_SIZE;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String contentLength = request.getHeaders().getFirst("Content-Length");
            if (!ObjectUtils.isEmpty(contentLength)) {
                long currentRequestSize = Long.parseLong(contentLength);
                if (currentRequestSize > config.getMaxBodySize().toBytes()) {
                    exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                    if (!exchange.getResponse().isCommitted()) {
                        exchange.getResponse().getHeaders().add("errorMessage",
                                getErrorMessage(currentRequestSize, config.getMaxBodySize().toBytes()));
                    }
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        }

        @Override
        public String toString() {
            return filterToStringCreator(PlusRequestSizeFilterFactory.this).append("max", config.getMaxBodySize()).toString();
        }

    }

    public static final String BEAN_NAME = "PlusRequestSize";

}
