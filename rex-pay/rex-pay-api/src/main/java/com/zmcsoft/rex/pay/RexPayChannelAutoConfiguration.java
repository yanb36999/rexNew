package com.zmcsoft.rex.pay;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(RexPayService.class)
public class RexPayChannelAutoConfiguration {

    @Bean
    public DefaultRexPayService defaultRexPayService() {
        return new DefaultRexPayService();
    }

    @Bean
    public BeanPostProcessor autoRegisterChannelProcessor(DefaultRexPayService defaultRexPayService) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
                return o;
            }

            @Override
            public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
                if (o instanceof RexPayChannelSupplier) {
                    RexPayChannelSupplier supplier = ((RexPayChannelSupplier) o);
                    defaultRexPayService.registerChannel(supplier.getChannel(), supplier.get());
                }
                return o;
            }
        };
    }
}
