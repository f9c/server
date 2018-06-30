package com.github.f9c.client;

import com.github.f9c.client.datamessage.AbstractDataMessage;

public interface ClientMessageListener {
    void handleDataMessage(AbstractDataMessage message);

    void handleError(Throwable cause);
}
