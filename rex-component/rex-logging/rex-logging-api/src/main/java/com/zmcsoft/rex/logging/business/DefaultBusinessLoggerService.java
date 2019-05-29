package com.zmcsoft.rex.logging.business;

import com.zmcsoft.rex.logging.business.logback.LogbackBusinessLoggerAppender;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0
 */
public class DefaultBusinessLoggerService implements BusinessLoggerService  {

    private List<BusinessLoggerListener> businessLoggerListeners;

    @Autowired(required = false)
    public void setBusinessLoggerListeners(List<BusinessLoggerListener> businessLoggerListeners) {
        this.businessLoggerListeners = businessLoggerListeners;
    }

    @Override
    public void push(BusinessLogger logger) {
        logger.setId(IDGenerator.MD5.generate());
        if (businessLoggerListeners != null) {
            businessLoggerListeners.forEach(cons -> cons.onLogger(logger));
        }
    }

    @PostConstruct
    public void init(){
        LogbackBusinessLoggerAppender.setLoggerService(this);
    }
}
