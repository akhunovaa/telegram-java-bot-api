package com.botmasterzzz.telegram.handler;

import it.tdlight.common.UpdatesHandler;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateHandler implements UpdatesHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);
    private static final String newLine = System.getProperty("line.separator");

    @Override
    public void onUpdates(List<TdApi.Object> list) {
        LOGGER.info("Incoming update update:{}{}", newLine, list.toString());
    }
}
