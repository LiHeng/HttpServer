package liheng.io.httpserver.http.response;

import com.alibaba.fastjson.JSONObject;
import liheng.io.httpserver.http.exceptions.ServerInternalException;

import java.io.UnsupportedEncodingException;

/**
 * Json响应
 */
public class JsonResponse extends Response {
    public JsonResponse(Status status, Object obj) {
        super(status);
        if (obj == null) {
            throw new ServerInternalException("Json响应对象为空");
        }
        headers.put("Content-Type", "application/json; charset=" + DEFAULT_CHARSET);
        try {
            super.responseBody = JSONObject.toJSONString(obj).getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException ignored) {

        }

    }
}
