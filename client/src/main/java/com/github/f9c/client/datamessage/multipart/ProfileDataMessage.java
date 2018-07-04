package com.github.f9c.client.datamessage.multipart;

import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.client.datamessage.DataMessageHeader;
import com.github.f9c.message.ByteBufferHelper;
import com.github.f9c.message.InputStreamHelper;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PublicKey;

import static com.github.f9c.client.datamessage.multipart.MultiPartMessageOpcodes.PROFILE_DATA_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;

public class ProfileDataMessage extends AbstractMultiPartMessage implements ClientMessage {

    private String alias;
    private String statusText;

    public ProfileDataMessage(PublicKey sender, String senderServer, String alias, String statusText, InputStream profileImage) {
        super(sender, senderServer, profileImage);
        this.alias = alias;
        this.statusText = statusText;
    }

    public ProfileDataMessage(DataMessageHeader header, InputStream in) {
        super(header, in);
    }


    @Override
    protected void readBaseData(DataInputStream dataInputStream) {
        alias = InputStreamHelper.readString(dataInputStream);
        statusText = InputStreamHelper.readString(dataInputStream);
    }

    @Override
    protected int getOpcode() {
        return PROFILE_DATA_MESSAGE;
    }

    @Override
    protected byte[] getBaseData() {
        byte[] result = new byte[encodedSize(alias) + encodedSize(statusText)];
        ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        ByteBufferHelper.putString(alias, byteBuffer);
        ByteBufferHelper.putString(statusText, byteBuffer);
        return result;
    }

    public String getAlias() {
        return alias;
    }

    public String getStatusText() {
        return statusText;
    }

    public InputStream getProfileImage() {
        return getAdditionalData();
    }
}
