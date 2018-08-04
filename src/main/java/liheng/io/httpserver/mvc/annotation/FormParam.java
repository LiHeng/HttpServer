package liheng.io.httpserver.mvc.annotation;

import java.lang.annotation.*;

/**
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormParam {
    String value();

    boolean require() default false;
}
