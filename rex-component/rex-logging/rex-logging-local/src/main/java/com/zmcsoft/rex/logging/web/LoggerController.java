package com.zmcsoft.rex.logging.web;


import com.zmcsoft.rex.logging.access.AccessLoggerDao;
import com.zmcsoft.rex.logging.access.entity.AccessLogger;
import com.zmcsoft.rex.logging.business.BusinessLogger;
import com.zmcsoft.rex.logging.business.dao.BusinessLoggerDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logger")
@Api(tags = "日志管理")
@Authorize(permission = "logger", action = Permission.ACTION_QUERY)
@ConfigurationProperties(prefix = "com.zmcsoft.logger")
@org.hswebframework.web.logging.AccessLogger(value = "日志管理",ignore = true)
public class LoggerController {

    @Autowired
    private AccessLoggerDao accessLoggerDao;

    @Autowired
    private BusinessLoggerDao businessLoggerDao;

    private String dataSourceId = null;

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    private void trySwitchDataSource() {
        if (dataSourceId != null) {
            DataSourceHolder.switcher().use(dataSourceId);
        }
    }

    private void tryFallBackDataSource() {
        if (dataSourceId != null) {
            DataSourceHolder.switcher().useLast();
        }
    }

    @GetMapping("/business")
    @ApiOperation("查询业务日志")
    public ResponseMessage<PagerResult<BusinessLogger>> getBusinessLogger(QueryParamEntity paramEntity) {
        try {
            trySwitchDataSource();
            int total = businessLoggerDao.count(paramEntity);
            if (total == 0) {
                return ResponseMessage.ok(PagerResult.empty());
            }
            paramEntity.rePaging(total);
            List<BusinessLogger> list = businessLoggerDao.query(paramEntity);

            return ResponseMessage.ok(PagerResult.of(total, list))
                    .exclude(BusinessLogger.class, paramEntity.getExcludes())
                    .include(BusinessLogger.class, paramEntity.getIncludes());
        } finally {
            tryFallBackDataSource();
        }
    }

    @GetMapping("/access")
    @ApiOperation("查询请求日志")
    public ResponseMessage<PagerResult<AccessLogger>> getAccessLogger(QueryParamEntity paramEntity) {
        try {
            trySwitchDataSource();
            int total = accessLoggerDao.count(paramEntity);
            if (total == 0) {
                return ResponseMessage.ok(PagerResult.empty());
            }
            paramEntity.rePaging(total);
            List<AccessLogger> list = accessLoggerDao.query(paramEntity);

            return ResponseMessage.ok(PagerResult.of(total, list))
                    .exclude(AccessLogger.class, paramEntity.getExcludes())
                    .include(AccessLogger.class, paramEntity.getIncludes());
        } finally {
            tryFallBackDataSource();
        }
    }

    @GetMapping("/access/{id}")
    @ApiOperation("根据ID查询请求日志")
    public ResponseMessage<AccessLogger> getAccessLoggerDetail(@PathVariable String id) {
        try {
            trySwitchDataSource();
            return ResponseMessage.ok(DefaultDSLQueryService.createQuery(accessLoggerDao)
                    .where("id", id).single());
        } finally {
            tryFallBackDataSource();
        }
    }


    @GetMapping("/business/{id}")
    @ApiOperation("根据ID查询业务日志")
    public ResponseMessage<BusinessLogger> getBusinessLoggerDetail(@PathVariable String id) {
        try {
            trySwitchDataSource();
            return ResponseMessage.ok(DefaultDSLQueryService.createQuery(businessLoggerDao)
                    .where("id", id).single());
        } finally {
            tryFallBackDataSource();
        }
    }

    @GetMapping("/business/request/{requestId}")
    @ApiOperation("根据请求ID查询业务日志")
    public ResponseMessage<List<BusinessLogger>> getBusinessLoggerByRequestId(@PathVariable String requestId) {
        try {
            trySwitchDataSource();
            return ResponseMessage.ok(DefaultDSLQueryService.createQuery(businessLoggerDao)
                    .where("requestId", requestId)
                    .list());
        } finally {
            tryFallBackDataSource();
        }
    }
}
