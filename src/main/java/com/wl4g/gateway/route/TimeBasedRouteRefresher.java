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
package com.wl4g.gateway.route;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ScheduledFuture;

import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;

import com.wl4g.infra.common.task.RunnerProperties;
import com.wl4g.infra.context.task.ApplicationTaskRunner;
import com.wl4g.gateway.route.config.RouteProperties;

/**
 * Gateway configuration refresh scheduler coordinator.
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2019/6/2
 * @since v1.0
 */
public class TimeBasedRouteRefresher extends ApplicationTaskRunner<RunnerProperties>
        implements ApplicationRunner, DisposableBean {

    private @Autowired RouteProperties config;
    @Getter
    private @Autowired IRouteCacheRefresher refresher;

    /** Refreshing scheduled future */
    protected ScheduledFuture<?> future;

    public TimeBasedRouteRefresher() {
        super(new RunnerProperties().withConcurrency(1));
    }

    @Override
    public void destroy() throws Exception {
        super.close();
    }

    /**
     * Starting configuration refresher schedule.
     */
    @Override
    public void run() {
        refreshRouters();
    }

    /**
     * Restarting configuration refresher schedule.
     */
    public void restartRefresher() {
        isTrue(future.cancel(false), () -> {
            throw new CurrentlyInRefreshingException(
                    "Updating refreshDelayMs failed, because refreshing is currently in progressing");
        });

        refreshRouters();
    }

    /**
     * Creating or refreshing routers.
     */
    private void refreshRouters() throws IllegalStateException {
        if (nonNull(future) && !future.isDone()) {
            throw new IllegalStateException(format("Cannot refresh routes, because already refreshing task. %s", future));
        }
        this.future = getWorker().scheduleWithFixedDelay(() -> {
            try {
                log.debug("Refreshing routes ...");
                refresher.refreshRoutes();
            } catch (Exception e) {
                log.error("Failed to refreshing routes.", e);
            }
        }, 1_000L, config.getRefreshDelayMs(), MILLISECONDS);
    }

}