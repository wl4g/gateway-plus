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
package com.wl4g.iam.gateway.ipfilter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.ipfilter.IpSubnetFilterFactory;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer;
import com.wl4g.iam.gateway.ipfilter.configurer.RedisIpFilterConfigurer;
import com.wl4g.iam.gateway.metrics.GatewayPlusMetricsFacade;

/**
 * {@link IpFilterAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-05-05
 */
public class IpFilterAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IPFILTER)
    public IpFilterProperties ipFilterProperties() {
        return new IpFilterProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public IpFilterConfigurer redisIpListConfigurer() {
        return new RedisIpFilterConfigurer();
    }

    @Bean
    public IpSubnetFilterFactory ipSubnetFilterFactory(
            IpFilterProperties ipListConfig,
            IpFilterConfigurer configurer,
            GatewayPlusMetricsFacade metricsFacade) {
        return new IpSubnetFilterFactory(ipListConfig, configurer, metricsFacade);
    }

}
