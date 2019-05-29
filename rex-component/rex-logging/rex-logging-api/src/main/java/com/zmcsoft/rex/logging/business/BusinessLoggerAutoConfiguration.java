package com.zmcsoft.rex.logging.business;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0
 */
@Configuration
public class BusinessLoggerAutoConfiguration {

    @ConditionalOnMissingBean(BusinessLoggerService.class)
    @Bean
    public DefaultBusinessLoggerService defaultBusinessLoggerService() {
        return new DefaultBusinessLoggerService();
    }


}
