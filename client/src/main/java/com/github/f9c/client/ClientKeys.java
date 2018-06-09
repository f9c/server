package com.github.f9c.client;

import com.github.f9c.message.encryption.Crypt;

import java.security.*;

public class ClientKeys {
    private KeyPair keys;

    public ClientKeys() {
        this.keys = generateKeys();
    }

    public ClientKeys(PublicKey publicKey, PrivateKey privateKey) {
        this.keys = new KeyPair(publicKey, privateKey);
    }

    private KeyPair generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(1024, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sign(byte[] data) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(keys.getPrivate());
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getPublicKey() {
        return keys.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keys.getPrivate();
    }
}
