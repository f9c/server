package com.github.f9c.message;

import java.nio.ByteBuffer;

public class MessageHelper {

    public static byte[] toBinary(Message message) {
        byte[] data = message.data();

        ByteBuffer bb = ByteBuffer.allocate(data.length + 4);
        bb.putInt(message.getType().getOpcode());
        bb.put(data);
        bb.rewind();

        byte[] arr = new byte[bb.remaining()];
        bb.get(arr);
        return arr;
    }
}
