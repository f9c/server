package com.github.f9c.message;

public class UnsupportedProtocolVersionMessage implements Message {
    @Override
    public MessageType getType() {
        return MessageType.UNSUPPORTED_PROTOCOL_VERSION;
    }

    @Override
    public byte[] data() {
        return new byte[0];
    }
}
