package com.github.f9c.bot;

import com.github.f9c.Client;
import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.ClientUrl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class F9cBot {
    private static final Logger log = Logger.getLogger(F9cBot.class.getName());

    public static final String PRIVATE_KEY_DAT = "privateKey.dat";
    public static final String PUBLIC_KEY_DAT = "publicKey.dat";

    public static void main(String[] args) throws Exception {
        // TODO: Wait until server port is open
        Thread.sleep(4000);
        if (args.length != 4)  {
            throw new IllegalArgumentException("Please specify the parameters: <configdir> <botdir> <botname> <server>");
        }

        String configDir = args[0];
        String botDir = args[1];
        String botName = args[2];
        String server = args[3];

        String domain = getDomain();
        if (getDomain() == null || "".equals(getDomain())) {
            System.err.println("No domain specified. Using localhost as send server.");
            domain = "localhost";
        }

        BotListener botListener = new BotListener(botDir, botName, domain);

        ClientKeys keys = initKeys(configDir, botName);

        Client client = new Client(server, 8443, keys, botListener);
        botListener.setClient(client);
        botListener.setClientKeys(keys);

        String encodedKey  = Base64.getUrlEncoder().encodeToString(keys.getPublicKey().getEncoded());

        System.out.println("Bot link: " + ClientUrl.createSharingUrl(botListener.getAlias(), encodedKey, domain));
    }

    private static String getDomain() {
        return System.getenv("F9C_DOMAIN");
    }

    private static ClientKeys initKeys(String configDir, String botName) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        String baseDir = configDir + File.separator + botName + File.separator;
        new File(baseDir).mkdirs();

        log.info("Using config dir " + baseDir);

        if (new File(baseDir + PRIVATE_KEY_DAT).exists() && new File(baseDir + PUBLIC_KEY_DAT).exists()) {
            log.info("Using existing keys.");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return new ClientKeys(keyFactory.generatePublic(new X509EncodedKeySpec(readFile(baseDir + PUBLIC_KEY_DAT))),
                    keyFactory.generatePrivate(new PKCS8EncodedKeySpec(readFile(baseDir + PRIVATE_KEY_DAT))));
        } else {
            log.info("Creating new keys.");
            ClientKeys keys = new ClientKeys();
            writeFile(keys.getPrivateKey().getEncoded(), baseDir + PRIVATE_KEY_DAT);
            writeFile(keys.getPublicKey().getEncoded(), baseDir + PUBLIC_KEY_DAT);
            return keys;
        }
    }

    private static void writeFile(byte[] encoded, String file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(encoded);
        }
    }

    private static byte[] readFile(String file) throws IOException {
        return Files.readAllBytes(Paths.get(file));
    }
}