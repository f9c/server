package com.github.f9c.client.datamessage;

import com.github.f9c.message.TargetedPayloadMessage;
import com.github.f9c.message.encryption.Crypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.f9c.client.datamessage.DataMessageOpcodes.MULTI_PART;


public class MultiPartDataMessage extends AbstractDataMessage {
    private static final int MSG_SIZE = 50000;
    private static final int BLOCK_SIZE = 512;

    private InputStream additionalData;
    private byte[] baseData;
    private int multiOpcode;

    public MultiPartDataMessage(PublicKey sender, String senderServer, int multiOpcode, byte[] baseData, InputStream additionalData) {
        super(sender, senderServer);
        this.baseData = baseData;
        this.additionalData = additionalData;
        this.multiOpcode = multiOpcode;
    }

    public MultiPartDataMessage(DataMessageHeader header, int multiOpcode, byte[] baseData, InputStream additionalData) {
        super(header);
        this.baseData = baseData;
        this.additionalData = additionalData;
        this.multiOpcode = multiOpcode;
    }

    protected InputStream additionalData() {
        return additionalData;
    }

    protected byte[] baseData() {
        return baseData;
    }

    @Override
    protected int getOpcode() {
        return MULTI_PART;
    }

    public Stream<TargetedPayloadMessage> createPayloadMessages(PrivateKey sender, PublicKey recipient) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new MultiPartStream(getHeader(), multiOpcode, sender, recipient, baseData(), additionalData()), Spliterator.ORDERED),
                false);
    }

    private static class MultiPartStream implements Iterator<TargetedPayloadMessage> {
        private static final int HAS_NEXT_INDEX = 4;

        private final InputStream additionalData;
        private final DataMessageHeader header;
        private ByteBuffer buffer;

        private int part;
        private PrivateKey sender;
        private PublicKey recipient;
        private int multiOpcode;

        private TargetedPayloadMessage next;
        private boolean moreDateAvailable;

        public MultiPartStream(DataMessageHeader header, int multiOpcode, PrivateKey sender, PublicKey recipient, byte[] baseData, InputStream additionalData) {
            this.sender = sender;
            this.recipient = recipient;
            this.additionalData = additionalData;
            this.header = header;
            this.multiOpcode = multiOpcode;

            part = 0;
            buffer = ByteBuffer.allocate(MSG_SIZE);

            this.header.write(buffer);
            new MultiPartDataMessageHeader(multiOpcode, part, moreDateAvailable).write(buffer);

            buffer.put(baseData);

            moreDateAvailable = fillBuffer();
            next = createMessageFromBuffer();
        }

        private boolean fillBuffer() {
            byte[] copyBuffer = new byte[BLOCK_SIZE];
            try {
                while (buffer.remaining() >= BLOCK_SIZE) {
                    int byteCount = additionalData.read(copyBuffer);
                    if (byteCount > -1) {
                        buffer.put(copyBuffer, 0, byteCount);
                    } else {
                        return false;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public TargetedPayloadMessage next() {
            TargetedPayloadMessage result = next;
            next = readNextPart();
            return result;
        }

        private TargetedPayloadMessage readNextPart() {
            if (!moreDateAvailable) {
                return null;
            }

            part++;
            buffer.reset();
            header.write(buffer);
            new MultiPartDataMessageHeader(multiOpcode, part, moreDateAvailable).write(buffer);
            moreDateAvailable = fillBuffer();

            return createMessageFromBuffer();
        }

        private TargetedPayloadMessage createMessageFromBuffer() {
            int msgSize = buffer.position();

            header.updateDataSize(buffer, msgSize - header.size());

            // overwrite MultiPartDataMessageHeader since moreDateAvailable is only available now
            buffer.position(header.size());
            new MultiPartDataMessageHeader(multiOpcode, part, moreDateAvailable).write(buffer);

            buffer.rewind();
            byte[] msgData = new byte[msgSize];
            buffer.get(msgData);

            byte[] signatureData = Crypt.sign(sender, msgData);

            byte[] resultData = new byte[msgData.length + signatureData.length];
            ByteBuffer buf = ByteBuffer.wrap(resultData);
            buf.put(msgData);
            buf.put(signatureData);

            return new TargetedPayloadMessage(recipient, resultData);
        }
    }
}
