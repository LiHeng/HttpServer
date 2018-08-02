package liheng.io.httpserver.http.exceptions;

/**
 * 业务异常
 */
public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(String msg,Throwable e) {
        super(msg,e);
    }

    public BaseRuntimeException(String msg) {
        super(msg);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
