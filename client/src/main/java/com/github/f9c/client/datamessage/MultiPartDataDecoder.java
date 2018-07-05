package com.github.f9c.client.datamessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Combines multi part data messages to one message again. Currently this is done in memory and only completed messages
 * are returned.
 * <p>
 * Later larger streams should be stored in temporary files and the MultiPartStream should be returned while partly
 * filled and block until all data is received.
 */
public class MultiPartDataDecoder {
    // TODO: implement cache with timeouts / size limit
    private Map<DataMessageHeader, MultiPartStream> partlyReceivedMessages = new HashMap<>();

    public InputStream add(DataMessageHeader messageHeader, MultiPartDataMessageHeader multiPartDataMessageHeader, byte[] data) {
        MultiPartStream stream = partlyReceivedMessages.get(messageHeader);
        if (stream == null) {
            stream = new MultiPartStream();
            partlyReceivedMessages.put(messageHeader, stream);
        }
        stream.add(multiPartDataMessageHeader, data);

        if (stream.isComplete()) {
            partlyReceivedMessages.remove(messageHeader);
            return stream;
        }
        return null;
    }


    private static class MultiPartStream extends InputStream {
        private List<StreamPart> parts = new ArrayList<>();
        private ByteArrayInputStream bais;

        @Override
        public int read() {
            if (bais == null) {
                if (parts.size() > 0) {
                    bais = nextPart();
                } else {
                    return -1;
                }
            }

            int result;
            while ((result = bais.read()) == -1 && parts.size() > 0) {
                bais = nextPart();
            }
            return result;
        }

        private ByteArrayInputStream nextPart() {
            return new ByteArrayInputStream(parts.remove(0).data);
        }

        public void add(MultiPartDataMessageHeader partHeader, byte[] data) {
            parts.add(new StreamPart(partHeader, data));
            Collections.sort(parts, new Comparator<StreamPart>() {
                @Override
                public int compare(StreamPart p1, StreamPart p2) {
                   return p1.getMultiPartDataMessageHeader().getPart() - p2.getMultiPartDataMessageHeader().getPart();
                }
            });
        }

        public boolean isComplete() {
            MultiPartDataMessageHeader lastHeader = parts.get(parts.size() - 1).multiPartDataMessageHeader;
            // This condition only works, if there are no duplicates
            return !lastHeader.isHasNext() && parts.size() - 1 == lastHeader.getPart();
        }
    }

    private static class StreamPart {
        private MultiPartDataMessageHeader multiPartDataMessageHeader;
        private byte[] data;

        public StreamPart(MultiPartDataMessageHeader multiPartDataMessageHeader, byte[] data) {
            this.multiPartDataMessageHeader = multiPartDataMessageHeader;
            this.data = data;
        }

        public MultiPartDataMessageHeader getMultiPartDataMessageHeader() {
            return multiPartDataMessageHeader;
        }
    }
}
