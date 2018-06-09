package com.github.f9c.message;

public enum MessageType {
    CHALLENGE_REQUEST(MessageOpcodes.CHALLENGE_REQUEST),
    CHALLENGE(MessageOpcodes.CHALLENGE),
    CHALLENGE_RESPONSE(MessageOpcodes.CHALLENGE_RESPONSE),
    TARGETED_DATA(MessageOpcodes.TARGETED_DATA),
    PAYLOAD(MessageOpcodes.DATA),
    CONNECTION_SUCCESSFUL(MessageOpcodes.CONNECTION_SUCCESSFUL),
    VERIFICATION_FAILED(MessageOpcodes.VERIFICATION_FAILED),
    UNSUPPORTED_PROTOCOL_VERSION(MessageOpcodes.UNSUPPORTED_PROTOCOL_VERSION);

    private int opcode;

    MessageType(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

}
