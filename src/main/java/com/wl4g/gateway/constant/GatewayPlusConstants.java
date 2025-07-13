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

package com.wl4g.gateway.constant;

import com.wl4g.infra.common.lang.EnvironmentUtil;

/**
 * The constants of gateway.
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2018/11/13
 */
public abstract class GatewayPlusConstants extends EnvironmentUtil {

    public static final String CONF_PREFIX = "spring.gateway-plus";

    //
    // (Static) configuration properties prefix definitions.
    //

    public static final String CONF_PREFIX_SERVER = CONF_PREFIX + ".server";
    public static final String CONF_PREFIX_IPFILTER = CONF_PREFIX + ".ipfilter";
    public static final String CONF_PREFIX_REQUESTSIZE = CONF_PREFIX + ".requestsize";
    public static final String CONF_PREFIX_FAULT = CONF_PREFIX + ".fault";
    public static final String CONF_PREFIX_SECURITY = CONF_PREFIX + ".security";
    public static final String CONF_PREFIX_TRACE = CONF_PREFIX + ".trace";
    public static final String CONF_PREFIX_LOGGING = CONF_PREFIX + ".logging";
    public static final String CONF_PREFIX_REQUESTLIMIT = CONF_PREFIX + ".requestlimit";
    public static final String CONF_PREFIX_ROUTE = CONF_PREFIX + ".route";
    public static final String CONF_PREFIX_RETRY = CONF_PREFIX + ".retry";
    public static final String CONF_PREFIX_CIRCUITBREAKER = CONF_PREFIX + ".circuitbreaker";
    public static final String CONF_PREFIX_LOADBANANER = CONF_PREFIX + ".loadbalancer";
    public static final String CONF_PREFIX_RESPONSECACHE = CONF_PREFIX + ".responsecache";
    public static final String CONF_PREFIX_TRAFFIC = CONF_PREFIX + ".traffic";

    //
    // (Dynamic) configuration cache prefix definitions.
    //

    public static final String CACHE_PREFIX = "gateway:plus";

    public static final String CACHE_PREFIX_IPFILTER = CACHE_PREFIX + ":ipfilter";

    public static final String CACHE_PREFIX_ROUTES = CACHE_PREFIX + ":routes";

    public static final String CACHE_PREFIX_AUTH = CACHE_PREFIX + ":auth";
    public static final String CACHE_PREFIX_AUTH_SIGN_SECRET = CACHE_PREFIX_AUTH + ":sign:secret";
    public static final String CACHE_PREFIX_AUTH_SIGN_REPLAY_BLOOM = CACHE_PREFIX_AUTH + ":sign:replay:bloom";
    public static final String CACHE_PREFIX_AUTH_SIGN_EVENT_SUCCESS = CACHE_PREFIX_AUTH + ":sign:event:success";
    public static final String CACHE_PREFIX_AUTH_SIGN_EVENT_FAILURE = CACHE_PREFIX_AUTH + ":sign:event:failure";

    public static final String CACHE_PREFIX_REQUESTLIMIT = CACHE_PREFIX + ":requestlimit";
    public static final String CACHE_PREFIX_REQUESTLIMIT_CONF_RATE = CACHE_PREFIX_REQUESTLIMIT + ":config:rate";
    public static final String CACHE_PREFIX_REQUESTLIMIT_CONF_QUOTA = CACHE_PREFIX_REQUESTLIMIT + ":config:quota";
    public static final String CACHE_PREFIX_REQUESTLIMIT_TOKEN_RATE = CACHE_PREFIX_REQUESTLIMIT + ":token:rate";
    public static final String CACHE_PREFIX_REQUESTLIMIT_TOKEN_QUOTA = CACHE_PREFIX_REQUESTLIMIT + ":token:quota";
    public static final String CACHE_PREFIX_REQUESTLIMIT_EVENT_HITS_RATE = CACHE_PREFIX_REQUESTLIMIT
            + ":event:hits:rate";
    public static final String CACHE_PREFIX_REQUESTLIMIT_EVENT_HITS_QUOTA = CACHE_PREFIX_REQUESTLIMIT
            + ":event:hits:quota";

    public static final String CACHE_SUFFIX_IAM_GATEWAY_RESPONSECACHE = CACHE_PREFIX + ":responsecache:data";

    public static final String CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYMMDD = "yyMMdd";

}