package com.botmasterzzz.telegram;

import com.botmasterzzz.telegram.container.TelegramBotContainer;
import com.botmasterzzz.telegram.handler.AuthorizationRequestHandler;
import com.botmasterzzz.telegram.handler.ErrorHandler;
import com.botmasterzzz.telegram.handler.ResultsHandler;
import com.botmasterzzz.telegram.handler.UpdateHandler;
import com.botmasterzzz.telegram.util.BeanUtil;
import it.tdlight.common.*;
import it.tdlight.jni.TdApi;
import it.tdlight.tdlight.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {

    private static final TelegramBotContainer container = TelegramBotContainer.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final Lock authorizationLock = new ReentrantLock();
    private final Lock authorizationTokenLock = new ReentrantLock();
    private final Condition gotAuthorization = authorizationLock.newCondition();
    private final Condition gotAuthorizationToken = authorizationTokenLock.newCondition();
    private final boolean needQuit = false;
    private volatile boolean haveAuthorization = false;
    private volatile boolean canQuit = false;

    private volatile String botToken;

    @Value("${telegram.client.path.location}")
    private String temporaryDatabaseFolderPath;
    @Value("${telegram.client.system.language.code}")
    private String systemLanguageCode;
    @Value("${telegram.client.api.hash}")
    private String apiHash;
    @Value("${telegram.client.device.model}")
    private String applicationName;
    @Value("${telegram.client.application.version}")
    private String applicationVersion;
    @Value("${telegram.client.api.id}")
    private int apiId;

    public void init(String _botToken) {
        this.botToken = _botToken;
        this.haveAuthorization = false;
        LOGGER.info("Telegram TDLib native invoke interface has been started");
        TelegramClient client = ClientManager.create();
        ResultsHandler resultsHandler = BeanUtil.getBean(ResultsHandler.class);
        ErrorHandler errorHandler = BeanUtil.getBean(ErrorHandler.class);
        client.initialize(resultsHandler, errorHandler, errorHandler);
        client.execute(new TdApi.SetLogVerbosityLevel(0));
        // disable TDLib log
        if (client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(_botToken.substring(0, botToken.indexOf(":")) + "tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        container.addTelegramClient(botToken, client);
        authorizationLock.lock();
        try {
            while (!haveAuthorization) {
                try {
                    gotAuthorization.await();
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("There is an error as interrupted authorization BOT token awaiting");
                }
            }
        } finally {
            authorizationLock.unlock();
        }
    }

    @EventListener
    public void onAuthorizationStateUpdate(TdApi.AuthorizationState authorizationState) {
        if (authorizationState == null) {
            return;
        }
        authorizationTokenLock.lock();
        try {
            while (!container.getTelegramClientMap().containsKey(this.botToken)) {
                gotAuthorizationToken.await();
            }
        } catch (InterruptedException interruptedException) {
            LOGGER.error("There is an error as interrupted authorization BOT token awaiting");
        } finally {
            authorizationTokenLock.unlock();
        }
        TelegramClient client = container.getTelegramClientMap().get(this.botToken);
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = botToken.substring(0, botToken.indexOf(":")) + this.temporaryDatabaseFolderPath;
                parameters.useMessageDatabase = false;
                parameters.apiId = this.apiId;
                parameters.apiHash = this.apiHash;
                parameters.systemLanguageCode = this.systemLanguageCode;
                parameters.deviceModel = this.applicationName;
                parameters.applicationVersion = this.applicationVersion;
                parameters.enableStorageOptimizer = false;

                client.send(new TdApi.SetTdlibParameters(parameters), BeanUtil.getBean(AuthorizationRequestHandler.class));
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), BeanUtil.getBean(AuthorizationRequestHandler.class));
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                LOGGER.info("Logging out...");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                LOGGER.info("Closing...");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                LOGGER.info("Closed...");
                if (!needQuit) {
                    client = ClientManager.create();
                    client.initialize(BeanUtil.getBean(UpdateHandler.class), BeanUtil.getBean(ErrorHandler.class), BeanUtil.getBean(ErrorHandler.class));
                } else {
                    canQuit = true;
                }
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                authorizationTokenLock.lock();
                try {
                    while (StringUtils.isEmpty(this.botToken)) {
                        gotAuthorizationToken.await();
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("There is an error as interrupted authorization BOT token awaiting");
                } finally {
                    authorizationTokenLock.unlock();
                }
                client.send(new TdApi.CheckAuthenticationBotToken(this.botToken), BeanUtil.getBean(AuthorizationRequestHandler.class));
                break;
            }
            default:
                LOGGER.error("Unsupported authorization state: {}", authorizationState);
        }
    }

    public void sendMessage(final String _botToken, final long chatId, final String message) {
        if (!container.getTelegramClientMap().containsKey(_botToken)) {
            init(_botToken);
        }
        getChat(_botToken, chatId);
        TelegramClient client = container.getTelegramClientMap().get(_botToken);
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl("https://video.yourapi.ru")), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl("https://video.yourapi.ru")), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl("https://video.yourapi.ru"))};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});
        TdApi.MessageSendOptions sendOptions = new TdApi.MessageSendOptions(true, true, null);
        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, 0, sendOptions, replyMarkup, content), BeanUtil.getBean(ResultsHandler.class));

        //TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText("Test", null), false, false);

        //TdApi.Function requestSendMessageFunction = new TdApi.SendMessage(78024364L, 0, 0, sendOptions, null, content);
    }


    public void getChat(final String _botToken, final long chatId) {
        if (!container.getTelegramClientMap().containsKey(_botToken)) {
            init(_botToken);
        }
        TelegramClient client = container.getTelegramClientMap().get(_botToken);
        client.send(new TdApi.GetChat(chatId), BeanUtil.getBean(ResultsHandler.class));
    }

    public void getMe(final String _botToken, ResultHandler resultHandler) {
        if (!container.getTelegramClientMap().containsKey(_botToken)) {
            init(_botToken);
        }
        TelegramClient client = container.getTelegramClientMap().get(_botToken);
        client.send(new TdApi.GetMe(), resultHandler);
    }


    public void getUpdates(String _botToken, UpdatesHandler updatesHandler, ExceptionHandler exceptionHandler) {
        if (!container.getTelegramClientMap().containsKey(_botToken)) {
            init(_botToken);
        }

        InternalClient client = (InternalClient) container.getTelegramBotClient(_botToken);
        int clientId = client.getClientId();
        haveAuthorization = false;
        client.send(new TdApi.LogOut(), BeanUtil.getBean(ResultsHandler.class));
        client = (InternalClient) ClientManager.create();
        client.initialize(updatesHandler, exceptionHandler, exceptionHandler);
        container.getTelegramClientMap().put(_botToken, client);
    }

}
