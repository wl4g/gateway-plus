/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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
package com.wl4g.gateway.fault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.gateway.constant.GatewayPlusConstants;
import com.wl4g.gateway.fault.PlusFaultInjectorFilterFactory;
import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;

/**
 * {@link FaultAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @date 2022-04-27 v1.0.0
 * @since v1.0.0
 */
public class FaultAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayPlusConstants.CONF_PREFIX_FAULT)
    public FaultProperties faultProperties() {
        return new FaultProperties();
    }

    @Bean
    public PlusFaultInjectorFilterFactory faultInjectorFilterFactory(
            FaultProperties faultConfig,
            GatewayPlusMetricsFacade metricsFacade) {
        return new PlusFaultInjectorFilterFactory(faultConfig, metricsFacade);
    }

}