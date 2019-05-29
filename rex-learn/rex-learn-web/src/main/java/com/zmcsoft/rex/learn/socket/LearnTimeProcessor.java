package com.zmcsoft.rex.learn.socket;

import com.zmcsoft.rex.api.user.entity.UserDriverLicense;
import com.zmcsoft.rex.api.user.service.UserServiceManager;
import com.zmcsoft.rex.learn.api.service.DayDetailService;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.socket.CommandRequest;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.processor.AbstractCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Component
@RestController
public class LearnTimeProcessor extends AbstractCommandProcessor {

    @Autowired
    private DayDetailService dayDetailService;

    @Override
    public String getName() {
        return "learn";
    }

    /**
     * 处理websocket请求.
     * <pre>
     * var ws = new WebSocket("ws://localhost:8085/socket");
     * ws.onmessage=function(e){console.log(e.data)}
     * ws.send(JSON.stringify({"command":"learn",parameters:{"type":"update-learn-time","dayDetailId":"0123131231ds"}}));
     * </pre>
     * 如果没有使用session保存权限，请先进行授权操作。否则会返回401!(使用session会在首次连接的时候进行授权)
     *
     * <pre>
     * ws.send(JSON.stringify({"command":"authorize",parameters:{"access_token":"令牌"}}));
     * 返回结果:
     * {"command":"authorize","result":true,"status":200}
     * result:true 则代表授权成功
     * </pre>
     *
     * @param commandRequest
     */
    @Override
    @Authorize //验证权限
    public void execute(CommandRequest commandRequest) {

        String type = (String) commandRequest.getParameters().get("type");
        System.out.println(Thread.currentThread().getId());
        sendMessage(commandRequest.getSession(), new WebSocketMessage(200, type));

        String userId = commandRequest.getAuthentication().getUser().getId();
        String dayDetailId = (String) commandRequest.getParameters().get("dayDetailId");

        dayDetailService.updateLearnTime(dayDetailId,userId);
    }
}
