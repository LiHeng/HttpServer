package liheng.io.httpserver.http.response;

/**
 *  状态码
 *  1xx Informational responses
 *	2xx Success
 *	3xx Redirection
 *	4xx Client errors
 *	5xx Server errors
 */
public enum Status {
    OK_200(200, "OK"),
    NOT_MODIFIED_304(304, "NOT MODIFIED"),
    BAD_REQUEST_400(400, "BAD REQUEST"),
    NOT_FOUND_404(404, "NOT FOUND"),
    METHOD_NOT_ALLOWED_405(405, "METHOD NOT ALLOWED"),
    INTERNAL_SERVER_ERROR_500(500, "INTERNAL SERVER ERROR");

    private int code;
    private String message;

    Status(int code,String msg){
        this.code = code;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
