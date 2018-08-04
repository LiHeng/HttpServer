package liheng.io.httpserver.mvc.annotation;

import java.lang.annotation.*;

/**
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiPart {
    String value();

    boolean require() default false;
}
