package liheng.io.httpserver.mvc;

import liheng.io.httpserver.http.HttpMethod;
import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.mvc.annotation.Controller;
import liheng.io.httpserver.mvc.annotation.WebPath;
import liheng.io.httpserver.util.FileUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerScan {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerScan.class);
    private static Map<String, ControllerMethodInfo> controllerMethodMap = new ConcurrentHashMap<>();


    public static void scanPackage(String pkgName) {
        Reflections reflections = new Reflections(pkgName);

        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> controller : controllers) {
            WebPath webPathOnClass = controller.getAnnotation(WebPath.class);
            String[] parentPath = new String[]{"/"};
            HttpMethod[] parentHttpMethod = null;
            if (webPathOnClass != null) {
                if (webPathOnClass.value().length == 0) {
                    LOGGER.warn("{}上的@WebPath value为空，已默认为/", controller.getName());
                } else {
                    parentPath = webPathOnClass.value();
                }
                parentHttpMethod = webPathOnClass.method();
            }


            Method[] methods = controller.getMethods();
            for (Method method : methods) {
                WebPath webPathOnMethod = method.getAnnotation(WebPath.class);
                if (webPathOnMethod == null) {
                    continue;
                }
                String childPath[] = webPathOnMethod.value();
                HttpMethod[] childHttpMethod = webPathOnMethod.method();
                if (ArrayUtils.isEmpty(childHttpMethod)) {
                    if (ArrayUtils.isEmpty(parentHttpMethod)) {
                        childHttpMethod = new HttpMethod[]{HttpMethod.GET};
                    } else {
                        childHttpMethod = parentHttpMethod;
                    }
                }

                ControllerMethodInfo controllerMethod = null;
                try {
                    controllerMethod = new ControllerMethodInfo(controller.newInstance(), method, childHttpMethod);   //可以考虑将controller设置为单例
                } catch (InstantiationException e) {  //controller.newInstance() 出错
                    e.printStackTrace();
                } catch (IllegalAccessException e) {  //controller.newInstance() 出错
                    e.printStackTrace();
                }

                for (String parent : parentPath) {
                    for (String child : childPath) {
                        String path = FileUtil.combinePath(parent, child);
                        if (controllerMethodMap.containsKey(path)) {
                            LOGGER.warn("RequestMapping 出现重复,映射路径为'{}', 将被覆盖!", path);
                        }
                        controllerMethodMap.put(path, controllerMethod);
                        LOGGER.info("成功注册请求方法映射,映射[{}]到[{}.{}]", path, controller.getName(), method.getName());
                    }
                }
            }

        }
    }

    /**
     * 根据request里的URI信息搜索对应的controller方法
     */
    public static ControllerMethodInfo findControllerMethod(Request request) {
        String requestURI = request.getRequestURI();
        for (Map.Entry<String, ControllerMethodInfo> entry : controllerMethodMap.entrySet()) {
            if (requestURI.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
