package com.github.f9c.client.datamessage;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.message.Message;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DataMessageFactoryTest {

    @Test
    public void shouldDecodeEncodedClass() throws Exception {
        ClientKeys senderKeys = new ClientKeys();
        ClientKeys recipientKeys = new ClientKeys();

        TextMessage testMessage = new TextMessage("Testmessage", senderKeys.getPublicKey(), "server");

        Iterator<TargetedPayloadMessage> payloadMessages = testMessage.createPayloadMessages(senderKeys.getPrivateKey(), recipientKeys.getPublicKey());

        assertTrue(payloadMessages.hasNext());

        TargetedPayloadMessage targetedPayloadMessage = payloadMessages.next();

        assertFalse(payloadMessages.hasNext());

        byte[] msgData = Crypt.decrypt(recipientKeys.getPrivateKey(), targetedPayloadMessage.getEncryptedData());
        ByteBuffer byteBuffer = ByteBuffer.wrap(msgData);

        TextMessage testMessage2 = (TextMessage) new DataMessageFactory().readMessage(byteBuffer);

        assertEquals(testMessage.getMsgId(), testMessage2.getMsgId());
        assertEquals(testMessage.getHeader().getSenderServer(), testMessage2.getHeader().getSenderServer());
        assertEquals(testMessage.getTimestamp(), testMessage2.getTimestamp());

        assertEquals("Testmessage", testMessage2.getMsg());
        assertEquals(senderKeys.getPublicKey(), testMessage2.getSenderPublicKey());
    }

}