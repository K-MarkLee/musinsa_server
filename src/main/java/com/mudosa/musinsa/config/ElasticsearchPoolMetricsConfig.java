package com.mudosa.musinsa.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.httpcomponents.PoolingHttpClientConnectionManagerMetricsBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ReflectionUtils;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class ElasticsearchPoolMetricsConfig {

    @Bean
    @SuppressWarnings("deprecation") // ES 8 REST client uses Apache HttpComponents 4.
    ApplicationRunner elasticsearchPoolMetrics(RestClient client, MeterRegistry registry) {
        return args -> {
            // ponytail: Apache 4.1.5 exposes no pool getter; read once for dev diagnostics.
            // Recheck this adapter/test when upgrading the client; never replace its SSL/connection setup.
            try {
                var httpClient = client.getHttpClient();
                var field = ReflectionUtils.findField(httpClient.getClass(), "connmgr");
                if (field == null) {
                    throw new IllegalStateException("ES HTTP client connection manager field is unavailable");
                }
                ReflectionUtils.makeAccessible(field);
                if (!(ReflectionUtils.getField(field, httpClient) instanceof PoolingNHttpClientConnectionManager pool)) {
                    throw new IllegalStateException("ES HTTP client does not use the expected Apache 4 pool");
                }
                new PoolingHttpClientConnectionManagerMetricsBinder(pool, "elasticsearch").bindTo(registry);
                // Apache NIO total.pending omits queued leases; route stats include them.
                Gauge.builder("es.http.pool.pending", pool,
                        p -> p.getRoutes().stream().mapToInt(route -> p.getStats(route).getPending()).sum())
                    .description("Requests waiting for an ES connection, including connections being established")
                    .register(registry);
                log.info("ES HTTP pool metrics enabled: totalMax={}, defaultRouteMax={}",
                    pool.getMaxTotal(), pool.getDefaultMaxPerRoute());
            } catch (RuntimeException ex) {
                log.warn("ES HTTP pool metrics unavailable; client configuration was not changed", ex);
            }
        };
    }
}
