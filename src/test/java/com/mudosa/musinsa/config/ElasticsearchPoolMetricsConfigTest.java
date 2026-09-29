package com.mudosa.musinsa.config;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ElasticsearchPoolMetricsConfigTest {
    @Test
    void appliesTheMeasuredDevConnectionLimits() throws Exception {
        var customizer = new ElasticsearchRestClientConfig();
        var builder = RestClient.builder(new HttpHost("127.0.0.1", 9200));
        customizer.customize(builder);
        builder.setHttpClientConfigCallback(http -> {
            customizer.customize(http);
            return http;
        });
        var registry = new SimpleMeterRegistry();
        try (var client = builder.build()) {
            new ElasticsearchPoolMetricsConfig().elasticsearchPoolMetrics(client, registry).run(null);
            assertThat(registry.get("httpcomponents.httpclient.pool.total.max").gauge().value()).isEqualTo(150);
            assertThat(registry.get("httpcomponents.httpclient.pool.route.max.default").gauge().value()).isEqualTo(150);
        } finally {
            registry.close();
        }
    }

    @Test
    void exposesTheExistingPoolAndRecordsConnectionWaits() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            entered.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        var registry = new SimpleMeterRegistry();
        try (var client = RestClient.builder(new HttpHost("127.0.0.1", server.getAddress().getPort()))
                 .setHttpClientConfigCallback(builder -> builder.setMaxConnPerRoute(1)).build()) {
            new ElasticsearchPoolMetricsConfig().elasticsearchPoolMetrics(client, registry).run(null);
            String prefix = "httpcomponents.httpclient.pool.";
            assertThat(registry.get(prefix + "total.max").gauge().value()).isEqualTo(30);
            assertThat(registry.get(prefix + "route.max.default").gauge().value()).isEqualTo(1);
            var leased = registry.get(prefix + "total.connections").tag("state", "leased").gauge();
            var pending = registry.get("es.http.pool.pending").gauge();
            var available = registry.get(prefix + "total.connections").tag("state", "available").gauge();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            var first = client.getHttpClient().execute(new HttpGet(url), null);
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            var second = client.getHttpClient().execute(new HttpGet(url), null);
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(leased.value()).isEqualTo(1);
                assertThat(pending.value()).isEqualTo(1);
                assertThat(available.value()).isZero();
            });
            release.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS).getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(second.get(5, TimeUnit.SECONDS).getStatusLine().getStatusCode()).isEqualTo(200);
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(leased.value()).isZero();
                assertThat(pending.value()).isZero();
                assertThat(available.value()).isEqualTo(1);
            });
        } finally {
            release.countDown();
            server.stop(0);
            registry.close();
        }
    }
}
