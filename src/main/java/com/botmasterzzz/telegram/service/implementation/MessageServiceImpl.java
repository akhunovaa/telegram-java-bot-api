package com.botmasterzzz.telegram.service.implementation;

import com.botmasterzzz.telegram.Client;
import com.botmasterzzz.telegram.service.MessageService;
import com.proto.telegram.SendMessageRequest;
import com.proto.telegram.SendMessageResponse;
import com.proto.telegram.SendMessageServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MessageServiceImpl extends SendMessageServiceGrpc.SendMessageServiceImplBase implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private Client client;
///ToDO Only for test
    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        String botToken = request.getBotToken();
        if (StringUtils.isEmpty(botToken)) {
            responseObserver.onError(new RuntimeException("Bot token unrecognized"));
        }

        String messageText = request.getMessage().getText();
        client.sendMessage(botToken, 78024364, messageText);
        LOGGER.info("Send message {}", messageText);
        SendMessageResponse sendMessageResponse = SendMessageResponse.newBuilder().setStatus(true).build();
        responseObserver.onNext(sendMessageResponse);
        responseObserver.onCompleted();
    }

}
