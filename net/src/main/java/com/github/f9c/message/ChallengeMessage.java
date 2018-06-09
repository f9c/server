package com.github.f9c.message;

import static com.github.f9c.message.MessageType.CHALLENGE;

public class ChallengeMessage implements Message {

    private final byte[] challengeBytes;


    public ChallengeMessage(byte[] challengeBytes) {
        this.challengeBytes = challengeBytes;
    }

    @Override
    public MessageType getType() {
        return CHALLENGE;
    }

    @Override
    public byte[] data() {
        return challengeBytes;
    }
}
