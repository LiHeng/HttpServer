package liheng.io.test.controller;

import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.http.response.FileResponse;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.http.response.Status;
import liheng.io.httpserver.mvc.annotation.Controller;
import liheng.io.httpserver.mvc.annotation.WebPath;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Controller
public class IndexController {
    @WebPath("/")
    public Response indexController(Request request) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("pages/index.html");
        File index = Files.createTempFile("index", ".html").toFile();
        FileOutputStream os = new FileOutputStream(index);
        IOUtils.copy(is, os);
        is.close();
        os.close();
        return new FileResponse(Status.OK_200, index);
    }
}
