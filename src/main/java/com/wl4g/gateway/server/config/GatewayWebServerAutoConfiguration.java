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
package com.wl4g.gateway.server.config;

import static com.wl4g.gateway.constant.GatewayPlusConstants.CONF_PREFIX_SERVER;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.ServerCodecConfigurer;

import com.wl4g.gateway.server.SecureNettyReactiveWebServerFactory;

/**
 * {@link GatewayWebServerAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2021-09-10
 * @see {@link org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryConfiguration.EmbeddedNetty}
 */
public class GatewayWebServerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_SERVER)
    public GatewayWebServerProperties gatewayWebServerProperties() {
        return new GatewayWebServerProperties();
    }

    /**
     * @see {@link org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext#createWebServer()}
     */
    @Bean
    public SecureNettyReactiveWebServerFactory secureNettyReactiveWebServerFactory() {
        return new SecureNettyReactiveWebServerFactory();
    }

    /**
     * @see {@link org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryConfiguration.EmbeddedNetty#reactorServerResourceFactory()}
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactorResourceFactory reactorServerResourceFactory() {
        return new ReactorResourceFactory();
    }

    @Bean
    public ServerCodecConfigurer serverCodecConfigurer() {
        return ServerCodecConfigurer.create();
    }

    // @Bean
    // public WebServerFactoryCustomizer customWebServerFactoryCustomizer(
    // Environment environment,
    // ServerProperties serverProperties) {
    // return new WebServerFactoryCustomizer(environment,
    // serverProperties){};
    // }

}
