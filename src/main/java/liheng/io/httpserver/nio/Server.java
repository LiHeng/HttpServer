package liheng.io.httpserver.nio;

import liheng.io.httpserver.context.Context;
import liheng.io.httpserver.http.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * http 服务器
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private Selector selector;

    public Server() {

    }

    public void start(){
        if (!init()){
            return;
        }
        while (true){
            if (!selector.isOpen()){
                break;
            }
            try {
                if (selector.select(500) == 0) {
                    continue;
                }
            } catch (IOException e) {
                LOGGER.error("selector错误", e);
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                try {
                    if (key.isAcceptable()){
                        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }else if (key.isWritable()){
                        LOGGER.info("Writeable");
                        SocketChannel client = (SocketChannel) key.channel();
                        Response response = (Response) key.attachment();
                        ByteBuffer byteBuffer = response.getByteBuffer(); // TODO 考虑修改为获取流
                        if (byteBuffer.hasRemaining()) {
                            client.write(byteBuffer);
                        }
                        if (!byteBuffer.hasRemaining()) {
                            key.cancel();
                            client.close();
                        }
                        LOGGER.info("write end");
                    }else if (key.isReadable()) {
                        LOGGER.info("Readable");
                        SocketChannel client = (SocketChannel) key.channel();
                        ThreadPool.execute(new Connector(client, selector));
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        LOGGER.info("After Read");
                    }
                    iterator.remove();
                } catch (IOException e) {
                    LOGGER.error("socket channel 出错了", e);
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException e2) {
                        LOGGER.error("socket channel 关闭出错", e2);
                    }
                }
            }
        }
    }

    private boolean init(){
        long start = System.currentTimeMillis();
        ServerSocketChannel serverSocketChannel = null;
        try {
            //ServiceRegistry.registerServices();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(Context.getIp(), Context.getPort()));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("服务器启动 http://{}:{}/ ,耗时{}ms", Context.getIp().getHostAddress(), Context.getPort(), System.currentTimeMillis() - start);
            return true;
        } catch (IOException e) {
            LOGGER.error("初始化错误", e);
            if (serverSocketChannel != null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e1) {
                    LOGGER.error("serverChannel关闭错误", e1);
                }
            }
            return false;
        }
    }

    public void destroy() {
        try {
            selector.close();
        } catch (IOException e) {
            LOGGER.error("关闭selector失败", e);
        }
    }

    public static void main(String[] args) {
        Context.init(new String[]{"start","8080"});
        new Server().start();
    }


}
