package com.github.f9c.client.datamessage.multipart;

import com.github.f9c.client.datamessage.ClientMessage;
import com.github.f9c.client.datamessage.DataMessageHeader;
import com.github.f9c.message.ByteBufferHelper;
import com.github.f9c.message.InputStreamHelper;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PublicKey;

import static com.github.f9c.client.datamessage.multipart.MultiPartMessageOpcodes.REQUEST_PROFILE_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;

/**
 * Request profile data from another client. This includes the profile data of the sender so the receiver can decide
 * whether this request will be granted based on the profile data.
 */
public class RequestProfileMessage extends AbstractMultiPartMessage implements ClientMessage {

    private String alias;
    private String statusText;

    public RequestProfileMessage(PublicKey sender, String senderServer, String alias, String statusText, InputStream profileImage) {
        super(sender, senderServer, profileImage);

        this.alias = alias;
        this.statusText = statusText;
    }

    public RequestProfileMessage(DataMessageHeader header, InputStream in) {
        super(header, in);
    }

    @Override
    protected void readBaseData(DataInputStream dataInputStream) {
        alias = InputStreamHelper.readString(dataInputStream);
        statusText = InputStreamHelper.readString(dataInputStream);
    }

    @Override
    protected byte[] getBaseData() {
        byte[] result = new byte[encodedSize(alias) + encodedSize(statusText)];
        ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        ByteBufferHelper.putString(alias, byteBuffer);
        ByteBufferHelper.putString(statusText, byteBuffer);
        return result;
    }

    @Override
    protected int getOpcode() {
        return REQUEST_PROFILE_MESSAGE;
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
