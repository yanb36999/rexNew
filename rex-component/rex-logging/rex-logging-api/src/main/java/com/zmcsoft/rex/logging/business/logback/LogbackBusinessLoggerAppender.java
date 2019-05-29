package com.zmcsoft.rex.logging.business.logback;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.zmcsoft.rex.logging.business.BusinessLogger;
import com.zmcsoft.rex.logging.business.BusinessLoggerService;
import org.hswebframework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public class LogbackBusinessLoggerAppender extends UnsynchronizedAppenderBase<LoggingEvent> {

    private static BusinessLoggerService loggerService;

    private static Logger logger = LoggerFactory.getLogger(LogbackBusinessLoggerAppender.class);

    public static synchronized void setLoggerService(BusinessLoggerService loggerService) {
        LogbackBusinessLoggerAppender.loggerService = loggerService;
    }

    @Override
    protected void append(LoggingEvent event) {
        StackTraceElement element = event.getCallerData()[0];
        IThrowableProxy proxies = event.getThrowableProxy();
        String message = event.getFormattedMessage();

        if (null != proxies) {

            int commonFrames = proxies.getCommonFrames();
            StackTraceElementProxy[] stepArray = proxies.getStackTraceElementProxyArray();
            StringJoiner joiner = new StringJoiner("\n", message+"\n[", "]");

            StringBuilder stringBuilder = new StringBuilder();

            ThrowableProxyUtil.subjoinFirstLine(stringBuilder, proxies);
            joiner.add(stringBuilder);

            for (int i = 0; i < stepArray.length - commonFrames; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(CoreConstants.TAB);
                ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
                joiner.add(sb);
            }
            message=joiner.toString();
        }
        //  .getStackTraceElementProxyArray();

        BusinessLogger businessLogger = BusinessLogger.builder()
                .level(event.getLevel().levelStr)
                .name(event.getLoggerName())
                .className(element.getClassName())
                .methodName(element.getMethodName())
                .lineNumber(element.getLineNumber())
                .message(message)
                .threadName(event.getThreadName())
                .createTime(event.getTimeStamp())
                .threadId(String.valueOf(Thread.currentThread().getId()))
                .build();
        if (loggerService == null) {
            logger.warn("businessLoggerService not ready!logger:{}", businessLogger);
        } else {
            loggerService.push(businessLogger);
        }
    }
}
