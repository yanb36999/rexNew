package com.zmcsoft.rex.redission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "business.session")
public class SessionCreateListener implements ApplicationListener<SessionCreatedEvent> {
    @Override
    public void onApplicationEvent(SessionCreatedEvent event) {
        log.info("session created:{}", event.getSessionId());
    }
}
