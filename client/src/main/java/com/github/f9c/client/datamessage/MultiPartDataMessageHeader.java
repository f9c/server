package com.github.f9c.client.datamessage;

import java.nio.ByteBuffer;

public class MultiPartDataMessageHeader {
    public static final int HEADER_SIZE = 9;
    private int multiOpcode;
    private int part;
    private boolean hasNext;

    public MultiPartDataMessageHeader(ByteBuffer buffer) {
        multiOpcode = buffer.getInt();
        part = buffer.getInt();
        hasNext = buffer.get() == 1;
    }
    public MultiPartDataMessageHeader(int multiOpcode, int part, boolean hasNext) {
        this.part = part;
        this.hasNext = hasNext;
        this.multiOpcode = multiOpcode;
    }

    public void write(ByteBuffer buffer) {
        buffer.putInt(multiOpcode);
        buffer.putInt(part);
        buffer.put(hasNext ? (byte) 1 : 0);
    }

    public int getPart() {
        return part;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public int getMultiOpcode() {
        return multiOpcode;
    }

    public int size() {
        return HEADER_SIZE;
    }
}
