package liheng.io.httpserver.mvc.annotation;

import liheng.io.httpserver.http.HttpMethod;

import java.lang.annotation.*;

/**
 * 标注路径
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebPath {
    String[] value() default {"/"};

    HttpMethod[] method() default {};
}
