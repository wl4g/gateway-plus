package com.wl4g.gateway.security.config;

import com.wl4g.gateway.security.ram.policy.SimpleUserPolicyLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link GatewayPlusSecurityProperties}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2020-07-23
 */
@Getter
@Setter
@ToString
public class GatewayPlusSecurityProperties {
    private SimpleSignAuthingProperties simpleSign;
    private SimpleUserPolicyLoader.UserPolicyProperties simpleUserPolicies;
}
