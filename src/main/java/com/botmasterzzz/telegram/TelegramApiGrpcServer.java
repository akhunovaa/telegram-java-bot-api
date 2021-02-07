package com.botmasterzzz.telegram;

import com.botmasterzzz.telegram.config.ServerMainConfig;
import com.botmasterzzz.telegram.service.InfoService;
import com.botmasterzzz.telegram.service.MessageService;
import com.botmasterzzz.telegram.service.UpdatesService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TelegramApiGrpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramApiGrpcServer.class);

    private static final int DEFAULT_PORT = 7499;

    @Autowired
    private MessageService messageService;
    @Autowired
    private InfoService infoService;
    @Autowired
    private UpdatesService updatesService;

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerMainConfig.class);
        TelegramApiGrpcServer telegramApiGrpcServer = context.getBean(TelegramApiGrpcServer.class);
        telegramApiGrpcServer.start();
    }

    private void start() {
        LOGGER.info("Telegram API gRPC server has been started");

        Server server = ServerBuilder.forPort(DEFAULT_PORT)
                .addService(messageService)
                .addService(infoService)
                .addService(updatesService)
                .build();

        try {
            server.start();
        } catch (IOException exception) {
            LOGGER.error("There is an error while gRPC server starts", exception);
        }


        try {
            Init.start();
        } catch (CantLoadLibrary exception) {
            LOGGER.error("There is an error while Telegram native libraries starts", exception);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Received Shutdown Request");
            server.shutdown();
            LOGGER.info("Successfully stopped the server");
        }));

        try {
            server.awaitTermination();
        } catch (InterruptedException exception) {
            LOGGER.error("There is an error while termination of the gRPC server awaits", exception);
        }
    }

}
