package com.github.f9c.client.datamessage.multipart;

import com.github.f9c.client.datamessage.DataMessageHeader;
import com.github.f9c.client.datamessage.DataMessageOpcodes;
import com.github.f9c.client.datamessage.MultiPartDataMessage;
import com.github.f9c.message.TargetedPayloadMessage;

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.stream.Stream;

public abstract class AbstractMultiPartMessage {

    private DataMessageHeader header;
    private InputStream additionalData;

    public AbstractMultiPartMessage(PublicKey sender, String senderServer, InputStream additionalData) {
        this.additionalData = additionalData;
        this.header = new DataMessageHeader(DataMessageOpcodes.MULTI_PART, sender, senderServer, 0);
    }

    public AbstractMultiPartMessage(DataMessageHeader header, InputStream additionalData) {
        this.header = header;
        this.additionalData = additionalData;

        readBaseData(new DataInputStream(additionalData));
    }

    protected abstract void readBaseData(DataInputStream dataInputStream);

    protected abstract int getOpcode();

    protected abstract byte[] getBaseData();

    protected InputStream getAdditionalData() {
        return additionalData;
    }

    public MultiPartDataMessage toMultiPartDataMessage() {
        return new MultiPartDataMessage(header, getOpcode(), getBaseData(), additionalData);
    }

    public DataMessageHeader getHeader() {
        return header;
    }

    public Stream<TargetedPayloadMessage> createPayloadMessages(PrivateKey privateKey, PublicKey recipient) {
        return toMultiPartDataMessage().createPayloadMessages(privateKey, recipient);
    }
}
