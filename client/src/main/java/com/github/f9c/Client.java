package com.github.f9c;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientMessageListener;
import com.github.f9c.client.AuthenticatedClientEndpoint;
import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.message.MessageHelper;
import com.github.f9c.message.TargetedPayloadMessage;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Client {
    private static final int DEFAULT_PORT = 443;
    private final ClientKeys keys;
    private final boolean trustAllSslCertificates;

    private AuthenticatedClientEndpoint endpoint;
    private WebSocket primaryServerSocket;
    /** Map from host name to socket connection. */
    private Map<String, WebSocket> secondarySockets;
    private String uri;
    private WebSocketFactory factory;
    private String host;

    public Client(String host, ClientKeys keys, ClientMessageListener clientMessageListener) throws Exception {
        this(host, keys, clientMessageListener, false);
    }

    /**
     * @param trustAllSslCertificates This should only be set when working on a local server that does not have
     *                                a valid certificate.
     */
    public Client(String host, ClientKeys keys, ClientMessageListener clientMessageListener, boolean trustAllSslCertificates) throws Exception {
        this.host = host;
        this.uri = createUri(host, DEFAULT_PORT);
        this.keys = keys;
        this.secondarySockets = new HashMap<>();
        this.trustAllSslCertificates = trustAllSslCertificates;

        factory = createWebSocketFactory();
        endpoint = new AuthenticatedClientEndpoint(keys, clientMessageListener);

        primaryServerSocket = createWebSocket(uri, factory, endpoint);
    }

    private String createUri(String host, int port) {
        return "https://" + host + ":" + port + "/websocket";
    }

    private WebSocketFactory createWebSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        WebSocketFactory factory = new WebSocketFactory();

        if (trustAllSslCertificates) {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            factory.setSSLContext(sslContext);
            factory.setVerifyHostname(false);
        }

        return factory;
    }

    private WebSocket createWebSocket(String uri, WebSocketFactory factory, AuthenticatedClientEndpoint endpoint) throws IOException, WebSocketException {
        WebSocket webSocket = factory.createSocket(uri);
        webSocket.setMaxPayloadSize(64 * 1024);
        if (endpoint != null) {
            webSocket.addListener(endpoint);
        }
        webSocket.connect();
        return webSocket;
    }

    public void sendDataMessage(PublicKey recipient, String recipientServer, ClientMessage msg) throws IOException, WebSocketException {
        if (recipientServer.equals(host)) {
            sendDataMessageToPrimaryServer(recipient, msg);
        } else {
            sendDataMessageToSecondaryServer(recipient, recipientServer, msg);
        }
    }

    private void sendDataMessageToPrimaryServer(PublicKey recipient, ClientMessage msg) throws IOException, WebSocketException {
        if (!primaryServerSocket.isOpen()) {
            primaryServerSocket = createWebSocket(uri, factory, null);
        }

        waitForConnection();

        endpoint.sendDataMessage(recipient, msg);
    }

    private void sendDataMessageToSecondaryServer(PublicKey recipient, String recipientServer, ClientMessage msg) throws IOException, WebSocketException {
        WebSocket webSocket = secondarySockets.get(recipientServer);
        if (webSocket == null || !webSocket.isOpen()) {
            webSocket = createWebSocket(createUri(recipientServer, DEFAULT_PORT), factory, null);
            secondarySockets.put(recipientServer, webSocket);
        }

        Iterator<TargetedPayloadMessage> it = msg.createPayloadMessages(keys.getPrivateKey(), recipient);
        while (it.hasNext()) {
            webSocket.sendBinary(MessageHelper.toBinary(it.next()));
        }
    }

    public void waitForConnection() {
        endpoint.waitForConnection();
    }

    public void close() {
        primaryServerSocket.sendClose();
    }

    public String getHost() {
        return host;
    }
}
