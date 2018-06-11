package com.github.f9c;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientMessageListener;
import com.github.f9c.client.MessageClientEndpoint;
import com.github.f9c.client.datamessage.AbstractDataMessage;
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

public class Client {
    private MessageClientEndpoint endpoint;
    private WebSocket webSocket;
    private String uri;
    private  WebSocketFactory factory;

    public Client(String host, int port, ClientKeys keys, ClientMessageListener clientMessageListener) throws Exception {
        uri = "https://" + host + ":" + port + "/websocket";

        factory = createWebSocketFactory();
        endpoint = new MessageClientEndpoint(keys, clientMessageListener);

        webSocket = createWebSocket(uri, factory);
    }

    private WebSocketFactory createWebSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        WebSocketFactory factory = new WebSocketFactory();
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
        return factory;
    }

    private WebSocket createWebSocket(String uri, WebSocketFactory factory) throws IOException, WebSocketException {
        WebSocket webSocket = factory.createSocket(uri);
        webSocket.setMaxPayloadSize(64 * 1024);
        webSocket.addListener(endpoint);
        webSocket.connect();
        return webSocket;
    }

    public void sendDataMessage(PublicKey recipient, AbstractDataMessage msg) throws IOException, WebSocketException {
        if (!webSocket.isOpen()) {
            webSocket = createWebSocket(uri, factory);
        }

        waitForConnection();

        endpoint.sendDataMessage(recipient, msg);
    }

    public void waitForConnection() {
        endpoint.waitForConnection();
    }

    public void close() {
        webSocket.sendClose();
    }
}
