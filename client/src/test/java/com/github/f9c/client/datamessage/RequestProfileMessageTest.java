package com.github.f9c.client.datamessage;

import com.github.f9c.client.ClientKeys;
import com.github.f9c.client.datamessage.multipart.RequestProfileMessage;
import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class RequestProfileMessageTest {

    @Test
    public void shouldBeSerializable() throws Exception {
//  TODO      ClientKeys ck = new ClientKeys();
//        RequestProfileMessage pdm = new RequestProfileMessage("someAlias", "someStatus", new byte[] {1, 2, 3}, ck.getPublicKey(), "someServer");
//
//        TargetedPayloadMessage targetedPayloadMessage = DataMessageFactory.createTargetedPayloadMessage(ck.getPrivateKey(), ck.getPublicKey(), pdm);
//
//        byte[] msgData = Crypt.decrypt(ck.getPrivateKey(), targetedPayloadMessage.getEncryptedData());
//        ByteBuffer byteBuffer = ByteBuffer.wrap(msgData);
//
//        RequestProfileMessage deserialized = (RequestProfileMessage) DataMessageFactory.readMessage(byteBuffer);
//
//        assertEquals("someAlias", deserialized.getAlias());
//        assertEquals("someServer", deserialized.getServer());
//        assertEquals("someStatus", deserialized.getStatusText());
//        assertEquals(3, deserialized.getProfileImage().length);
//        assertEquals(1, deserialized.getProfileImage()[0]);
//        assertEquals(2, deserialized.getProfileImage()[1]);
//        assertEquals(3, deserialized.getProfileImage()[2]);
//        assertEquals(ck.getPublicKey(), deserialized.getSenderPublicKey());
    }
}