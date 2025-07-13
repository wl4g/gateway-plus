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
package com.wl4g.gateway.loadbalance.config;

import static com.wl4g.gateway.constant.GatewayPlusConstants.CONF_PREFIX_LOADBANANER;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.gateway.loadbalance.chooser.CanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.CanaryLoadBalancerChooser.LoadBalancerAlgorithm;
import com.wl4g.gateway.loadbalance.chooser.DestinationCanaryHashLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.LeastConnCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.LeastTimeCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.RandomCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.RoundRobinCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.SourceHashCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.WeightLeastConnCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.WeightLeastTimeCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.WeightRandomCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.chooser.WeightRoundRobinCanaryLoadBalancerChooser;
import com.wl4g.gateway.loadbalance.metrics.CanaryLoadBalancerCollector;
import com.wl4g.gateway.loadbalance.stats.DefaultLoadBalancerStats;
import com.wl4g.gateway.loadbalance.stats.InMemoryLoadBalancerRegistry;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerRegistry;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats;
import com.wl4g.gateway.loadbalance.stats.ReachableStrategy;
import com.wl4g.gateway.loadbalance.stats.ReachableStrategy.DefaultLatestReachableStrategy;
import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade;
import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;
import com.wl4g.infra.context.web.matcher.SpelRequestMatcher;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

/**
 * {@link CanaryLoadbalanceAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2021-10-13
 */
public class CanaryLoadbalanceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_LOADBANANER)
    public CanaryLoadBalancerProperties canaryLoadBalancerProperties() {
        return new CanaryLoadBalancerProperties();
    }

    // Load-balancer loadBalancerStats.

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerRegistry inMemoryLoadBalancerCache() {
        return new InMemoryLoadBalancerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReachableStrategy defaultReachableStrategy() {
        return new DefaultLatestReachableStrategy();
    }

    @Bean
    public LoadBalancerStats defaultLoadBalancerStats(CanaryLoadBalancerProperties loadBalancerConfig) {
        return new DefaultLoadBalancerStats(loadBalancerConfig);
    }

    // Load-balancer metrics.

    /**
     * @see {@link org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration}
     */
    public Collector canaryLoadBalancerCollector(CollectorRegistry registry) {
        CanaryLoadBalancerCollector collector = new CanaryLoadBalancerCollector();
        registry.register(collector);
        return collector;
    }

    // Load-balancer rules.

    @Bean(BEAN_CANARY_LB_REQUEST_MATCHER)
    public SpelRequestMatcher canaryLoadBalancerSpelRequestMatcher(CanaryLoadBalancerProperties loadBalancerConfig) {
        return new SpelRequestMatcher(loadBalancerConfig.getCanaryMatchRuleDefinitions());
    }

    @Bean
    public CanaryLoadBalancerChooser destinationHashCanaryLoadBalancerRule() {
        return new DestinationCanaryHashLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser leastConnCanaryLoadBalancerRule() {
        return new LeastConnCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser leastTimeCanaryLoadBalancerRule() {
        return new LeastTimeCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser randomCanaryLoadBalancerRule() {
        return new RandomCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser roundRobinCanaryLoadBalancerRule() {
        return new RoundRobinCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser sourceHashCanaryLoadBalancerRule() {
        return new SourceHashCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightRandomCanaryLoadBalancerRule() {
        return new WeightRandomCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightRoundRobinCanaryLoadBalancerRule() {
        return new WeightRoundRobinCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightLeastConnCanaryLoadBalancerRule() {
        return new WeightLeastConnCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightLeastTimeCanaryLoadBalancerRule() {
        return new WeightLeastTimeCanaryLoadBalancerChooser();
    }

    @Bean
    public GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> compositeCanaryLoadBalancerAdapter(
            List<CanaryLoadBalancerChooser> rules) {
        return new GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser>(rules) {
        };
    }

    // Load-balancer filters.

    @Bean
    public CanaryLoadBalancerFilterFactory canaryLoadBalancerFilterFactory(
            CanaryLoadBalancerProperties loadBalancerConfig,
            GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter,
            LoadBalancerStats loadBalancerStats,
            GatewayPlusMetricsFacade metricsFacade) {
        return new CanaryLoadBalancerFilterFactory(loadBalancerConfig, ruleAdapter, loadBalancerStats, metricsFacade);
    }

    public static final String BEAN_CANARY_LB_REQUEST_MATCHER = "canaryLoadBalancerSpelRequestMatcher";
    public static final String BEAN_CANARY_LB_STATS = "defaultCanaryLoadBalancerStats";

}
