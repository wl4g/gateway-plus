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
package com.wl4g.gateway.metrics.config;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * {@link GatewayPlusMetricsAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2022-04-16
 */
public class GatewayPlusMetricsAutoConfiguration {

    @Bean
    public GatewayPlusMetricsFacade iamGatewayMetricsFacade(
            PrometheusMeterRegistry meterRegistry,
            InetUtils inet,
            Environment environment) {
        return new GatewayPlusMetricsFacade(meterRegistry, inet, environment);
    }

}
