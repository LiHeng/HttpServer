package liheng.io.httpserver.http.request;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;


/**
 * http request
 */
public class Request implements HttpServletRequest {

    /**
     * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01#Request
     *
     * Full-Request   = Request-Line              ; Section 5.1
     *                  ( General-Header          ; Section 4.3
     *                  | Request-Header          ; Section 5.2
     *                  | Entity-Header )         ; Section 7.1
     *                  CRLF
     *                  [ Entity-Body ]           ; Section 7.2
     */

    private final RequestHeader header;

    private final RequestBody body;

    private final AtomicReference<ConcurrentMap<String, Object>> attributes = new AtomicReference<>();

    private String characterEncoding;

    public Request(RequestHeader header, RequestBody body) {
        this.header = header;
        this.body = body;
    }

    private ConcurrentMap<String, Object> getMap() {
        while (true) {
            ConcurrentMap<String, Object> map = attributes.get();
            if (map != null)
                return map;
            map = new ConcurrentHashMap<>();
            if (attributes.compareAndSet(null, map))
                return map;
        }
    }

    public Collection<String> queryValue(String key) {
        return header.queryValue(key);
    }

    public Collection<String> formValue(String key) {
        return body.getFormMap().get(key);
    }

    public MimeData mimeValue(String key) {
        return body.mimeValue(key);
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return header.getMethod();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return header.getURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return getMap().get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(getMap().keySet());
    }

    @Override
    public String getCharacterEncoding() {
        if (StringUtils.isNotEmpty(characterEncoding)) {
            return characterEncoding;
        }
        String contentType = header.getContentType();
        int i = StringUtils.indexOf(contentType, "charset=");
        if (i < 0) {
            return null;
        }
        characterEncoding = StringUtils.substring(contentType, i + 8, contentType.length()).trim();
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        characterEncoding = encoding;
        if (!StringUtils.equalsIgnoreCase("UTF-8", encoding)) {
            Charset.forName(encoding);
        }
    }

    @Override
    public int getContentLength() {
        return header.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public String getContentType() {
        return header.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        String method = getMethod();
        Collection<String> collection = method.equalsIgnoreCase("GET") ? header.getQueryMap().get(s) : body.getFormMap().get(s);
        return CollectionUtils.isEmpty(collection) ? null : collection.iterator().next();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        TreeSet<String> names = new TreeSet<>();
        names.addAll(header.getQueryMap().keySet());
        names.addAll(body.getFormMap().keySet());
        return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String s) {
        Collection<String> values = CollectionUtils.union(header.queryValue(s), body.formValue(s));
        return CollectionUtils.isEmpty(values) ? new String[0] : (String[]) values.toArray();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return header.getSchema().name();
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {
        getMap().put(s,o);
    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
