package com.github.f9c.client.datamessage;

import com.github.f9c.client.datamessage.multipart.MultiPartMessageOpcodes;
import com.github.f9c.client.datamessage.multipart.ProfileDataMessage;
import com.github.f9c.client.datamessage.multipart.RequestProfileMessage;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.github.f9c.message.ByteBufferHelper.allRemainingData;
import static com.github.f9c.message.ByteBufferHelper.getString;
import static com.github.f9c.message.encryption.Crypt.decodeKey;
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

    private MultiPartDataDecoder decoder = new MultiPartDataDecoder();

    public ClientMessage readMessage(ByteBuffer content) {

        DataMessageHeader header = DataMessageHeader.read(content);

        int msgDataStart = content.position();

        content.position(msgDataStart + header.getDataSize());

        byte[] signature = allRemainingData(content);

        // limit buffer to data without signature for signature verification
        content.limit(msgDataStart + header.getDataSize());
        content.position(0);
        verifySignature(decodeKey(header.getSenderPublicKey()), content, signature);

        content.position(msgDataStart);

        ClientMessage result;
        switch (header.getOpcode()) {
            case DataMessageOpcodes.TEXT_MESSAGE:
                result = readTextMessage(header, content);
                break;
            case DataMessageOpcodes.MULTI_PART:
                result = readMultiPartMessage(header, content);
                break;
            default:
                throw new IllegalArgumentException("Unsupported opcode: " + header.getOpcode());
        }

        return result;
    }

    private ClientMessage readMultiPartMessage(DataMessageHeader header, ByteBuffer content) {
        MultiPartDataMessageHeader multiPartHeader = new MultiPartDataMessageHeader(content);

        byte[] data = new byte[header.getDataSize() - multiPartHeader.size()];
        content.get(data);
        InputStream inputStream = decoder.add(header, multiPartHeader, data);

        if (inputStream != null) {
            switch (multiPartHeader.getMultiOpcode()) {

                case MultiPartMessageOpcodes.REQUEST_PROFILE_MESSAGE:
                    return new RequestProfileMessage(header, inputStream);

                case MultiPartMessageOpcodes.PROFILE_DATA_MESSAGE:
                    return new ProfileDataMessage(header, inputStream);

                default:
                    new IllegalArgumentException("Unsupported multi opcode: " + header.getOpcode());
            }

        }
        return null;
    }


    private ClientMessage readTextMessage(DataMessageHeader header, ByteBuffer content) {
        return new TextMessage(header, getString(MAX_STRING_LENGTH, content));
    }


    public static TargetedPayloadMessage createTargetedPayloadMessage(PrivateKey sender, PublicKey recipient, byte[] msgData) {
        byte[] signatureData = Crypt.sign(sender, msgData);

        byte[] resultData = new byte[msgData.length + signatureData.length];
        ByteBuffer buf = ByteBuffer.wrap(resultData);
        buf.put(msgData);
        buf.put(signatureData);

        return new TargetedPayloadMessage(recipient, resultData);
    }

}
