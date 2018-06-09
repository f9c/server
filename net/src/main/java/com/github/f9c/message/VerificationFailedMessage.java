package com.github.f9c.message;

public class VerificationFailedMessage implements Message {
    @Override
    public MessageType getType() {
        return MessageType.VERIFICATION_FAILED;
    }

    @Override
    public byte[] data() {
        return new byte[0];
    }
}
