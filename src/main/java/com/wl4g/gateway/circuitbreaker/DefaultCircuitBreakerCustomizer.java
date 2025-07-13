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
package com.wl4g.gateway.circuitbreaker;

import org.springframework.cloud.client.circuitbreaker.Customizer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link DefaultCircuitBreakerCustomizer}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-04-16
 */
@Slf4j
public class DefaultCircuitBreakerCustomizer implements Customizer<CircuitBreaker> {

    @Override
    public void customize(CircuitBreaker circuitBreaker) {
        // TODO
        circuitBreaker.getEventPublisher().onSuccess(event -> {
            log.debug("CircuitBreaker success: ", event);
        }).onError(event -> {
            log.warn("CircuitBreaker error: ", event);
        }).onStateTransition(event -> {
            log.warn("CircuitBreaker stateTransition: ", event);
        }).onReset(event -> {
            log.warn("CircuitBreaker reset: ", event);
        }).onIgnoredError(event -> {
            log.warn("CircuitBreaker ignore error: ", event);
        }).onFailureRateExceeded(event -> {
            log.warn("CircuitBreaker failureRateExceeded: ", event);
        }).onCallNotPermitted(event -> {
            log.warn("CircuitBreaker callNotPermitted: ", event);
        }).onSlowCallRateExceeded(event -> {
            log.warn("CircuitBreaker slowCallRateExceeded: ", event);
        });
    }

}
