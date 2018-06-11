package com.github.f9c.redis;

import com.github.f9c.message.PayloadMessage;
import com.github.f9c.net.WebSocketFrameHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class RedisConnection {
    private static Logger log = Logger.getLogger(RedisConnection.class.getSimpleName());

    private static final long TIMEOUT = 30000;

    private StatefulRedisConnection<byte[], byte[]> connection;
    private RedisAsyncCommands<byte[], byte[]> commands;
    private RedisClient redisClient;
    private Map<KeyBytes, WebSocketFrameHandler> handlers;
    private StatefulRedisPubSubConnection<byte[], byte[]> pubSubConnection;

    public RedisConnection() {
        redisClient = connect();

        ByteArrayCodec codec = new ByteArrayCodec();
        connection = redisClient.connect(codec);
        commands = connection.async();

        pubSubConnection = redisClient.connectPubSub(codec);
        pubSubConnection.addListener(new RedisListener());
        handlers = Collections.synchronizedMap(new TreeMap<>());
    }

    private RedisClient connect() {
        long startTime = System.currentTimeMillis();
        do {
            try {
                return RedisClient.create("redis://redis:6379/0");
            } catch (RedisConnectionException e) {
                if (System.currentTimeMillis() - startTime >TIMEOUT) {
                    throw e;
                } else {
                    log.info("Waiting for redis to start.");
                }
            }
        } while (true);
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }

    public void putMessage(PublicKey recipient, byte[] encryptedData) {
        byte[] recipientKey = recipientKey(recipient);
        commands.lpush(recipientKey, encryptedData);
        commands.publish(recipientKey, new byte[]{1});
    }

    private byte[] recipientKey(PublicKey recipient) {
        return recipient.getEncoded();
    }


    // TODO: Use listener interface instead of WebSocketFrameHandler?
    public void retrieveMessages(PublicKey recipient, WebSocketFrameHandler webSocketHandler) {
        byte[] recipientKey = recipientKey(recipient);
        commands.lpop(recipientKey).thenAccept(new MessageConsumer(webSocketHandler, recipientKey));
    }

    public void subscribeForUpdates(PublicKey recipient, WebSocketFrameHandler socketHandler) {
        byte[] recipientKey = recipientKey(recipient);
        handlers.put(new KeyBytes(recipientKey), socketHandler);
        log.info("Sockethandler: " + socketHandler);
        log.info("Subscribing: " + Arrays.toString(recipientKey));
        pubSubConnection.sync().subscribe(recipientKey);
    }

    public void unsubscribe(PublicKey recipient) {
        byte[] recipientKey = recipientKey(recipient);
        handlers.remove(new KeyBytes(recipientKey));
        log.info("Unubscribing: " + recipient);
        pubSubConnection.sync().unsubscribe(recipientKey);
    }

    private class RedisListener extends RedisPubSubAdapter<byte[], byte[]> {
        @Override
        public void message(byte[] channel, byte[] message) {
            WebSocketFrameHandler webSocketHandler = handlers.get(new KeyBytes(channel));

            log.info("onMessage: " + webSocketHandler);
            if (webSocketHandler != null) {
                commands.lpop(channel).thenAccept(new MessageConsumer(webSocketHandler, channel));
            }
        }
    }

    private class MessageConsumer implements Consumer<byte[]> {

        private WebSocketFrameHandler webSocketHandler;
        private byte[] recipientKey;

        MessageConsumer(WebSocketFrameHandler webSocketHandler, byte[] recipientKey) {
            this.webSocketHandler = webSocketHandler;
            this.recipientKey = recipientKey;
        }

        @Override
        public void accept(byte[] message) {
            if (message == null) {
                return;
            }
            webSocketHandler.sendMessage(new PayloadMessage(message));
            commands.lpop(recipientKey).thenAccept(this);
        }
    }

    private class KeyBytes implements Comparable<KeyBytes> {
        private final int cachedHashCode;
        private byte[] data;

        KeyBytes(byte[] data) {
            this.data = data;
            this.cachedHashCode = hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyBytes keyBytes = (KeyBytes) o;
            return Arrays.equals(data, keyBytes.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }

        @Override
        public int compareTo(KeyBytes bytes) {
            if (cachedHashCode != bytes.cachedHashCode) {
                return cachedHashCode - bytes.cachedHashCode;
            }
            if (data.length != bytes.data.length) {
                return data.length - bytes.data.length;
            }

            for (int i = 0; i < data.length; i++) {
                if (data[i] != bytes.data[i]) {
                    return data[i] - bytes.data[i];
                }
            }
            return 0;
        }
    }
}
