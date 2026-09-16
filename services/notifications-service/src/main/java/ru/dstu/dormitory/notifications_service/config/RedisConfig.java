package ru.dstu.dormitory.notifications_service.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.dstu.dormitory.notifications_service.client.dto.AuthUserDto;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    public static final String CACHE_USER = "notif-user";
    public static final String CACHE_USERS_BY_ROLE = "notif-users-by-role";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializationContext.SerializationPair<String> keyPair =
                RedisSerializationContext.SerializationPair.fromSerializer(keySerializer);

        // DECISION: для кешей пользователей используем типизированный Jackson2JsonRedisSerializer
        // без activateDefaultTyping — иначе при чтении значений, записанных auth-service (без @class),
        // консьюмеры падают с "missing type id property '@class'". Тип фиксируется здесь, а не в payload.
        Jackson2JsonRedisSerializer<AuthUserDto> userSerializer =
                new Jackson2JsonRedisSerializer<>(om, AuthUserDto.class);

        JavaType usersByRoleType = om.getTypeFactory().constructCollectionType(List.class, AuthUserDto.class);
        Jackson2JsonRedisSerializer<List<AuthUserDto>> usersByRoleSerializer =
                new Jackson2JsonRedisSerializer<>(om, usersByRoleType);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(keyPair)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(om)))
                .entryTtl(Duration.ofMinutes(10));

        RedisCacheConfiguration userCfg = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(keyPair)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(userSerializer))
                .entryTtl(Duration.ofMinutes(5));

        RedisCacheConfiguration usersByRoleCfg = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(keyPair)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(usersByRoleSerializer))
                .entryTtl(Duration.ofMinutes(1));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration(CACHE_USER, userCfg)
                .withCacheConfiguration(CACHE_USERS_BY_ROLE, usersByRoleCfg)
                .build();
    }
}
