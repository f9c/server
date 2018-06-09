package com.github.f9c.client;

import com.github.f9c.client.datamessage.AbstractDataMessage;

@FunctionalInterface
public interface ClientMessageListener {
    void handleDataMessage(AbstractDataMessage message);
}
