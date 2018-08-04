package liheng.io.httpserver;

import liheng.io.httpserver.util.BytesUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioServer {
    private String ip;

    private int port;

    private Selector selector;

    public NioServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void startListen() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverChannel.bind(new InetSocketAddress(ip, port));

        while (true) {
            //不能使用select方法，该方法会阻塞，如果在阻塞过程中channel状态就绪，会因此处阻塞而无法执行。
            //所以，如果调用阻塞方法，下面对channel状态的处理得另起一个常驻线程
            int result = selector.selectNow();
            if (result == 0) {
                continue;
            }

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                } else {
                    System.out.println("Unknow selector type");
                }

                //一定要调用remove方法将已经处理过的SelectionKey清除掉，否则会造成后面的请求无法接受
                it.remove();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        System.out.println("Receive connection");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocketChannel.accept();

        if (channel != null) {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }
        System.out.println("Connection end");
    }

    private void read(SelectionKey key) throws IOException {
        System.out.println("Start read");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(64);
        boolean hasContent = false;

        //这里的判断条件不能是不等于-1，因为channel一直都在，只是在数据被读完后里面为空，返回的长度是0.用-1判断会无限循环无法退出
        while (channel.read(buffer) > 0) {
            buffer.flip(); //切换为读模式
            CharBuffer cb = Charset.forName("UTF-8").decode(buffer);
            System.out.print(cb.toString());
            buffer.clear();
            hasContent = true;
        }

        if (hasContent) {
            //设置interestOps，用于写响应
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            channel.close();
        }
        System.out.println("Read end");
    }

    private void write(SelectionKey key) throws IOException {
        System.out.println("Start write");
        SocketChannel channel = (SocketChannel) key.channel();

        String resText = getResponseText();
        ByteBuffer buffer = ByteBuffer.wrap(resText.getBytes());

        //此处不可使用channel.write(buffer) != -1来判断，因为在两端都不关闭的情况下，会一直返回0，导致该循环无法退出
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.close();
        System.out.println("End write");
    }

    private String getResponseText() {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 200 OK\n");
        sb.append("Content-Type: text/html; charset=UTF-8\n");
        sb.append("\n");
        sb.append("<html>");
        sb.append("  <head>");
        sb.append("    <title>");
        sb.append("      NIO Http Server");
        sb.append("    </title>");
        sb.append("  </head>");
        sb.append("  <body>");
        sb.append("    <h1>Hello World!</h1>");
        sb.append("  </body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static void main(String[] args) {

    }

}
