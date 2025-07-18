package com.wl4g.gateway.route.config;

import com.wl4g.gateway.route.Https2HttpGlobalFilter;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link IamSecurityProperties}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-07-23
 * @since
 */
@Getter
@Setter
public class RouteProperties {

    /**
     * Enabled to https to http forward filter. {@link Https2HttpGlobalFilter}
     */
    private boolean forwardHttpsToHttp = true;

    private Long refreshDelayMs = 30_000L;

}
