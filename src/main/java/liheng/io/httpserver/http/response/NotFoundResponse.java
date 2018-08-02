package liheng.io.httpserver.http.response;


import liheng.io.httpserver.util.PropertiesHelper;

import java.io.File;

/**
 * 404未找到响应
 */
public class NotFoundResponse extends FileResponse {

    private static final File PATH_404HTML;

    static {
        PATH_404HTML = new File(PropertiesHelper.getProperty("404html_path", PropertiesHelper.getResourcePath("template/404.html")));
    }

    public NotFoundResponse() {
        super(Status.NOT_FOUND_404, PATH_404HTML);
    }
}
