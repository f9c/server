package com.github.f9c.bot;

import com.github.f9c.Client;
import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientMessageListener;
import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.client.datamessage.TextMessage;
import com.github.f9c.client.datamessage.multipart.ProfileDataMessage;
import com.github.f9c.client.datamessage.multipart.RequestProfileMessage;
import com.github.f9c.message.encryption.Crypt;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.neovisionaries.ws.client.WebSocketException;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import java.io.ByteArrayInputStream;
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
    private final String sendServer;
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

    public BotListener(String baseDir, String botName, String sendServer) throws IOException {
        System.out.println("Using " + new File(baseDir).getAbsolutePath() + " as bas dir.");

        this.sendServer = sendServer;
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
    public void handleDataMessage(ClientMessage message) {
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
            client.sendDataMessage(Crypt.decodeKey(message.getHeader().getSenderPublicKey()), message.getHeader().getSenderServer(), new ProfileDataMessage(clientKeys.getPublicKey(),  client.getHost(),
                    getAlias(), "A harty welcome to f9c!", new ByteArrayInputStream(profileImage)));
        } catch (IOException | WebSocketException e) {
            logger.log(Level.SEVERE, "Communication Error.", e);
        }
    }

    private void handleTextMessage(TextMessage message) {
        try {
            Chat chatSession = openChats.get(message.getSenderPublicKey());
            String response = chatSession.multisentenceRespond(message.getMsg());
            client.sendDataMessage(message.getSenderPublicKey(), message.getHeader().getSenderServer(), new TextMessage(response, clientKeys.getPublicKey(), sendServer));
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
