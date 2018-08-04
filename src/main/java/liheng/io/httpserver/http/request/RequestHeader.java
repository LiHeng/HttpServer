package liheng.io.httpserver.http.request;

import liheng.io.httpserver.http.HttpHeader;
import liheng.io.httpserver.http.HttpSchema;
import org.apache.commons.collections4.MultiValuedMap;

import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.Map;

/**
 * http 请求头，这里包含了request line里面的内容
 */
public class RequestHeader {

    private String URI;

    private String method;

    // 保存header里面参数的键值对
    private Map<String, String> head;

    // 保存查询参数的值
    private MultiValuedMap<String, String> queryMap;

    private String queryString;

    //客户端发送的cookie
    private Cookie[] cookies;

    //http协议版本
    private HttpSchema schema;

    public Cookie[] getCookies() {
        return cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHead() {
        return head;
    }

    public void setHead(Map<String, String> head) {
        this.head = head;
    }


    public String getURI() {
        return URI;
    }

    public String getMethod() {
        return method;
    }

    public boolean containKey(String key) {
        return queryMap.containsKey(key);
    }

    public MultiValuedMap<String, String> getQueryMap() {
        return queryMap;
    }

    public Collection<String> queryValue(String key) {
        return queryMap.get(key);
    }

    public String getContentType() {
        return head.get(HttpHeader.CONTENT_TYPE.getName());
    }

    public int getContentLength() {
        return Integer.valueOf(head.getOrDefault(HttpHeader.CONTENT_LENGTH.getName(), "0"));
    }


    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryMap(MultiValuedMap<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    public HttpSchema getSchema() {
        return schema;
    }

    public void setSchema(HttpSchema schema) {
        this.schema = schema;
    }
}
