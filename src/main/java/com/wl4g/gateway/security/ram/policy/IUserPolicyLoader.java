package com.wl4g.gateway.security.ram.policy;

import com.wl4g.gateway.security.ram.RamRequestContext;

import java.util.List;

/**
 * The {@link IUserPolicyLoader}
 *
 * @author James Wong
 * @version v1.0 Sunday
 * @since v1.0
 */
public interface IUserPolicyLoader {
    /**
     * Loads and combines policies for both user and user group
     *
     * @param uid User ID from request header
     * @param gid User Group ID from request header
     * @return Combined list of policy statements
     */
    List<RamRequestContext.PolicyStatement> loadCombinePolicy(String uid, String gid);
}
