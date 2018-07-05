package com.github.f9c.client;

import com.github.f9c.Client;
import com.github.f9c.client.datamessage.AbstractDataMessage;
import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.client.datamessage.DataMessageFactory;
import com.github.f9c.client.datamessage.TextMessage;
import com.github.f9c.message.PayloadMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class ClientIntegrationTest {
    private static final int TEST_PORT = 8443;

    @Test
    void shouldOpenConnection() throws Exception {
        ClientKeys clientKeys = new ClientKeys();

        DummyListener clientMessageListener = new DummyListener();
        String server = "127.0.0.1";
        Client client = new Client(server, TEST_PORT, clientKeys, clientMessageListener);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            client.sendDataMessage(clientKeys.getPublicKey(), server,
                    new TextMessage("Hello Server!", clientKeys.getPublicKey(), server));
        }

        System.out.println("Writing took: " + (System.currentTimeMillis() - time));

        for (int i = 0; i < 10000; i++) {
            TextMessage received = (TextMessage) clientMessageListener.waitForMessage();
            Assertions.assertEquals(received.getMsg(), "Hello Server!");
        }

        System.out.println("Reading took: " + (System.currentTimeMillis() - time));

        client.close();
    }

    private static class DummyListener implements ClientMessageListener {

        private List<ClientMessage> messages = new ArrayList<>();

        @Override
        public synchronized void handleDataMessage(ClientMessage message) {
            messages.add(message);
            notifyAll();
        }

        @Override
        public void handleError(Throwable cause) {
            // ignore for test
        }


        synchronized ClientMessage waitForMessage() throws InterruptedException {
            if (messages.isEmpty()) {
                wait(5000);
            }

            if (messages.isEmpty()) {
                throw new IllegalStateException("No message received!");
            }

            return messages.get(0);
        }
    }
}