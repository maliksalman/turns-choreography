package com.smalik.choreographer.db;

import com.smalik.choreographer.Move;
import org.apache.geode.cache.GemFireCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.LookupRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableClusterDefinedRegions;

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
}
