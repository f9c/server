package com.github.f9c.client.datamessage;

import com.github.f9c.message.ByteBufferHelper;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static com.github.f9c.message.ByteBufferHelper.*;

public class DataMessageHeader {
    private static final int MAX_SERVER_LENGTH = 20;
    private static final int MAX_PUBLIC_KEY_LENGTH = 4096;

    private int opcode;
    private UUID msgId;
    private long timestamp;
    private byte[] senderPublicKey;
    private String senderServer;
    private int dataSize;

    public DataMessageHeader(int opcode, UUID msgId, long timestamp, byte[] senderPublicKey, String senderServer, int dataSize) {
        this.opcode = opcode;
        this.msgId = msgId;
        this.timestamp = timestamp;
        this.senderPublicKey = senderPublicKey;
        this.senderServer = senderServer;
        this.dataSize = dataSize;
    }

    public DataMessageHeader(int opcode, PublicKey sender, String senderServer, int dataSize) {
        this.opcode = opcode;
        this.msgId = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.senderPublicKey = sender.getEncoded();
        this.senderServer = senderServer;
        this.dataSize = dataSize;
    }

    public static DataMessageHeader read(ByteBuffer content) {
        int opcode = content.getInt();
        UUID msgId = new UUID(content.getLong(), content.getLong());
        long timestamp = content.getLong();
        byte[] senderPublicKey = get(MAX_PUBLIC_KEY_LENGTH, content);
        String senderServer = getString(MAX_SERVER_LENGTH, content);
        int dataSize = content.getInt();
        return new DataMessageHeader(opcode, msgId, timestamp, senderPublicKey, senderServer, dataSize);
    }

    public UUID getMsgId() {
        return msgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getSenderPublicKey() {
        return senderPublicKey;
    }

    public String getSenderServer() {
        return senderServer;
    }

    public int getOpcode() {
        return opcode;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void updateDataSize(ByteBuffer buf, int dataSize) {
        buf.position(size() - 4);
        buf.putInt(dataSize);
    }

    public void write(ByteBuffer buf)  {
        buf.putInt(opcode);
        buf.putLong(msgId.getMostSignificantBits());
        buf.putLong(msgId.getLeastSignificantBits());
        buf.putLong(timestamp);
        put(senderPublicKey, buf);
        putString(senderServer, buf);
        buf.putInt(dataSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataMessageHeader that = (DataMessageHeader) o;
        return opcode == that.opcode &&
                timestamp == that.timestamp &&
                Objects.equals(msgId, that.msgId) &&
                Arrays.equals(senderPublicKey, that.senderPublicKey) &&
                Objects.equals(senderServer, that.senderServer);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(opcode, msgId, timestamp, senderServer);
        result = 31 * result + Arrays.hashCode(senderPublicKey);
        return result;
    }

    public int size() {
        return 4 + (3 * 8) + encodedSize(senderPublicKey) + encodedSize(senderServer) + 4;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }
}
