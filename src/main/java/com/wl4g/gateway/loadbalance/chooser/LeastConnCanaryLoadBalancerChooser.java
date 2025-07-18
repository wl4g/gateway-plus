package com.wl4g.gateway.loadbalance.chooser;

import static java.util.Objects.isNull;

import java.util.Comparator;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.gateway.metrics.GatewayPlusMetricsFacade.MetricsName;

/**
 * Grayscale load balancer rule for based least connections. </br>
 * This algorithm will decide who to distribute the request to according to the
 * number of connections of the back-end RS. For example, if the number of RS1
 * connections is less than the number of RS2 connections, the request will be
 * sent to RS1 first. The problem here is that the session cannot be maintained,
 * that is, session sharing.
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2021-09-03
 */
public class LeastConnCanaryLoadBalancerChooser extends AbstractCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.LC;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        int count = 0;
        InstanceStatus chosenInstance = null;
        while (isNull(chosenInstance) || count++ < config.getChoose().getMaxChooseTries()) {
            List<InstanceStatus> allInstances = stats.getAllInstances(exchange);
            List<InstanceStatus> reachableInstances = stats.getReachableInstances(exchange);
            List<InstanceStatus> availableInstances = getAvailableInstances(reachableInstances, candidateInstances);

            int allCount = allInstances.size();
            int avaCount = availableInstances.size();

            if ((avaCount == 0) || (allCount == 0)) {
                log.warn("No up servers available from load balancer loadBalancerStats: {}", stats);
                return null;
            }

            chosenInstance = availableInstances.stream().min(DEFAULT).orElse(null);
            if (isNull(chosenInstance)) {
                // Give up the opportunity for short-term CPU to give other
                // threads execution, just like the sleep() method does not
                // release the lock.
                Thread.yield();
                continue;
            }

            if (LoadBalancerUtil.isAlive(config, chosenInstance.getStats())) {
                return chosenInstance.getInstance();
            }

            // Next.
            chosenInstance = null;
        }

        if (count >= config.getChoose().getMaxChooseTries()) {
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_MAX_TRIES_TOTAL, serviceId);
            log.warn("No available alive servers after {} tries from load balancer loadBalancerStats: {}", count, stats);
        }
        return null;
    }

    public static final Comparator<InstanceStatus> DEFAULT = (o1, o2) -> o1.getStats().getConnections().get()
            - o2.getStats().getConnections().get();

}