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

package com.wl4g.gateway.circuitbreaker.config;

import com.wl4g.gateway.circuitbreaker.DefaultCircuitBreakerCustomizer;
import com.wl4g.gateway.circuitbreaker.Resilience4JGatewayPlusCircuitBreakerFilterFactory;
import com.wl4g.gateway.circuitbreaker.GatewayPlusCircuitBreakerFilterFactory;
import com.wl4g.gateway.constant.GatewayPlusConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.DispatcherHandler;

/**
 * {@link PlusCircuitBreakerAutoConfiguration}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @see <a href="https://cloud.spring.io/spring-cloud-circuitbreaker/reference/html/spring-cloud-circuitbreaker.html#auto-configuration">...</a>
 * @see <a href="https://resilience4j.readme.io/docs/examples">resilience4j docs</a>
 * @since v1.0.0
 */
public class PlusCircuitBreakerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayPlusConstants.CONF_PREFIX_CIRCUITBREAKER)
    public PlusCircuitBreakerProperties plusCircuitBreakerProperties() {
        return new PlusCircuitBreakerProperties();
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public GatewayPlusCircuitBreakerFilterFactory Resilience4JPlusSpringCloudCircuitBreakerFilterFactory(
            ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory,
            ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        return new Resilience4JGatewayPlusCircuitBreakerFilterFactory(reactiveCircuitBreakerFactory,
                dispatcherHandlerProvider);
    }

    @Bean
    public DefaultCircuitBreakerCustomizer defaultCircuitBreakerCustomizer() {
        return new DefaultCircuitBreakerCustomizer();
    }

    /**
     * {@link org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory#configureDefault(java.util.function.Function)}
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultReactiveResilience4JCircuitBreakerCustomizer(
            PlusCircuitBreakerProperties circuitBreakerConfig,
            DefaultCircuitBreakerCustomizer customizer) {
        return factory -> {
            factory.configureDefault(
                    id -> new Resilience4JConfigBuilder(id).circuitBreakerConfig(circuitBreakerConfig.toCircuitBreakerConfig())
                                                           .timeLimiterConfig(circuitBreakerConfig.toTimeLimiterConfig())
                                                           .build());
            factory.addCircuitBreakerCustomizer(customizer, customizer.getClass().getSimpleName());
        };
    }

}
