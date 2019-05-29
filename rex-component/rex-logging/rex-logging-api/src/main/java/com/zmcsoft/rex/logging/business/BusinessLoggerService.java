package com.zmcsoft.rex.logging.business;

/**
 * @author zhouhao
 * @since
 */
public interface BusinessLoggerService {
    /**
     * 推送一个业务日志
     *
     * @param logger 日志内容
     */
    void push(BusinessLogger logger);
}
