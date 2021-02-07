package com.botmasterzzz.telegram.handler;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationRequestHandler implements ResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationRequestHandler.class);

    private static final String newLine = System.getProperty("line.separator");

    @Override
    public void onResult(TdApi.Object object) {
        LOGGER.info("Incoming authorization object:{}{}", newLine, object);
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                LOGGER.error("Receive an error:{}{}", newLine, object);
                break;
            case TdApi.Ok.CONSTRUCTOR:
                // nothing to do
                break;
            default:
                LOGGER.error("Receive wrong response from TDLib:{}{}", newLine, object);
        }
    }
}
