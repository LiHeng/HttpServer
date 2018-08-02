package liheng.io.httpserver.http.response;


import liheng.io.httpserver.http.MimeTypes;
import liheng.io.httpserver.http.exceptions.ServerInternalException;
import liheng.io.httpserver.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 文件响应
 */
public class FileResponse extends Response {

    public FileResponse(Status status, File file) {
        super(status);
        if (file == null) {
            throw new ServerInternalException("Response File 对象为空");
        }
        if (!file.isFile() || !file.canRead()) {
            this.status = Status.NOT_FOUND_404;
            return;
        }
        long l = file.lastModified();
        headers.put("Last-Modified", TimeUtil.toRFC822(ZonedDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())));
        String path = file.getAbsolutePath();
        try {
            String contentType = MimeTypes.getContentType(path);
            responseBody = Files.readAllBytes(FileSystems.getDefault().getPath(path)); // TODO 静态文件不应该全部读到内存中下载 同时需要支持gzip以及chunk
            if (contentType.startsWith("text")) {
                contentType += "; charset=" + DEFAULT_CHARSET;
            }
            headers.put("Content-Type", contentType);
        } catch (IOException e) {
            this.status = Status.NOT_FOUND_404;
        }
    }
}
