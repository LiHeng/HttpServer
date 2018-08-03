package liheng.io.httpserver.http.response;

import liheng.io.httpserver.util.PropertiesHelper;
import liheng.io.httpserver.util.TimeUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 响应
 *  Full-Response   = Status-Line               ; Section 6.1
 *                     ( General-Header         ; Section 4.3
 *                      | Response-Header        ; Section 6.2
 *                      | Entity-Header )        ; Section 7.1
 *                      CRLF
 *                      [ Entity-Body ]           ; Section 7.2
 */
public class Response implements HttpServletResponse{

    protected static final String HTTP_VERSION = "HTTP/1.1";
    protected static final String DEFAULT_CHARSET = "utf-8";
    protected Status status;
    protected Map<String, String> headers;
    protected byte[] responseBody;
    private ByteBuffer finalData = null;

    public Response(Status status) {
        this.status = status;
        headers = new HashMap<>();
        responseBody = new byte[0];
        headers.put("Date", TimeUtil.toRFC822(ZonedDateTime.now()));
        headers.put("Server", "HttpServer");
        headers.put("Connection", "Close"); // TODO keep-alive
    }

    public ByteBuffer getByteBuffer() {
        if (finalData == null) {
            headers.put("Content-Length", String.valueOf(responseBody.length));
            StringBuffer sb = new StringBuffer();
            //response line
            sb.append(HTTP_VERSION).append(" ").append(status.getCode()).append(" ").append(status.getMessage()).append("\r\n");

            //response head
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }

            //CRLF
            sb.append("\r\n");
            byte[] head = new byte[0];
            try {
                head = sb.toString().getBytes(DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException ignored) {
            }
            finalData = ByteBuffer.allocate(head.length + responseBody.length + 2);
            finalData.put(head);
            finalData.put(responseBody);
            finalData.put((byte) '\r');
            finalData.put((byte) '\n');
            finalData.flip();
        }
        return finalData;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public void setStatus(int i, String s) {

    }

    @Override
    public int getStatus() {
        return status.getCode();
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentLengthLong(long l) {

    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
