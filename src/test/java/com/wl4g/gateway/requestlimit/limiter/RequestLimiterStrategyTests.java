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
package com.wl4g.gateway.requestlimit.limiter;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import org.junit.Test;

import com.wl4g.gateway.requestlimit.key.KeyStrategyTests;
import com.wl4g.gateway.requestlimit.limiter.rate.RedisRateRequestLimiterStrategy;

/**
 * {@link KeyStrategyTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-04-26
 */
public class RequestLimiterStrategyTests {

    @Test
    public void testLimiterStrategySerialToJson() {
        RedisRateRequestLimiterStrategy strategy = new RedisRateRequestLimiterStrategy(100, 20, 1);
        System.out.println(toJSONString(strategy));
    }

    @Test
    public void testLimiterStrategyParseFromJson() {
        String json = "{\"provider\":\"RedisRateLimiter\",\"includeHeaders\":true,\"burstCapacity\":100,\"replenishRate\":20,\"requestedTokens\":1}";
        RequestLimiterStrategy strategy = parseJSON(json, RequestLimiterStrategy.class);
        System.out.println(strategy);
    }

}
