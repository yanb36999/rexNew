package com.zmcsoft.rex.logging.business;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface BusinessLoggerListener {
    /**
     * 消费
     * @param logger
     */
    void onLogger(BusinessLogger logger);
}
