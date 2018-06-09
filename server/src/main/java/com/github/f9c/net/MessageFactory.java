package com.github.f9c.net;

import com.github.f9c.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class MessageFactory {

    public static Message readMessage(BinaryWebSocketFrame frame) {
        ByteBuf content = frame.content();
        content.readableBytes();
        int opcode = content.readInt();
        switch (opcode) {
            case MessageOpcodes.CHALLENGE_REQUEST: return new ChallengeRequestMessage(content.readInt(), readKey(content));
            case MessageOpcodes.CHALLENGE_RESPONSE: return new ChallengeResponseMessage(readSignature(content));
            case MessageOpcodes.TARGETED_DATA: return new TargetedPayloadMessage(readArray(content), readArray(content));
            default: throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }
    }

    private static byte[] readArray(ByteBuf content) {
        int size = content.readInt();

        byte[] result = new byte[size];
        content.readBytes(result);
        return result;
    }

    private static PublicKey readKey(ByteBuf buffer) {
        byte[] keyData = new byte[buffer.readableBytes()];
        buffer.readBytes(keyData);

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyData);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static byte[] readSignature(ByteBuf buffer) {
        byte[] keyData = new byte[buffer.readableBytes()];
        buffer.readBytes(keyData);
        return keyData;
    }
}
