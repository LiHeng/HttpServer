package liheng.io.httpserver.http.request;

import liheng.io.httpserver.http.exceptions.IllegalRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *  解析客户端发送的请求
 */
public class RequestParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParser.class);

    public static Request parseRequest(SocketChannel channel) throws IllegalRequestException, IOException {

        return null;
    }
}
