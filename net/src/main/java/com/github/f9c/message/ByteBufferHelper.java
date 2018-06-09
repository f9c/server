package com.github.f9c.message;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ByteBufferHelper {
    public static byte[] allRemainingData(ByteBuffer content) {
        byte[] data = new byte[content.remaining()];
        content.get(data, 0, data.length);
        return data;
    }

    public static void put(byte[] data, ByteBuffer buffer) {
        buffer.putInt(data.length);
        buffer.put(data);
    }

    public static int encodedSize(byte[] bytes) {
        return bytes.length + 4;
    }

    public static int encodedSize(String data) {
        return data.getBytes(UTF_8).length + 4;
    }

    public static byte[] get(int maxSize, ByteBuffer buffer) {
        int length = buffer.getInt();

        if (length > maxSize) {
            throw new IllegalArgumentException("Data is too long: " + length + " > " + maxSize);
        }

        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    public static String getString(int maxSize, ByteBuffer buffer) {
        return new String(get(maxSize, buffer), UTF_8);
    }

    public static void putString(String data, ByteBuffer buffer) {
        ByteBufferHelper.put(data.getBytes(UTF_8), buffer);
    }
}
