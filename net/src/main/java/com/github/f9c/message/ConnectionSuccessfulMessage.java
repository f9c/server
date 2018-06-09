package com.github.f9c.message;

public class ConnectionSuccessfulMessage implements Message {
    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_SUCCESSFUL;
    }

    @Override
    public byte[] data() {
        return new byte[0];
    }
}
