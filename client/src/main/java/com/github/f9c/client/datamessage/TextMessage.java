package com.github.f9c.client.datamessage;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.TEXT_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.putString;

public class TextMessage extends AbstractDataMessage {
    private String msg;

    public TextMessage(String msg, PublicKey sender) {
        super(sender);
        this.msg = msg;
    }


    public TextMessage(UUID msgId, long timestamp, byte[] senderPublicKey, String msg) {
        super(msgId, timestamp, senderPublicKey);
        this.msg = msg;
    }

    @Override
    protected void writeData(ByteBuffer buf) {
        super.writeData(buf);
        putString(msg, buf);
    }

    @Override
    protected int getOpcode() {
        return TEXT_MESSAGE;
    }

    @Override
    public int size() {
        return super.size() + encodedSize(msg);
    }

    public String getMsg() {
        return msg;
    }
}
