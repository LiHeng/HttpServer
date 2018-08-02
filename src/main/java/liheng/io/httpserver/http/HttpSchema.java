package liheng.io.httpserver.http;

import org.apache.commons.lang3.StringUtils;

/**
 * http 请求方式, 现在包括http和https
 */
public enum HttpSchema{

    HTTP("http"),
    HTTPS("https");

    private final String content;

    HttpSchema(String s) {
        this.content = s;
    }

    public static HttpSchema parseScheme(String s) {
        String[] split = s.split("/");
        for (HttpSchema httpScheme : HttpSchema.values()) {
            if (StringUtils.equals(split[0].toLowerCase(), httpScheme.toString())) {
                return httpScheme;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return content;
    }
}
