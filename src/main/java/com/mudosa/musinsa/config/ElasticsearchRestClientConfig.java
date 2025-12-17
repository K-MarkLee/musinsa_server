//package com.mudosa.musinsa.config;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import org.apache.http.Header;
//import org.apache.http.HttpHost;
//import org.apache.http.HttpResponseInterceptor;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
//import org.apache.http.message.BasicHeader;
//import org.elasticsearch.client.RestClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * OpenSearch 전용 RestClient 설정.
// * - Basic Auth를 사용해 인증 (환경변수/설정에서 username/password 주입)
// * - 호환 헤더 추가(compatible-with=7)
// * - OpenSearch 응답에 X-Elastic-Product 헤더가 없는 경우 주입해 클라이언트 검증 통과
// */
//@Configuration
//public class ElasticsearchRestClientConfig {
//
//    @Value("${elasticsearch.host:localhost}")
//    private String host;
//
//    @Value("${elasticsearch.port:9200}")
//    private int port;
//
//    @Value("${elasticsearch.scheme:https}")
//    private String scheme;
//
//    @Value("${elasticsearch.username:${OPENSEARCH_USERNAME:}}")
//    private String username;
//
//    @Value("${elasticsearch.password:${OPENSEARCH_PASSWORD:}}")
//    private String password;
//
//    @Bean
//    public RestClient restClient() {
//        Header[] headers = new Header[] {
//            new BasicHeader("Accept", "application/vnd.elasticsearch+json;compatible-with=7"),
//            new BasicHeader("Content-Type", "application/json;compatible-with=7")
//        };
//
//        return RestClient.builder(new HttpHost(host, port, scheme))
//            .setDefaultHeaders(headers)
//            .setHttpClientConfigCallback(this::configureHttpClient)
//            .setRequestConfigCallback(requestConfigBuilder ->
//                requestConfigBuilder
//                    .setConnectTimeout(5000)
//                    .setSocketTimeout(30000)
//            )
//            .build();
//    }
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
//        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
//        return new ElasticsearchClient(transport);
//    }
//
//    private HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        if (username != null && !username.isBlank()) {
//            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
//        }
//
//        HttpResponseInterceptor productHeaderInjector = (response, context) -> {
//            if (!response.containsHeader("X-Elastic-Product")) {
//                response.addHeader("X-Elastic-Product", "Elasticsearch");
//            }
//        };
//
//        return httpClientBuilder
//            .setDefaultCredentialsProvider(credentialsProvider)
//            .addInterceptorLast(productHeaderInjector)
//            .setMaxConnTotal(200)
//            .setMaxConnPerRoute(100);
//    }
//}
