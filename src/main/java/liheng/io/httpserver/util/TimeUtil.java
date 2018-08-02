package liheng.io.httpserver.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class TimeUtil {
    private static final DateTimeFormatter RFC822 = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    public static ZonedDateTime parseRFC822(String timeStr) {
        return ZonedDateTime.parse(timeStr, RFC822);
    }

    public static String toRFC822(ZonedDateTime dateTime) {
        return dateTime.format(RFC822);
    }
}
