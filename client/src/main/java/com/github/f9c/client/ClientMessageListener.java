package com.github.f9c.client;

import com.github.f9c.client.datamessage.ClientMessage;

public interface ClientMessageListener {
    void handleDataMessage(ClientMessage message);

    void handleError(Throwable cause);
}
