package com.github.f9c;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientMessageListener;
import com.github.f9c.client.MessageClientEndpoint;
import com.github.f9c.client.datamessage.AbstractDataMessage;
import com.github.f9c.client.datamessage.DataMessageFactory;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Client {
    private MessageClientEndpoint endpoint;
    private WebSocket webSocket;

    public Client(String host, int port, ClientKeys keys, ClientMessageListener clientMessageListener) throws Exception {
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
        webSocket = factory.createSocket("https://" + host + ":" + port + "/websocket");
        webSocket.setMaxPayloadSize(64 * 1024);
        endpoint = new MessageClientEndpoint(keys, clientMessageListener);
        webSocket.addListener(endpoint);
        webSocket.connect();
    }

    public void sendDataMessage(PublicKey recipient, AbstractDataMessage msg) {
        endpoint.sendDataMessage(recipient, msg);
    }

    public void waitForConnection() {
        endpoint.waitForConnection();
    }

    public void close() {
        webSocket.sendClose();
    }
}
