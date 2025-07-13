package com.wl4g.gateway.security.ram.policy;

import com.wl4g.gateway.security.ram.RamRequestContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * The {@link SimpleUserPolicyLoader}
 *
 * @author James Wong
 * @version v1.0 Sunday
 * @since v1.0
 */
@Slf4j
@AllArgsConstructor
public class SimpleUserPolicyLoader implements IUserPolicyLoader {
    private final UserPolicyProperties policyConfig;

    @Override
    public List<RamRequestContext.PolicyStatement> loadCombinePolicy(String uid, String gid) {
        log.debug("Loading policy for uid: {}, gid: {}", uid, gid);
        return policyConfig.getUserPolicies().stream()
                .filter(p -> p.getUid().equals(uid) && p.getGid().equals(gid))
                .findFirst()
                .map(UserPolicyStatement::getStatements)
                .orElse(List.of());
    }

    @Data
    public static class UserPolicyProperties {
        private List<UserPolicyStatement> userPolicies;
    }

    @Data
    public static class UserPolicyStatement {
        private String uid;
        private String gid;
        private List<RamRequestContext.PolicyStatement> statements;
    }
}
