package liheng.io.httpserver.http.request;

/**
 *
 */
public class MimeData {

    private String contentType;

    private byte[] data;

    private String filename;

    public MimeData(String contentType, byte[] data, String filename) {
        this.contentType = contentType;
        this.data = data;
        this.filename = filename;
    }

    public byte[] getData() {
        return data;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

}
