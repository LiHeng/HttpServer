package liheng.io.httpserver.http;

/**
 *  http 请求方法
 */
public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    HEAD("HEAD"),       // TODO
    PUT("PUT"),         // TODO
    OPTIONS("OPTIONS"), // TODO
    DELETE("DELETE");   // TODO


    /**
     * http请求方法名称
     */
    private String name;

    HttpMethod(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
