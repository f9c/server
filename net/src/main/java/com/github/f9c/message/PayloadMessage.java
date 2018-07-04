package com.github.f9c.message;

import com.github.f9c.message.encryption.Crypt;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;

public class PayloadMessage implements Message {

    private final byte[] encryptedData;

    public PayloadMessage(byte[] encryptedData) {
        this.encryptedData = encryptedData;

    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    @Override
    public MessageType getType() {
        return MessageType.PAYLOAD;
    }

    @Override
    public byte[] data() {
        return encryptedData;
    }

    public byte[] decrypt(PrivateKey privateKey) {
        try {
            return Crypt.decrypt(privateKey, encryptedData);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
