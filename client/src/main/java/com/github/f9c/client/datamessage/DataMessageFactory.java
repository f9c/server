package com.github.f9c.client.datamessage;

import com.github.f9c.message.ByteBufferHelper;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import static com.github.f9c.message.ByteBufferHelper.allRemainingData;
import static com.github.f9c.message.encryption.Crypt.verifySignature;

/**
 * Data messages are the messages contained inside the com.github.f9c.message.PayloadMessage.
 * All of the data contained in the PayloadMessage is encrypted and not visible to the server.
 */
public class DataMessageFactory {
    private static final int MAX_DATA_LENGTH = 64000;
    private static final int MAX_STRING_LENGTH = 4096;
    private static final int MAX_ALIAS_LENGTH = 40;
    private static final int MAX_STATUS_LENGTH = 100;
    private static final int MAX_SERVER_LENGTH = 20;
    private static final int MAX_PUBLIC_KEY_LENGTH = 4096;

    public static AbstractDataMessage readMessage(ByteBuffer content) {
        int opcode = content.getInt();
        UUID msgId = new UUID(content.getLong(), content.getLong());
        long timestamp = content.getLong();
        byte[] senderPublicKey = ByteBufferHelper.get(MAX_PUBLIC_KEY_LENGTH, content);

        AbstractDataMessage result;
        switch (opcode) {
            case DataMessageOpcodes.TEXT_MESSAGE:
                result = readTextMessage(msgId, timestamp, senderPublicKey, content);
                break;
            case DataMessageOpcodes.REQUEST_PROFILE_MESSAGE:
                result = readRequestProfileMessage(msgId, timestamp, senderPublicKey, content);
                break;
            case DataMessageOpcodes.PROFILE_DATA_MESSAGE:
                result = readProfileDataMessage(msgId, timestamp, senderPublicKey, content);
                break;
            default:
                throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }

        verifySignature(result.getSenderPublicKey(), result.data(), allRemainingData(content));

        return result;
    }


    private static TextMessage readTextMessage(UUID msgId, long timestamp, byte[] senderPublicKey, ByteBuffer content) {
        String msgText = ByteBufferHelper.getString(MAX_STRING_LENGTH, content);
        String server = ByteBufferHelper.getString(MAX_SERVER_LENGTH, content);
        return new TextMessage(msgId, timestamp, senderPublicKey, msgText, server);
    }

    private static RequestProfileMessage readRequestProfileMessage(UUID msgId, long timestamp, byte[] senderPublicKey, ByteBuffer content) {
        String server = ByteBufferHelper.getString(MAX_SERVER_LENGTH, content);
        String alias = ByteBufferHelper.getString(MAX_ALIAS_LENGTH, content);
        String statusText = ByteBufferHelper.getString(MAX_STATUS_LENGTH, content);
        byte[] profileImage = ByteBufferHelper.get(MAX_DATA_LENGTH, content);

        return new RequestProfileMessage(msgId, timestamp, senderPublicKey, server, alias, statusText, profileImage);
    }

    private static ProfileDataMessage readProfileDataMessage(UUID msgId, long timestamp, byte[] senderPublicKey, ByteBuffer content) {
        String alias = ByteBufferHelper.getString(MAX_ALIAS_LENGTH, content);
        String statusText = ByteBufferHelper.getString(MAX_STATUS_LENGTH, content);
        byte[] profileImage = ByteBufferHelper.get(MAX_DATA_LENGTH, content);

        return new ProfileDataMessage(msgId, timestamp, senderPublicKey, alias, statusText, profileImage);
    }


    public static TargetedPayloadMessage createTargetedPayloadMessage(PrivateKey sender, PublicKey recipient, AbstractDataMessage message) {
        byte[] msgData = message.data();
        byte[] signatureData = Crypt.sign(sender, msgData);

        byte[] resultData = new byte[msgData.length + signatureData.length];
        ByteBuffer buf = ByteBuffer.wrap(resultData);
        buf.put(msgData);
        buf.put(signatureData);

        return new TargetedPayloadMessage(recipient, resultData);
    }

}
