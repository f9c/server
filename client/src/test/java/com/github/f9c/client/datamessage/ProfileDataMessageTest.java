package com.github.f9c.client.datamessage;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.datamessage.multipart.ProfileDataMessage;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileDataMessageTest {
    private ClientKeys senderKeys = new ClientKeys();
    private ClientKeys recipientKeys = new ClientKeys();

    @Test
    public void shouldBeSerializable() throws Exception {

        ProfileDataMessage pdm = new ProfileDataMessage(senderKeys.getPublicKey(), "some.server", "someAlias", "someStatus", createImageStream(3));

        Iterator<TargetedPayloadMessage> payloadMessages = pdm.createPayloadMessages(senderKeys.getPrivateKey(), recipientKeys.getPublicKey());

        ProfileDataMessage deserialized = deserialize(payloadMessages);

        assertEquals("someAlias", deserialized.getAlias());
        assertEquals("someStatus", deserialized.getStatusText());
        InputStream profileImage = deserialized.getProfileImage();

        assertEquals(1, profileImage.read());
        assertEquals(2, profileImage.read());
        assertEquals(3, profileImage.read());
        assertEquals(-1, profileImage.read());

        assertEquals(senderKeys.getPublicKey(), Crypt.decodeKey(deserialized.getHeader().getSenderPublicKey()));
    }

    private ByteArrayInputStream createImageStream(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) i;
        }
        return new ByteArrayInputStream(result);
    }

    private ProfileDataMessage deserialize(Iterator<TargetedPayloadMessage> payloadMessages) throws GeneralSecurityException {
        DataMessageFactory messageFactory = new DataMessageFactory();

        ProfileDataMessage deserialized = null;

        while (payloadMessages.hasNext()) {
            TargetedPayloadMessage msg = payloadMessages.next();

            byte[] msgData = Crypt.decrypt(recipientKeys.getPrivateKey(), msg.getEncryptedData());
            ByteBuffer byteBuffer = ByteBuffer.wrap(msgData);

            ClientMessage result = messageFactory.readMessage(byteBuffer);
            if (result != null) {
                deserialized = (ProfileDataMessage) result;
            }
        }
        return deserialized;
    }


    @Test
    public void shouldSupportLargeMessages() throws Exception {

        int messageSize = 100000;
        ProfileDataMessage pdm = new ProfileDataMessage(senderKeys.getPublicKey(), "some.server", "someAlias", "someStatus", createImageStream(messageSize));

        Iterator<TargetedPayloadMessage> payloadMessages = pdm.createPayloadMessages(senderKeys.getPrivateKey(), recipientKeys.getPublicKey());

        ProfileDataMessage deserialized = deserialize(payloadMessages);

        InputStream profileImage = deserialized.getProfileImage();

        for (int i=0; i<messageSize; i++) {
            assertEquals((byte) i, (byte) profileImage.read());
        }

        assertEquals(-1, profileImage.read());
    }
}