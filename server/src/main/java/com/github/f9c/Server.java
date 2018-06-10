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
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
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

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server(8443).run();
    }

}
