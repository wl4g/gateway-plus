package com.wl4g.gateway.security.ram.policy;

import com.wl4g.gateway.security.ram.RamRequestContext;

import java.util.List;

/**
 * The {@link JdbcUserPolicyLoader}
 *
 * @author James Wong
 * @version v1.0 Sunday
 * @since v1.0
 */
public class JdbcUserPolicyLoader implements IUserPolicyLoader {
    @Override
    public List<RamRequestContext.PolicyStatement> loadCombinePolicy(String uid, String gid) {
        // TODO: Implement actual policy loading logic from storage (e.g. PG)
        throw new UnsupportedOperationException("Not implemented");
    }
}
