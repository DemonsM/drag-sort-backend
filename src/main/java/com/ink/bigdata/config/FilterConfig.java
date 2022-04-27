package com.ink.bigdata.config;


import com.ink.bigdata.filter.CachingRequestBodyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CachingRequestBodyFilter> filterRegistrationBean() {
        return new FilterRegistrationBean<>(new CachingRequestBodyFilter());
    }
}
