package com.github.f9c.client.datamessage;

import com.github.f9c.message.ByteBufferHelper;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.PROFILE_DATA_MESSAGE;
import static com.github.f9c.message.ByteBufferHelper.encodedSize;
import static com.github.f9c.message.ByteBufferHelper.put;
import static com.github.f9c.message.ByteBufferHelper.putString;

public class ProfileDataMessage extends AbstractDataMessage {

    private String alias;
    private String statusText;
    private byte[] profileImage;

    public ProfileDataMessage(String alias, String statusText, byte[] profileImage, PublicKey sender) {
        super(sender);

        this.alias = alias;
        this.statusText = statusText;
        this.profileImage = profileImage;
    }

    public ProfileDataMessage(UUID msgId, long timestamp, byte[] sender, String alias, String statusText, byte[] profileImage) {
        super(msgId, timestamp, sender);

        this.alias = alias;
        this.statusText = statusText;
        this.profileImage = profileImage;
    }

    @Override
    protected void writeData(ByteBuffer buf) {
        super.writeData(buf);
        putString(alias, buf);
        putString(statusText, buf);
        put(profileImage, buf);
    }

    @Override
    public int size() {
        return super.size() + encodedSize(alias) + encodedSize(statusText) + encodedSize(profileImage);
    }

    @Override
    protected int getOpcode() {
        return PROFILE_DATA_MESSAGE;
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
}
