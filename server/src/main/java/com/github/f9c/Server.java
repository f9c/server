package com.github.f9c;


import com.github.f9c.net.WebSocketServerInitializer;
import com.github.f9c.redis.RedisConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;

public class Server {
    private final int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup ;
    private Channel ch;

    private RedisConnection redisConnection;

    public Server(int port) {
        this.port = port;
    }

    public void waitForStart() throws InterruptedException {
        synchronized (this) {
            while (ch == null) {
                wait();
            }
        }
    }

    public void run()  {
        redisConnection = new RedisConnection();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            SslContext sslCtx;
            if (getDomain() == null || "".equals(getDomain())) {
                System.err.println("No domain specified. Running with self-signed certificate.");
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                String certificateBaseDir = "/etc/letsencrypt/live/" + getDomain() + "/";
                File keyCertChainFile = new File(certificateBaseDir + "fullchain.pem");
                File keyFile = new File(certificateBaseDir + "privkey.pem");

                checkFileExists(keyFile);
                checkFileExists(keyCertChainFile);

                sslCtx = SslContextBuilder.forServer(keyCertChainFile, keyFile).build();
            }
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer(sslCtx, redisConnection));

            ch = b.bind(port).sync().channel();

            synchronized (this) {
                notifyAll();
            }

            ch.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            redisConnection.close();
        }
    }

    private void checkFileExists(File file) {
        if (!file.exists()) {
            throw new IllegalStateException("Required file does not exist: " + file.getAbsolutePath());
        }
    }

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting server for domain: " + getDomain());
        new Server(8443).run();
    }

    private static String getDomain() {
        return System.getenv("F9C_DOMAIN");
    }

}
