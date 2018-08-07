package liheng.io.httpserver.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;


public class Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);
    public static final int DEFAULT_PORT = 8877;
    private InetAddress ip;
    private int port;


    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
