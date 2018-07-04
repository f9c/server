package com.github.f9c.client.datamessage;

import com.github.f9c.message.TargetedPayloadMessage;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.f9c.message.encryption.Crypt.decodeKey;

/**
 * Abstract base class for data messages. Data messages are the messages in the encrypted blop of the
 * PayloadMessage so the content is not known to the server.
 */
public abstract class AbstractDataMessage {
    private DataMessageHeader header;

    AbstractDataMessage(PublicKey sender, String senderServer) {
        header = new DataMessageHeader(getOpcode(), sender, senderServer, 0);
    }

    AbstractDataMessage(DataMessageHeader header) {
        this.header = header;
    }

    public UUID getMsgId() {
        return header.getMsgId();
    }

    public long getTimestamp() {
        return header.getTimestamp();
    }

    public PublicKey getSenderPublicKey() {
        return decodeKey(header.getSenderPublicKey());
    }

    public byte[] getRawSenderPublicKey() {
        return header.getSenderPublicKey();
    }

    protected abstract int getOpcode();

    protected void writeHeader(ByteBuffer buf, int dataSize)  {
        header.setDataSize(dataSize);
        header.write(buf);
    }

    public DataMessageHeader getHeader() {
        return header;
    }

    public abstract Stream<TargetedPayloadMessage> createPayloadMessages(PrivateKey privateKey, PublicKey recipient);
}
