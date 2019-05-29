package com.zmcsoft.rex.redission;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "business.session")
public class SessionDeletedListener implements ApplicationListener<SessionExpiredEvent> {
    @Autowired
    private UserTokenManager userTokenManager;

    @Override
    public void onApplicationEvent(SessionExpiredEvent event) {

        log.info("session expired:{}", event.getSessionId());
        userTokenManager.signOutByToken(event.getSessionId());
    }
}
