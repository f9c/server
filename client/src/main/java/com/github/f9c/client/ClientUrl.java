package com.github.f9c.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ClientUrl {
    public static String createSharingUrl(String alias, String publicKey, String server) {
        try {
            return "https://f9c.eu?alias=" +  URLEncoder.encode(alias, StandardCharsets.UTF_8.name()) + "&publicKey=" + publicKey + "&server=" + server;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
