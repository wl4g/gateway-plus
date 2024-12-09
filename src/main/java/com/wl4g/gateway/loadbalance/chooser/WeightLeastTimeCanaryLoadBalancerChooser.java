package com.wl4g.gateway.loadbalance.chooser;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats;

/**
 * Grayscale load balancer rule for weight-based least response time.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @date 2021-09-03 v1.0.0
 * @since v1.0.0
 * @see {@link com.netflix.loadbalancer.WeightedResponseTimeRule}
 */
public class WeightLeastTimeCanaryLoadBalancerChooser extends LeastConnCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.WLT;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // TODO
        return super.doChooseInstance(config, exchange, stats, serviceId, candidateInstances);
    }

}