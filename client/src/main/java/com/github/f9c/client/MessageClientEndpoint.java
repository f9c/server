package com.github.f9c.client;

import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.client.datamessage.DataMessageFactory;
import com.github.f9c.message.*;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageClientEndpoint extends WebSocketAdapter {
    private static final long CONNECT_TIMEOUT = 15000;
    private ClientKeys clientKeys;
    private Logger log = Logger.getLogger(MessageClientEndpoint.class.getSimpleName());

    private ClientConnectionStatus status;
    private WebSocket webSocket;
    private ClientMessageListener clientMessageListener;
    private DataMessageFactory dataMessageFactory;

    public MessageClientEndpoint(ClientKeys clientKeys, ClientMessageListener clientMessageListener) {
        this.clientKeys = clientKeys;
        status = ClientConnectionStatus.INITIALIZING;
        this.clientMessageListener = clientMessageListener;
        this.dataMessageFactory = new DataMessageFactory();
    }

    @Override
    public void onConnected(WebSocket webSocket, Map<String, List<String>> headers) {
        log.info("Requesting challenge for own key.");
        this.webSocket = webSocket;
        sendMessage(new ChallengeRequestMessage(ChallengeRequestMessage.PROTOCOL_VERSION_1, clientKeys.getPublicKey()));
    }

    private void sendMessage(Message msg) {
        byte[] data = msg.data();

        ByteBuffer bb = ByteBuffer.allocate(data.length + 4);
        bb.putInt(msg.getType().getOpcode());
        bb.put(data);
        bb.rewind();

        byte[] arr = new byte[bb.remaining()];
        bb.get(arr);
        webSocket.sendBinary(arr);
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] data) {
        Message message = ClientMessageFactory.readMessage(ByteBuffer.wrap(data));
        int opcode = message.getType().getOpcode();
        switch (opcode) {
            case MessageOpcodes.CHALLENGE:
                handleChallenge((ChallengeMessage) message);
                return;
            case MessageOpcodes.DATA:
                byte[] decryptedBytes = ((PayloadMessage) message).decrypt(clientKeys.getPrivateKey());
                ClientMessage clientMessage = dataMessageFactory.readMessage(ByteBuffer.wrap(decryptedBytes));
                if (clientMessage != null) {
                    clientMessageListener.handleDataMessage(clientMessage);
                }
            case MessageOpcodes.CONNECTION_SUCCESSFUL:
                setStatus(ClientConnectionStatus.CONNECTED);
                return;
            default:
                throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }

    }

    private synchronized void setStatus(ClientConnectionStatus status) {
        this.status = status;
        notifyAll();
    }

    private void handleChallenge(ChallengeMessage message) {
        sendMessage(new ChallengeResponseMessage(clientKeys.sign(message.data())));
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        this.status = ClientConnectionStatus.CLOSED;
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) {
        websocket.sendClose();
        log.log(Level.SEVERE, "Unexpected websocket error.", cause);
        clientMessageListener.handleError(cause);
    }


    public synchronized void waitForConnection() {
        try {
            long waitUntil = System.currentTimeMillis() + CONNECT_TIMEOUT;
            do {
                if (ClientConnectionStatus.CONNECTED.equals(status)) {
                    return;
                }
                wait(CONNECT_TIMEOUT);
            } while (waitUntil < System.currentTimeMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDataMessage(PublicKey recipient, ClientMessage msg) {
        waitForConnection();
        Iterator<TargetedPayloadMessage>  it = msg.createPayloadMessages(clientKeys.getPrivateKey(), recipient);
        while (it.hasNext()) {
            sendMessage(it.next());
        }
    }
}
