package com.wl4g.gateway.loadbalance.chooser;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.infra.common.framework.operator.Operator;

/**
 * {@link CanaryLoadBalancerChooser}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2021-09-03
 */
public interface CanaryLoadBalancerChooser extends Operator<CanaryLoadBalancerChooser.LoadBalancerAlgorithm> {

    ServiceInstance choose(CanaryLoadBalancerFilterFactory.Config config, ServerWebExchange exchange, String serviceId);

    /**
     * see:https://www.cnblogs.com/pengpengboshi/p/13278440.html
     */
    public static enum LoadBalancerAlgorithm {
        R, RR, WR, WRR, DH, SH, LC, LT, WLC, WLT
    }

}
