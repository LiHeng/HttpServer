package liheng.io.httpserver.nio;

import liheng.io.httpserver.http.exceptions.IllegalRequestException;
import liheng.io.httpserver.http.exceptions.ServerInternalException;
import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.http.request.RequestParser;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.http.response.ServerInternalResponse;
import liheng.io.httpserver.http.response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connector.class);
    private final SocketChannel channel;
    private final Selector selector;


    public Connector(SocketChannel client, Selector selector) {
        this.channel = client;
        this.selector = selector;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        Request request = null;
        Response response = null;
        try {
            request = RequestParser.parseRequest(channel);
            if (request == null) {
                //return;
            }
            response = new Response(Status.OK_200);
        } catch (ServerInternalException e) { // 这个IOException都是parseRequest里出来的
            LOGGER.error("服务器内部错误", e);
            response = new ServerInternalResponse();
        } catch (IllegalRequestException e) {
            LOGGER.error("请求有错误", e);
            response = new ServerInternalResponse();
        } catch (Exception e) {
            LOGGER.error("未知服务器内部错误", e);
            response = new ServerInternalResponse();
        }
        attachResponse(response);
        if (request != null) {
            LOGGER.info("{} \"{}\" {} {}ms", request.getMethod(), request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - start);
        }
    }

    private void attachResponse(Response response) {
        try {
            channel.register(selector, SelectionKey.OP_WRITE, response);
            selector.wakeup();
        } catch (ClosedChannelException e) {
            LOGGER.error("通道已关闭", e);
        }
    }
}
