package liheng.io.httpserver.nio;

import liheng.io.httpserver.context.Context;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.mvc.ControllerScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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

    private Context context;

    public Server() {

    }

    /**
     * 启动服务器
     * @param args 格式:start [address:port]
     * @param controllerPackagePaths 扫描controller所在的包
     * @throws IOException
     */
    public void run(String[] args,String... controllerPackagePaths) throws IOException {
        //初始化
        init(args,controllerPackagePaths);
        //启动
        start();
    }

    private void init(String[] args,String... controllerPackagePaths) throws UnknownHostException {
        long start = System.currentTimeMillis();

        //初始化server context，包括ip、port等信息
        initContext(args);
        //扫描controller
        initController(controllerPackagePaths);

        long end = System.currentTimeMillis();
        LOGGER.info("服务器启动 http:/{}:{}/ 耗时:{}ms", context.getIp().getHostAddress(),
                context.getPort(), end - start);
    }

    private void initContext(String[] args) throws UnknownHostException {
        //parse command line arguments
        if(args.length < 1 || !args[0].equals("start")){
            LOGGER.info("Usage: start [address:port]");
            System.exit(1);
        }

        InetAddress ip = null;
        int port = 0;

        if(args.length == 2 && args[1].matches(".+:\\d+")){
            String[] addressAndPort = args[1].split(":");
            ip = InetAddress.getByName(addressAndPort[0]);
            port = Integer.valueOf(addressAndPort[1]);
        }else{
            ip = InetAddress.getLocalHost();
            port = Context.DEFAULT_PORT;
        }

        Context context = new Context();
        context.setIp(ip);
        context.setPort(port);
        this.context = context;
    }

    private void initController(String... controllerPackagePaths){
        for (String packagePath:controllerPackagePaths){
            ControllerScan.scanPackage(packagePath);
        }
    }



    public void start(){
        if (!initServerSocket()){
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
                        //LOGGER.info("Writeable");
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
                        //LOGGER.info("write end");
                    }else if (key.isReadable()) {
                        //LOGGER.info("Readable");
                        SocketChannel client = (SocketChannel) key.channel();
                        ThreadPool.execute(new Connector(client, selector));
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        //LOGGER.info("After Read");
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

    private boolean initServerSocket(){
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(context.getIp(), context.getPort()));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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

    public static void main(String[] args) throws IOException {
        //Context.init(new String[]{"start","8080"});
        new Server().run(args,"liheng.io.httpserver.controller");
    }


}
