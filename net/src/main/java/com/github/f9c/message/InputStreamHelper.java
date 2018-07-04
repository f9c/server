package com.github.f9c.message;

import java.io.DataInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InputStreamHelper {

    public static String readString(DataInputStream dataInputStream) {
        return new String(readBytes(dataInputStream), UTF_8);
    }

    private static byte[] readBytes(DataInputStream dataInputStream)  {
        try {
            int size = dataInputStream.readInt();
            byte[] data = new byte[size];
            dataInputStream.readFully(data);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
