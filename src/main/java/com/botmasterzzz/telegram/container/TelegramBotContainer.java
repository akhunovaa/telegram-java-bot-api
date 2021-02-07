package com.botmasterzzz.telegram.container;

import it.tdlight.common.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class TelegramBotContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotContainer.class);

    private final ConcurrentHashMap<String, TelegramClient> telegramClientMap;

    private TelegramBotContainer() {
        telegramClientMap = new ConcurrentHashMap<>();
    }

    public static TelegramBotContainer getInstance() {
        return Holder.INST;
    }

    public void addTelegramClient(String telegramClientToken, TelegramClient client) {
        if (telegramClientMap.containsKey(telegramClientToken)) {
            LOGGER.warn("This {} already added", telegramClientToken);
        }
        telegramClientMap.put(telegramClientToken, client);
    }

    public TelegramClient getTelegramBotClient(String telegramClientToken) {
        return telegramClientMap.get(telegramClientToken);
    }

    public ConcurrentHashMap<String, TelegramClient> getTelegramClientMap() {
        return telegramClientMap;
    }

    private static class Holder {
        static final TelegramBotContainer INST = new TelegramBotContainer();
    }
}
