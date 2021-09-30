package com.smalik.choreographer.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smalik.choreographer.Move;
import org.apache.geode.cache.Region;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DatabaseConfig {

    @Bean("movesDatabase")
    @Profile("redisson")
    public RedissonMovesDatabase redissonMovesDatabase(RedissonClient client, ObjectMapper mapper) {
        return new RedissonMovesDatabase(client, mapper);
    }

    @Bean("movesDatabase")
    @Profile("geode")
    public GeodeMovesDatabase geodeMovesDatabase(@Qualifier("movesRegion") Region<String, Move> region) {
        return new GeodeMovesDatabase(region);
    }

    @Bean("movesDatabase")
    @ConditionalOnMissingBean(name = "movesDatabase")
    public InMemoryMovesDatabase inMemoryMovesDatabase() {
        return new InMemoryMovesDatabase();
    }
}
