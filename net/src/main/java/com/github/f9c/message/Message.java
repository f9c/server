package com.github.f9c.message;

public interface Message {
    MessageType getType();

    byte[] data();
}
