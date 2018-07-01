package com.github.f9c.bot;

import com.github.f9c.Client;
import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientMessageListener;
import com.github.f9c.client.datamessage.AbstractDataMessage;
import com.github.f9c.client.datamessage.ProfileDataMessage;
import com.github.f9c.client.datamessage.RequestProfileMessage;
import com.github.f9c.client.datamessage.TextMessage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.neovisionaries.ws.client.WebSocketException;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotListener implements ClientMessageListener {
    private static Logger logger = Logger.getLogger(BotListener.class.getName());
    private Client client;
    private ClientKeys clientKeys;
    private byte[] profileImage;

    private Bot bot;

    private LoadingCache<PublicKey, Chat> openChats = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<PublicKey, Chat>() {
                        public Chat load(PublicKey key) {
                            logger.info("Creating new chat: " + key);
                            return new Chat(bot);
                        }
                    });

    public BotListener(String baseDir, String botName) throws IOException {
        System.out.println("Using " + new File(baseDir).getAbsolutePath() + " as bas dir.");

        profileImage = Files.readAllBytes(Paths.get(baseDir, "bots", botName, "profile.png"));

        this.bot = new Bot(botName, baseDir, "chat");
    }

    public String getAlias() {
        return bot.name;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setClientKeys(ClientKeys clientKeys) {
        this.clientKeys = clientKeys;
    }

    @Override
    public void handleDataMessage(AbstractDataMessage message) {
        if (message instanceof TextMessage) {
            handleTextMessage((TextMessage) message);
        } else if (message instanceof RequestProfileMessage) {
            handleProfileDataRequest((RequestProfileMessage) message);
        } else {
            logger.info(() -> "Unexpected Message: " + message.getClass().getName());
        }
    }

    private void handleProfileDataRequest(RequestProfileMessage message) {
        try {
            client.sendDataMessage(message.getSenderPublicKey(), new ProfileDataMessage(
                    getAlias(), "A harty welcome to f9c!", profileImage, clientKeys.getPublicKey()));
        } catch (IOException | WebSocketException e) {
            logger.log(Level.SEVERE, "Communication Error.", e);
        }
    }

    private void handleTextMessage(TextMessage message) {
        try {
            Chat chatSession = openChats.get(message.getSenderPublicKey());
            String response = chatSession.multisentenceRespond(message.getMsg());
            client.sendDataMessage(message.getSenderPublicKey(), new TextMessage(response, clientKeys.getPublicKey(), message.getServer()));
        } catch (IOException | WebSocketException e) {
            logger.log(Level.SEVERE, "Communication Error.", e);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Unexpected Error.", e);
        }
    }

    @Override
    public void handleError(Throwable cause) {
        logger.log(Level.SEVERE, "", cause);
    }
}
