package com.github.f9c.client.datamessage;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.datamessage.multipart.ProfileDataMessage;
import com.github.f9c.message.Message;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileDataMessageTest {

    @Test
    public void shouldBeSerializable() throws Exception {
        ClientKeys senderKeys = new ClientKeys();
        ClientKeys recipientKeys = new ClientKeys();

        ProfileDataMessage pdm = new ProfileDataMessage(senderKeys.getPublicKey(), "some.server", "someAlias", "someStatus", new ByteArrayInputStream(new byte[] {1, 2, 3}));

        Stream<TargetedPayloadMessage> payloadMessages = pdm.createPayloadMessages(senderKeys.getPrivateKey(), recipientKeys.getPublicKey());

        DataMessageFactory messageFactory = new DataMessageFactory();

        List<TargetedPayloadMessage> msgs = payloadMessages.collect(Collectors.toList());

        ProfileDataMessage deserialized = null;

        for (TargetedPayloadMessage msg : msgs) {
            byte[] msgData = Crypt.decrypt(recipientKeys.getPrivateKey(), msg.getEncryptedData());
            ByteBuffer byteBuffer = ByteBuffer.wrap(msgData);

            Optional<ClientMessage> result = messageFactory.readMessage(byteBuffer);
            if (result.isPresent()) {
                deserialized = (ProfileDataMessage) result.get();
            }
        }

        assertEquals("someAlias", deserialized.getAlias());
        assertEquals("someStatus", deserialized.getStatusText());
        InputStream profileImage = deserialized.getProfileImage();

        assertEquals(1, profileImage.read());
        assertEquals(2, profileImage.read());
        assertEquals(3, profileImage.read());
        assertEquals(-1, profileImage.read());

        assertEquals(senderKeys.getPublicKey(), Crypt.decodeKey(deserialized.getHeader().getSenderPublicKey()));
    }

    private void addToDecoder(TargetedPayloadMessage msg, MultiPartDataDecoder decoder) {
    }
}