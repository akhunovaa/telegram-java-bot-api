package com.botmasterzzz.telegram.service.implementation;

import com.botmasterzzz.telegram.Client;
import com.botmasterzzz.telegram.service.InfoService;
import com.proto.telegram.InfoServiceGrpc;
import com.proto.telegram.Request;
import com.proto.telegram.Response;
import com.proto.telegram.Result;
import io.grpc.stub.StreamObserver;
import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InfoServiceImpl extends InfoServiceGrpc.InfoServiceImplBase implements InfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoServiceImpl.class);
    private static final String newLine = System.getProperty("line.separator");

    @Autowired
    private Client client;

    @Override
    public void getMe(Request request, StreamObserver<Response> responseObserver) {
        String botToken = request.getBotToken();
        try {
            if (StringUtils.isEmpty(botToken)) {
                throw new RuntimeException("Bot token unrecognized");
            }
            ResultHandler resultHandler = object -> {
                Response response;
                if (object.getConstructor() == TdApi.User.CONSTRUCTOR) {
                    LOGGER.info("Consumed user info:{}{}", newLine, object);
                    TdApi.User userInfo = (TdApi.User) object;
                    TdApi.UserTypeBot userTypeBot = (TdApi.UserTypeBot) ((TdApi.User) object).type;

                    Result result = Result.newBuilder()
                            .setId(userInfo.id)
                            .setIsBot(!userInfo.isContact)
                            .setFirstName(userInfo.firstName)
                            .setUsername(userInfo.username)
                            .setCanJoinGroups(userTypeBot.canJoinGroups)
                            .setCanReadAllGroupMessages(userTypeBot.canReadAllGroupMessages)
                            .setSupportsInlineQueries(userTypeBot.isInline)
                            .build();
                    response = Response.newBuilder()
                            .setOk(true)
                            .setResult(result)
                            .build();

                } else {
                    LOGGER.warn("Unsupported result:{}{}", newLine, object);
                    response = Response.newBuilder()
                            .setOk(false)
                            .build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            };
            client.getMe(botToken, resultHandler);
        } catch (Exception exception) {
            responseObserver.onError(exception);
        }
    }

}
