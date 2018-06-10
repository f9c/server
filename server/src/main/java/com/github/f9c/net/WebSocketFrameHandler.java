package com.github.f9c.net;

import com.github.f9c.message.*;
import com.github.f9c.redis.RedisConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.NotSslRecordException;

import java.security.*;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private ChallengeMessage lastChallenge;
    private ChallengeRequestMessage lastChallengeRequest;
    private RedisConnection redisConnection;
    private PublicKey authenticatedKey;
    private ChannelHandlerContext ctx;

    public WebSocketFrameHandler(RedisConnection redisConnection) {
        this.redisConnection = redisConnection;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        if (authenticatedKey != null) {
            redisConnection.unsubscribe(authenticatedKey);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof BinaryWebSocketFrame) {
            handleMessage(MessageFactory.readMessage((BinaryWebSocketFrame) frame));
        } else {
            throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass().getName());
        }
    }

    private void handleMessage(Message message) {
        int opcode = message.getType().getOpcode();
        switch (opcode) {
            case MessageOpcodes.CHALLENGE_REQUEST:
                handleChallengeRequest((ChallengeRequestMessage) message);
                return;
            case MessageOpcodes.CHALLENGE_RESPONSE:
                handleChallengeResponse((ChallengeResponseMessage) message);
                return;
            case MessageOpcodes.TARGETED_DATA:
                handleData((TargetedPayloadMessage) message);
                return;
            default: throw new IllegalArgumentException("Unsupported opcode: " + opcode);
        }
    }

    private void handleData(TargetedPayloadMessage message) {
        redisConnection.putMessage(message.getRecipient(), message.getEncryptedData());
    }

    private void handleChallengeResponse(ChallengeResponseMessage message) {
        try {
            byte[] signatureBytes = message.getSignature();
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(lastChallengeRequest.getKey());
            signature.update(lastChallenge.data());

            if (signature.verify(signatureBytes)) {
                authenticatedKey = lastChallengeRequest.getKey();
                lastChallengeRequest = null;
                redisConnection.subscribeForUpdates(authenticatedKey, this);
                redisConnection.retrieveMessages(authenticatedKey, this);
                sendMessage(new ConnectionSuccessfulMessage());
            } else {
                sendMessage(new VerificationFailedMessage());
            }
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
           throw new RuntimeException(e);
        }
    }

    private void handleChallengeRequest(ChallengeRequestMessage msg) {
        if (msg.getProtocolVersion() != ChallengeRequestMessage.PROTOCOL_VERSION_1) {
            sendMessage(new UnsupportedProtocolVersionMessage());
            ctx.close();
            return;
        }

        ChallengeMessage challenge = new ChallengeMessage(createChallenge());
        lastChallenge = challenge;
        lastChallengeRequest = msg;
        sendMessage(challenge);
    }

    private byte[] createChallenge() {
        try {
            byte[] challengeBytes = new byte[64];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(challengeBytes);
            return challengeBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Message message) {
        byte[] data = message.data();
        ByteBuf buf = Unpooled.buffer(data.length + 4);
        buf.writeInt(message.getType().getOpcode());
        buf.writeBytes(data);
        ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buf));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof DecoderException && cause.getCause() instanceof NotSslRecordException) {
            // Someone is trying to open a non ssl connection. Ignore and close connection.
            // This happens all the time with people scanning the server
            ctx.close();
            return;
        }
        if (cause instanceof Error) {
            // Do not wrap errors.
            throw (Error) cause;
        }
        throw new RuntimeException(cause);
    }
}
