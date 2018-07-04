package com.github.f9c.client.datamessage;

import com.github.f9c.message.TargetedPayloadMessage;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.stream.Stream;

public interface ClientMessage {
    Stream<TargetedPayloadMessage> createPayloadMessages(PrivateKey privateKey, PublicKey recipient);
}
