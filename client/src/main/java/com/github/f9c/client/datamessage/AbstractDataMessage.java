package com.github.f9c.client.datamessage;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.put;
import static com.github.f9c.message.encryption.Crypt.decodeKey;

public abstract class AbstractDataMessage {
    private UUID msgId;
    private long timestamp;
    private byte[] senderPublicKey;

    AbstractDataMessage(PublicKey sender) {
        this.msgId = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.senderPublicKey = sender.getEncoded();
    }

    AbstractDataMessage(UUID msgId, long timestamp, byte[] sender) {
        this.msgId = msgId;
        this.timestamp = timestamp;
        this.senderPublicKey = sender;
    }

    public UUID getMsgId() {
        return msgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public PublicKey getSenderPublicKey() {
        return decodeKey(senderPublicKey);
    }

    public byte[] data() {
        byte[] data = new byte[size()];
        ByteBuffer buf = ByteBuffer.wrap(data);
        writeData(buf);
        return data;
    }

    protected void writeData(ByteBuffer buf) {
        buf.putInt(getOpcode());
        buf.putLong(msgId.getMostSignificantBits());
        buf.putLong(msgId.getLeastSignificantBits());
        buf.putLong(timestamp);
        put(senderPublicKey, buf);
    }

    protected abstract int getOpcode();

    public int size() {
        return 4 + (3 * 8) + encodedSize(senderPublicKey);
    }
}
