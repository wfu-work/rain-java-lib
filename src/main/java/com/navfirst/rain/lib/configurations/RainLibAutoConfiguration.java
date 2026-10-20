package com.navfirst.rain.lib.configurations;

import com.navfirst.rain.lib.services.RainLibService;
import com.navfirst.rain.lib.services.impl.RainLibServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建：馥溪凝
 * 日期：2026/10/23 12:14
 * 描述：RainAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@AutoConfiguration
public class RainLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RainLibService rainLibService() {
        return new RainLibServiceImpl();
    }

}
