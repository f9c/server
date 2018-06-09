package com.github.f9c.client;

import com.github.f9c.message.*;


import java.nio.ByteBuffer;

import static com.github.f9c.message.ByteBufferHelper.allRemainingData;

public class ClientMessageFactory {

    public static Message readMessage(ByteBuffer content) {
        int opcode = content.getInt();
        switch (opcode) {
            case MessageOpcodes.CHALLENGE:
                return readChallenge(content);
            case MessageOpcodes.DATA:
                return readData(content);
            case MessageOpcodes.CONNECTION_SUCCESSFUL:
                return new ConnectionSuccessfulMessage();
            case MessageOpcodes.VERIFICATION_FAILED:
                return new VerificationFailedMessage();
            default:
                throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }
    }

    private static Message readData(ByteBuffer content) {
        return new PayloadMessage(allRemainingData(content));
    }

    private static Message readChallenge(ByteBuffer content) {
        return new ChallengeMessage(allRemainingData(content));
    }
}
