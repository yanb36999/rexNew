package com.zmcsoft.rex.message;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageSenderAutoConfiguration {

    @Bean
    public DefaultMessageSenders defaultMessageSenders() {
        return new DefaultMessageSenders();
    }

    @Bean
    public BeanPostProcessor autoRegisterMessageProvider(DefaultMessageSenders messageSenders) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
                return o;
            }

            @Override
            public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
                if (o instanceof MessageSenderProvider) {
                    messageSenders.register(((MessageSenderProvider) o));
                }
                return o;
            }
        };
    }
}
