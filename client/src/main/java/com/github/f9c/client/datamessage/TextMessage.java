package com.github.f9c.client.datamessage;

import com.github.f9c.message.TargetedPayloadMessage;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.TEXT_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.putString;

public class TextMessage extends AbstractDataMessage implements ClientMessage {
    private String msg;

    public TextMessage(String msg, PublicKey sender, String server) {
        super(sender, server);
        this.msg = msg;
    }


    public TextMessage(DataMessageHeader header, String msg) {
        super(header);
        this.msg = msg;
    }

    protected void writeData(ByteBuffer buf) {
        writeHeader(buf, encodedSize(msg));
        putString(msg, buf);
    }

    @Override
    protected int getOpcode() {
        return TEXT_MESSAGE;
    }

    public String getMsg() {
        return msg;
    }

    public byte[] data() {
        byte[] data = new byte[size()];
        ByteBuffer buf = ByteBuffer.wrap(data);
        writeData(buf);
        return data;
    }

    public int size() {
        return getHeader().size() + encodedSize(msg);
    }

    public Iterator<TargetedPayloadMessage> createPayloadMessages(PrivateKey privateKey, PublicKey recipient) {
        return Arrays.asList(DataMessageFactory.createTargetedPayloadMessage(privateKey, recipient, this.data())).iterator();
    }

}
