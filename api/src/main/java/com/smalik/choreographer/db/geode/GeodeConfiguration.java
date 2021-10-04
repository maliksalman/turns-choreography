package com.smalik.choreographer.db.geode;

import com.smalik.choreographer.db.PlayerLockService;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.LookupRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnableClusterDefinedRegions;
import org.springframework.data.gemfire.support.ConnectionEndpoint;

import java.util.Arrays;
import java.util.stream.Collectors;

@Profile("geode")
@EnableClusterDefinedRegions
@Configuration
public class GeodeConfiguration {

    @Bean("locksRegion")
    public LookupRegionFactoryBean<String, String> locksRegion(GemFireCache cache) {
        LookupRegionFactoryBean<String, String> factoryBean = new LookupRegionFactoryBean<>();
        factoryBean.setRegionName("/player-locks");
        factoryBean.setLookupEnabled(true);
        factoryBean.setCache(cache);
        return factoryBean;
    }

    @Bean("locksService")
    public PlayerLockService playerLockService(@Qualifier("locksRegion") Region<String, String> region) {
        return new GeodePlayerLockService(region);
    }

    @Bean
    @ConditionalOnProperty(prefix = "geode", name = "locators")
    public ClientCacheConfigurer configurer(@Value("${geode.locators}") String[] locators) {
        return (name, bean) -> bean.setLocators(Arrays.stream(locators)
                .map(ConnectionEndpoint::parse)
                .collect(Collectors.toList())
        );
    }
}
