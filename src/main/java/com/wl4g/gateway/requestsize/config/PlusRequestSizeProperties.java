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
package com.wl4g.gateway.requestsize.config;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNull;

import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link PlusRequestSizeProperties}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @since v1.0 2022-05-16
 */
@Getter
@Setter
@Validated
@ToString
public class PlusRequestSizeProperties {

    private RequestSizeProperties requestSize = new RequestSizeProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class RequestSizeProperties {

        private DataSize maxBodySize = DataSize.ofBytes(5000000L);

        public RequestSizeProperties validate() {
            notNull(getMaxBodySize(), "maxBodySize may not be null");
            isTrue(getMaxBodySize().toBytes() > 0, "maxBodySize must be greater than 0");
            return this;
        }

    }

}
