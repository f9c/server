package com.github.f9c.message;

public interface MessageOpcodes {
    int CHALLENGE_REQUEST = 1;
    int CHALLENGE = 2;
    int CHALLENGE_RESPONSE = 3;
    int TARGETED_DATA = 4;
    int DATA = 5;
    int CONNECTION_SUCCESSFUL = 6;
    int VERIFICATION_FAILED = 7;
    int UNSUPPORTED_PROTOCOL_VERSION = 8;
}
