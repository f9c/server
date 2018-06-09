package com.github.f9c.message;

import com.github.f9c.message.encryption.Crypt;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

public class TargetedPayloadMessage implements Message {

    private final PublicKey recipient;
    private final byte[] encryptedData;

    public TargetedPayloadMessage(PublicKey recipient, byte[] data) {
        try {
            this.recipient = recipient;
            this.encryptedData = Crypt.encrypt(recipient, data);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public TargetedPayloadMessage(byte[] recipient, byte[] encryptedData) {
        this.recipient = Crypt.decodeKey(recipient);
        this.encryptedData = encryptedData;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    @Override
    public MessageType getType() {
        return MessageType.TARGETED_DATA;
    }

    @Override
    public byte[] data() {
        byte[] recipient = this.recipient.getEncoded();
        byte[] resultData = new byte[recipient.length + this.encryptedData.length + 8];
        ByteBuffer buf = ByteBuffer.wrap(resultData);
        buf.putInt(recipient.length);
        buf.put(recipient);
        buf.putInt(encryptedData.length);
        buf.put(encryptedData);
        return buf.array();
    }

}
