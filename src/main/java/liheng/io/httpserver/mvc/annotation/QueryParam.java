package liheng.io.httpserver.mvc.annotation;

import java.lang.annotation.*;

/**
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface QueryParam {
    String value();

    // 参数是否必须
    boolean require() default false;
}
