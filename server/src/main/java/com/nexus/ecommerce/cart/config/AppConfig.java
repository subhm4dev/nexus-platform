package com.nexus.ecommerce.cart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Application Configuration
 * 
 * <p>Provides beans for RedisTemplate for cart storage and JWT validation.
 * 
 * <p>Note: ResilientWebClient is auto-configured by http-client-starter.
 * No need to configure RestTemplate or WebClient manually.
 */
@Configuration
public class AppConfig {

    /**
     * RedisTemplate for JWT validation (token blacklisting)
     * Required by jwt-validation-starter - bean name must be "redisTemplate"
     * The auto-configuration checks for a bean with name "redisTemplate"
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * ObjectMapper configured for Redis serialization
     * Includes JavaTimeModule to handle LocalDateTime and other Java 8 time types
     * Note: We don't enable default typing to avoid issues with existing Redis data
     * Instead, we handle type conversion manually in the repository
     */
    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Don't enable default typing - it causes issues with existing Redis data
        // We'll handle type conversion manually in the repository
        return mapper;
    }

    /**
     * RedisTemplate for cart storage
     * Uses JSON serialization for cart objects with Java 8 time support
     * Marked as @Primary so it's injected by default when no qualifier is specified
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> cartRedisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // Use configured ObjectMapper with JavaTimeModule and type information support
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        template.afterPropertiesSet();
        return template;
    }
}

