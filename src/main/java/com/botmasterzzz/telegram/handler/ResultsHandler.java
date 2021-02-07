package com.botmasterzzz.telegram.handler;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ResultsHandler implements ResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsHandler.class);
    private static final String newLine = System.getProperty("line.separator");
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void onResult(TdApi.Object object) {
        LOGGER.info("Incoming update object:{}{}", newLine, object);

        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                LOGGER.info("Need authorization:{}{}", newLine, object);
                TdApi.AuthorizationState authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
                eventPublisher.publishEvent(authorizationState);
                break;
            default:
                LOGGER.warn("Unsupported update:{}{}", newLine, object);
        }
    }
}
