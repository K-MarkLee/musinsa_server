package com.mudosa.musinsa.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Value("${spring.data.redis.database:0}")
  private int redisDatabase;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
    standaloneConfig.setHostName(redisHost);
    standaloneConfig.setPort(redisPort);
    standaloneConfig.setDatabase(redisDatabase);

    if (redisPassword != null && !redisPassword.isEmpty()) {
      standaloneConfig.setPassword(redisPassword);
    }

    return new LettuceConnectionFactory(standaloneConfig);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    template.afterPropertiesSet();
    return template;
  }


    /*
     * StringRedisTemplate 추가 (쿠폰 발급용), set연산 최적화
     */

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return  new StringRedisTemplate(connectionFactory);
    }

    /*
     * Redisson 클라이언트 추가 (분산 락용)
     */

    @Bean
    public RedissonClient redissonClient(){

        Config config = new Config();

        String address = "redis://" + redisHost + ":" + redisPort;

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDatabase)
                .setConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000);

        // 패스워드가 있으면 설정
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

}

