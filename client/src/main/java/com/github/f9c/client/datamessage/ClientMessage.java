package com.github.f9c.client.datamessage;

import com.github.f9c.message.TargetedPayloadMessage;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;

public interface ClientMessage {
    Iterator<TargetedPayloadMessage> createPayloadMessages(PrivateKey privateKey, PublicKey recipient);

    DataMessageHeader getHeader();
}
