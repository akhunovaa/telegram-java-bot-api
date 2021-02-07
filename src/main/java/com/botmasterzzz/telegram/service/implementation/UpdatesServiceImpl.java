package com.botmasterzzz.telegram.service.implementation;

import com.botmasterzzz.telegram.Client;
import com.botmasterzzz.telegram.service.UpdatesService;
import com.proto.telegram.GetUpdatesRequest;
import com.proto.telegram.GetUpdatesResponse;
import com.proto.telegram.GetUpdatesServiceGrpc;
import com.proto.telegram.Update;
import io.grpc.stub.StreamObserver;
import it.tdlight.common.ExceptionHandler;
import it.tdlight.common.UpdatesHandler;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UpdatesServiceImpl extends GetUpdatesServiceGrpc.GetUpdatesServiceImplBase implements UpdatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesServiceImpl.class);
    private static final String newLine = System.getProperty("line.separator");

    @Autowired
    private Client client;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void getUpdates(GetUpdatesRequest request, StreamObserver<GetUpdatesResponse> responseObserver) {
        String botToken = request.getBotToken();
        try {
            if (StringUtils.isEmpty(botToken)) {
                throw new RuntimeException("Bot token unrecognized");
            }

            UpdatesHandler updatesHandler = new UpdatesHandler() {

                @Override
                public void onUpdates(List<TdApi.Object> list) {
                    list.forEach(object -> {
                        switch (object.getConstructor()) {
                            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                                LOGGER.info("Need authorization:{}{}", newLine, object);
                                TdApi.AuthorizationState authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
                                eventPublisher.publishEvent(authorizationState);
                                break;
                            default:
                                LOGGER.warn("Unsupported update:{}{}", newLine, object);
                                Update update = Update.newBuilder()
                                        .setId(list.size())
                                        .setResult(list.toString())
                                        .build();
                                GetUpdatesResponse getUpdatesResponse = GetUpdatesResponse.newBuilder()
                                        .setOk(true)
                                        .setUpdate(update)
                                        .build();
                                responseObserver.onNext(getUpdatesResponse);
                        }
                    });
//                    responseObserver.onCompleted();
                }
            };

            ExceptionHandler exceptionHandler = new ExceptionHandler() {

                @Override
                public void onException(Throwable throwable) {
                    GetUpdatesResponse getUpdatesResponse = GetUpdatesResponse.newBuilder()
                            .setOk(false)
                            .build();
                    responseObserver.onNext(getUpdatesResponse);
                }
            };
            client.getUpdates(botToken, updatesHandler, exceptionHandler);
        } catch (Exception exception) {
            LOGGER.error("There was en error{}{}", newLine, exception);
            responseObserver.onError(exception);
        }
    }

}
