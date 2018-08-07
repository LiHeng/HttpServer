package liheng.io.httpserver.http.request;

import liheng.io.httpserver.http.HttpHeader;
import liheng.io.httpserver.http.HttpSchema;
import liheng.io.httpserver.http.exceptions.IllegalRequestException;
import liheng.io.httpserver.util.BytesUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  解析客户端发送的请求
 */
public class RequestParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParser.class);

    public static Request parseRequest(SocketChannel channel) throws IllegalRequestException, IOException {
        //assume size of (request line + headers) <= 1024kb
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        buffer.flip();

        int remaining = buffer.remaining();   // 可读字节数
        if (remaining==0){
            return null;
        }
        byte[] bytes = new byte[remaining];
        buffer.get(bytes);
        int position = BytesUtil.indexOf(bytes,"\r\n\r\n");
        if (position == -1) {
            throw new IllegalRequestException("请求不合法");
        }

        byte[] head = Arrays.copyOf(bytes, position);
        RequestHeader requestHeader = parseHeader(head);
        int contentLength = requestHeader.getContentLength();  // body字节的长度
        buffer.position(position+4);   // 跨过 '\r\n\r\n'

        ByteBuffer bodyBuffer = ByteBuffer.allocate(contentLength);
        bodyBuffer.put(buffer);

        while (bodyBuffer.hasRemaining()) {            // request body 内容还未发送完毕
            channel.read(bodyBuffer);                  //IOException
        }

        byte[] body = bodyBuffer.array();
        RequestBody requestBody = parseBody(body, requestHeader);
        return new Request(requestHeader, requestBody);
    }

    /**
     * 解析请求头
     */
    private static RequestHeader parseHeader(byte[] head) throws IOException {
        RequestHeader header = new RequestHeader();
        try (BufferedReader reader = new BufferedReader(new StringReader(new String(head, "UTF-8")))) {
            Map<String, String> headMap = new HashMap<>();
            String line = reader.readLine();
            String[] requestLine = line.split("\\s");
            String path = URLDecoder.decode(requestLine[1], "utf-8");
            String method = requestLine[0];
            HttpSchema scheme = HttpSchema.parseScheme(requestLine[2]);
            while ((line = reader.readLine()) != null) {            //解析header里面的key-value
                String[] keyValue = line.split(":", 2);
                headMap.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
            }
            int index = path.indexOf('?');
            MultiValuedMap<String, String> queryMap = new ArrayListValuedHashMap<>();
            String queryString = StringUtils.EMPTY;
            if (index != -1) {
                queryString = path.substring(index + 1);
                RequestParser.parseParameters(queryString, queryMap);
                path = path.substring(0, index);
            }
            header.setURI(path); // 大小写敏感
            header.setMethod(method); // 大小写敏感
            header.setHead(headMap);
            header.setQueryString(queryString);
            header.setQueryMap(queryMap);
            header.setCookies(parseCookie(headMap));
            header.setSchema(scheme);
            return header;
        }
    }

    /**
     * 解析请求参数
     */
    private static void parseParameters(String s, MultiValuedMap<String, String> requestParameters) {
        String[] paras = s.split("&");
        for (String para : paras) {
            String[] split = para.split("=");
            if (split.length != 2) {
                continue;
            }
            requestParameters.put(split[0], split[1]);
        }
    }

    /**
     * 解析cookie信息
     */
    private static Cookie[] parseCookie(Map<String, String> headMap) {
        if (MapUtils.isEmpty(headMap)) {
            return new Cookie[0];
        }
        String cookies = headMap.get(HttpHeader.COOKIE.getName());
        if (StringUtils.isBlank(cookies)) {
            return new Cookie[0];
        }
        String[] split = cookies.split(";");
        Cookie[] cookieArray = new Cookie[split.length];
        for (int i = 0; i < split.length; i++) {
            String[] array;
            try {
                array = split[i].split("=", 2);
                cookieArray[i] = new Cookie(array[0], array[1]);
            } catch (RuntimeException e) {
                LOGGER.error("非法cookie", e);
                cookieArray[i] = new Cookie(StringUtils.EMPTY, StringUtils.EMPTY); // TODO 这里不能为空，如果出现了保留cookie怎么办
            }
        }
        return cookieArray;
    }

    /**
     * 解析请求体
     */
    public static RequestBody parseBody(byte[] body, RequestHeader header) {
        if (body.length == 0) {
            return new RequestBody();
        }

        String contentType = header.getContentType();
        Map<String, MimeData> mimeMap = Collections.emptyMap(); // 通过 multipart/form-data 方式提交的数据
        MultiValuedMap<String, String> formMap = new ArrayListValuedHashMap<>();
        String jsonString = null;
        if (contentType.contains("application/x-www-form-urlencoded")) {    //表单提交的参数
            try {
                String bodyMsg = new String(body, "utf-8");
                RequestParser.parseParameters(bodyMsg, formMap);
            } catch (UnsupportedEncodingException ignored) {
            }
        } else if (contentType.contains("multipart/form-data")) {      //表单提交的文件
            int boundaryValueIndex = contentType.indexOf("boundary=");
            String bouStr = contentType.substring(boundaryValueIndex + 9);  // 9是 `boundary=` 长度
            mimeMap = parseFormData(body, bouStr);

            // 将mimeMap中的content-type为null类型的值放入formMap
            try {
                for (Map.Entry<String,MimeData> entry:mimeMap.entrySet()){
                    if (entry.getValue().getContentType()==null){
                        formMap.put(entry.getKey(), new String(entry.getValue().getData(),"utf-8"));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else if (contentType.contains("application/json")){      // json
            try {
                jsonString = new String(body, "utf-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        RequestBody requestBody = new RequestBody();
        requestBody.setFormMap(formMap);
        requestBody.setMimeMap(mimeMap);
        requestBody.setRequestBody(jsonString);
        return requestBody;
    }

    /**
     * https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
     * standard form format
     */
//    <FORM action="http://server.com/cgi/handle"
//          enctype="multipart/form-data"
//          method="post">
//    <P>
//    What is your name? <INPUT type="text" name="submit-name"><BR>
//    What files are you sending? <INPUT type="file" name="files"><BR>
//    <INPUT type="submit" value="Send"> <INPUT type="reset">
//    </FORM>

//    Content-Type: multipart/form-data; boundary=AaB03x
//
//    --AaB03x
//    Content-Disposition: form-data; name="submit-name"
//
//    Larry
//    --AaB03x
//    Content-Disposition: form-data; name="files"; filename="file1.txt"
//    Content-Type: text/plain
//
//    ... contents of file1.txt ...
//    --AaB03x--

    /**
     * @param body   body
     * @param bouStr boundary 字符串
     * @return name和mime数据的map
     */
    private static Map<String, MimeData> parseFormData(byte[] body, String bouStr) {
        Map<String, MimeData> mimeData = new HashMap<>();
        bouStr = "--" + bouStr;

        int bouLength = bouStr.length();
        int lastIndex = BytesUtil.lastIndexOf(body, bouStr);
        int startIndex;
        int endIndex = BytesUtil.indexOf(body, bouStr);
        if (lastIndex==endIndex){   //empty form data
            return mimeData;
        }
        byte[] curBody;

        do{
            startIndex = endIndex + bouLength;
            endIndex = BytesUtil.indexOf(body,bouStr,startIndex);

            curBody = Arrays.copyOfRange(body, startIndex + 2, endIndex); //去掉\r\n
            int lineEndIndex = BytesUtil.indexOf(curBody, "\r\n");
            byte[] lineOne = Arrays.copyOfRange(curBody, 0, lineEndIndex);
            int leftQuoIndex = BytesUtil.indexOf(lineOne, "\"");
            int rightQuoIndex = BytesUtil.indexOf(lineOne, "\"", leftQuoIndex + 1);

            String name;
            String fileName = null;
            String mimeType = null;
            byte[] data;

            //name of the form data
            name = new String(Arrays.copyOfRange(lineOne, leftQuoIndex + 1, rightQuoIndex));
            leftQuoIndex = BytesUtil.indexOf(lineOne, "\"", rightQuoIndex + 1);
            int curIndex;
            if (leftQuoIndex != -1) {
                rightQuoIndex = BytesUtil.indexOf(lineOne, "\"", leftQuoIndex + 1);
                fileName = new String(Arrays.copyOfRange(lineOne, leftQuoIndex + 1, rightQuoIndex));
                int headEndIndex = BytesUtil.indexOf(curBody, "\r\n\r\n", 13);
                mimeType = headEndIndex == lineEndIndex ? "text/plain" :new String(Arrays.copyOfRange(curBody, lineEndIndex + 16, headEndIndex));
                curIndex = headEndIndex + 4;
            }else {
                curIndex = lineEndIndex + 4;
            }

            data = Arrays.copyOfRange(curBody, curIndex, curBody.length-2); //去掉后面的\r\n
            mimeData.put(name, new MimeData(mimeType, data, fileName));
        }while (endIndex!=lastIndex);
        return mimeData;
    }
}
