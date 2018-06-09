package com.github.f9c.message;

import java.nio.ByteBuffer;
import java.security.PublicKey;

public class ChallengeRequestMessage implements Message {
    public static final int PROTOCOL_VERSION_1 = 1;

    private int protocolVersion = PROTOCOL_VERSION_1;
    private PublicKey key;

    public ChallengeRequestMessage(int protocolVersion, PublicKey key) {
        this.key = key;
        this.protocolVersion = protocolVersion;
    }

    public PublicKey getKey() {
        return key;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHALLENGE_REQUEST;
    }

    @Override
    public byte[] data() {
        byte[] keyEncoded = key.getEncoded();
        byte[] resultData = new byte[keyEncoded.length + 4];
        ByteBuffer buf = ByteBuffer.wrap(resultData);
        buf.putInt(protocolVersion);
        buf.put(keyEncoded);
        return buf.array();
    }


}
