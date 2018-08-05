package liheng.io.httpserver.http.response;


import liheng.io.httpserver.mvc.ControllerScan;
import liheng.io.httpserver.util.PropertiesHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 404未找到响应
 */
public class NotFoundResponse extends FileResponse {

    private static File PATH_404HTML;

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerScan.class);

    static {
        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream("pages/404.html");
            PATH_404HTML = Files.createTempFile("index", ".html").toFile();
            FileOutputStream os = new FileOutputStream(PATH_404HTML);
            IOUtils.copy(inputStream, os);
            inputStream.close();
            os.close();
        } catch (IOException e) {
            LOGGER.warn("加载404页面出错");
        }
    }

    public NotFoundResponse() {
        super(Status.NOT_FOUND_404, PATH_404HTML);
    }
}
