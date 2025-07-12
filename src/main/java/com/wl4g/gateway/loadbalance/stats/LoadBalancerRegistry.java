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
package com.wl4g.gateway.loadbalance.stats;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.gateway.loadbalance.stats.LoadBalancerStats.RouteServiceStatus;

/**
 * {@link LoadBalancerRegistry}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0.0 2021-09-15
 */
public interface LoadBalancerRegistry {

    void register(
            @NotBlank String routeId,
            @NotNull CanaryLoadBalancerFilterFactory.Config config,
            @NotNull InstanceStatus instance);

    void update(@NotBlank String routeId, @NotNull RouteServiceStatus routeService, boolean safeCheck);

    @NotNull
    Map<String, RouteServiceStatus> getAllRouteServices();

    @NotNull
    RouteServiceStatus getRouteService(@NotBlank String routeId, boolean required);

}
