package com.github.f9c.client.datamessage;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.TEXT_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.putString;

public class TextMessage extends AbstractDataMessage {
    private String msg;
    private String server;

    public TextMessage(String msg, PublicKey sender, String server) {
        super(sender);
        this.msg = msg;
        this.server = server;
    }


    public TextMessage(UUID msgId, long timestamp, byte[] senderPublicKey, String msg, String server) {
        super(msgId, timestamp, senderPublicKey);
        this.msg = msg;
        this.server = server;
    }

    @Override
    protected void writeData(ByteBuffer buf) {
        super.writeData(buf);
        putString(msg, buf);
        putString(server, buf);
    }

    @Override
    protected int getOpcode() {
        return TEXT_MESSAGE;
    }

    @Override
    public int size() {
        return super.size() + encodedSize(msg) + encodedSize(server);
    }

    public String getMsg() {
        return msg;
    }

    public String getServer() {
        return server;
    }
}
