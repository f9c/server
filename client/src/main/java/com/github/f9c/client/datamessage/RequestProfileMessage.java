package com.github.f9c.client.datamessage;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.REQUEST_PROFILE_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.put;
import static com.github.f9c.message.ByteBufferHelper.putString;

/**
 * Request profile data from another client. This includes the profile data of the sender so the receiver can decide
 * whether this request will be granted based on the profile data.
 */
public class RequestProfileMessage extends AbstractDataMessage  {

    private String server;
    private String alias;
    private String statusText;
    private byte[] profileImage;

    public RequestProfileMessage(String alias, String statusText, byte[] profileImage, PublicKey sender, String server) {
        super(sender);

        this.server = server;
        this.alias = alias;
        this.statusText = statusText;
        this.profileImage = profileImage;
    }

    public RequestProfileMessage(UUID msgId, long timestamp, byte[] sender, String server, String alias, String statusText, byte[] profileImage) {
        super(msgId, timestamp, sender);

        this.alias = alias;
        this.statusText = statusText;
        this.profileImage = profileImage;
        this.server = server;
    }


    @Override
    protected void writeData(ByteBuffer buf) {
        super.writeData(buf);
        putString(server, buf);
        putString(alias, buf);
        putString(statusText, buf);
        put(profileImage, buf);
    }

    @Override
    public int size() {
        return super.size() +  encodedSize(server) + encodedSize(alias) + encodedSize(statusText) + encodedSize(profileImage);
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

    public byte[] getProfileImage() {
        return profileImage;
    }

    public String getServer() {
        return server;
    }
}
