package com.botmasterzzz.telegram.handler;

import it.tdlight.common.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandler implements ExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    @Override
    public void onException(Throwable exception) {
        LOGGER.error("There was an error {}", exception.getLocalizedMessage(), exception);
        exception.printStackTrace();
    }
}
