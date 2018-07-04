package com.github.f9c.client.datamessage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Combines multi part data messages to one message again. Currently this is done in memory and only completed messages
 * are returned.
 *
 * Later larger streams should be stored in temporary files and the MultiPartStream should be returned while partly
 * filled and block until all data is received.
 */
public class MultiPartDataDecoder {
    private Cache<DataMessageHeader, MultiPartStream> partlyReceivedMessages =
            CacheBuilder.newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .build();

    public Optional<InputStream> add(DataMessageHeader messageHeader, MultiPartDataMessageHeader multiPartDataMessageHeader, byte[] data) {
        try {
            MultiPartStream stream = partlyReceivedMessages.get(messageHeader, MultiPartStream::new);
            stream.add(multiPartDataMessageHeader, data);

            if (stream.isComplete()) {
                partlyReceivedMessages.invalidate(messageHeader);
                return Optional.of(stream);
            }
            return Optional.empty();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
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
            parts.sort(Comparator.comparingInt(p -> p.multiPartDataMessageHeader.getPart()));
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
    }
}
