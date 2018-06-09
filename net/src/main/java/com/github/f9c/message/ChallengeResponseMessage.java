package com.github.f9c.message;

public class ChallengeResponseMessage implements Message {

    private byte[] signature;

    public ChallengeResponseMessage(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public MessageType getType() {
        return MessageType.CHALLENGE_RESPONSE;
    }

    @Override
    public byte[] data() {
        return signature;
    }
}
