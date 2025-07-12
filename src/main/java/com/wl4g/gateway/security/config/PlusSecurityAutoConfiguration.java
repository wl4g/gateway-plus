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
package com.wl4g.gateway.security.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.wl4g.gateway.constant.GatewayPlusConstants;
import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;
import com.wl4g.gateway.security.sign.SimpleSignAuthingFilterFactory;
import com.wl4g.gateway.security.sign.event.DefaultRedisSignAuthingEventRecoder;
import com.wl4g.infra.common.eventbus.EventBusSupport;

/**
 * {@link PlusSecurityAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2021-09-01
 */
public class PlusSecurityAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayPlusConstants.CONF_PREFIX_SECURITY)
    public PlusSecurityProperties iamSecurityProperties() {
        return new PlusSecurityProperties();
    }

    // Simple signature authorizer configuration.

    @Bean(name = BEAN_SIMPLE_SIGN_EVENTBUS, destroyMethod = "close")
    public EventBusSupport simpleSignAuthingEventBusSupport(PlusSecurityProperties authingConfig) {
        return new EventBusSupport(authingConfig.getSimpleSign().getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public SimpleSignAuthingFilterFactory simpleSignAuthingFilterFactory(
            PlusSecurityProperties authingConfig,
            StringRedisTemplate stringTemplate,
            GatewayPlusMetricsFacade metricsFacade,
            @Qualifier(BEAN_SIMPLE_SIGN_EVENTBUS) EventBusSupport eventBus) {
        return new SimpleSignAuthingFilterFactory(authingConfig, stringTemplate, metricsFacade, eventBus);
    }

    // Simple signature authorizer event recorder

    @Bean
    public DefaultRedisSignAuthingEventRecoder defaultRedisSignAuthingEventRecoder(
            @Qualifier(BEAN_SIMPLE_SIGN_EVENTBUS) EventBusSupport eventBus) {
        DefaultRedisSignAuthingEventRecoder recorder = new DefaultRedisSignAuthingEventRecoder();
        eventBus.register(recorder);
        return recorder;
    }

    // Oauth2 authorizer configuration.

    // @Bean
    // public TokenRelayRefreshFilter
    // tokenRelayRefreshGatewayFilter(
    // ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
    // ReactiveClientRegistrationRepository clientRegistrationRepository) {
    // return new
    // TokenRelayRefreshFilterFactory(authorizedClientRepository,
    // clientRegistrationRepository);
    // }

    public static final String BEAN_SIMPLE_SIGN_EVENTBUS = "simpleSignAuthingEventBusSupport";

}
