package liheng.io.httpserver.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * mime types
 */
public class MimeTypes {

    private static final HashMap<String,String> mimeMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypes.class);

    static{
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mime.properties"));
            properties.entrySet().forEach(entry->{
                Object key = entry.getKey();
                Object value = entry.getValue();
                mimeMap.put((String)key,(String)value);
            });
        } catch (IOException e) {
            LOGGER.error("Failed to load mime.properties");
        }
    }

    //返回response时，要标明response的Content-Type
    public static String getContentType(String path) {
        int i = StringUtils.lastIndexOf(path, '.');
        if (i == -1 || i == path.length() - 1) {
            return "text/plain";
        }
        String suffix = path.substring(i + 1, path.length());
        return mimeMap.getOrDefault(suffix, "text/plain");
    }

}
