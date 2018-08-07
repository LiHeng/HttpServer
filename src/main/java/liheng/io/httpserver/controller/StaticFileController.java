package liheng.io.httpserver.controller;

import liheng.io.httpserver.http.HttpHeader;
import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.http.response.FileResponse;
import liheng.io.httpserver.http.response.NotFoundResponse;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.http.response.Status;
import liheng.io.httpserver.mvc.annotation.Controller;
import liheng.io.httpserver.mvc.annotation.WebPath;
import liheng.io.httpserver.util.PropertiesHelper;
import liheng.io.httpserver.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

@Controller
public class StaticFileController {
    private static final String prefix = "/s/";
    private static String staticPath;
    private static String root;

    static {
        staticPath = PropertiesHelper.getProperty("static_file_path","static");
        root = StaticFileController.class.getClassLoader().getResource("").getPath();
    }

    @WebPath(prefix + ".*")
    public Response staticFile(Request request){
        String filePath = root + staticPath + File.separator + request.getRequestURI().replaceAll(prefix,"");
        File file = new File(filePath);
        if(!file.exists() || !file.isFile() || !file.canRead()){
            return new NotFoundResponse();
        }

        /**
         * conditional get
         * https://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01#GET
         */
        String ifModifiedSinceStr = request.getHeader(HttpHeader.IF_MODIFIED_SINCE.getName().toLowerCase());
        if(StringUtils.isNotEmpty(ifModifiedSinceStr)){
            System.out.println(ifModifiedSinceStr);
            long ifModifiedSince = TimeUtil.parseRFC822(ifModifiedSinceStr).toInstant().toEpochMilli();
            if(file.lastModified() <= ifModifiedSince){
                return new Response(Status.NOT_MODIFIED_304);
            }
        }

        return new FileResponse(Status.OK_200,file);
    }

}
