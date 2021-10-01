package com.smalik.choreographer.db;

import com.smalik.choreographer.Move;
import org.apache.geode.cache.GemFireCache;
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

    @Bean("movesRegion")
    public LookupRegionFactoryBean<String, Move> movesRegion(GemFireCache cache) {
        LookupRegionFactoryBean<String, Move> factoryBean = new LookupRegionFactoryBean<>();
        factoryBean.setRegionName("/moves");
        factoryBean.setLookupEnabled(true);
        factoryBean.setCache(cache);
        return factoryBean;
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
