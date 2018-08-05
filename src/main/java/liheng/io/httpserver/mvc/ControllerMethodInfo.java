package liheng.io.httpserver.mvc;

import com.alibaba.fastjson.JSON;
import liheng.io.httpserver.http.HttpMethod;
import liheng.io.httpserver.http.exceptions.ServerInternalException;
import liheng.io.httpserver.http.request.MimeData;
import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.http.response.Status;
import liheng.io.httpserver.mvc.annotation.FormParam;
import liheng.io.httpserver.mvc.annotation.MultiPart;
import liheng.io.httpserver.mvc.annotation.QueryParam;
import liheng.io.httpserver.mvc.annotation.RequestBody;
import liheng.io.httpserver.util.ReflectUtil;
import org.apache.commons.collections4.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;

public class ControllerMethodInfo {

    private Object controller;
    private Method method;
    private HttpMethod[] httpMethods;

    public ControllerMethodInfo(Object object, Method method, HttpMethod[] httpMethod) {
        this.controller = object;
        this.method = method;
        this.httpMethods = httpMethod;
    }

    public boolean containHttpMethod(String method) {
        for (HttpMethod httpMethod : httpMethods) {
            if (httpMethod.getName().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    public Object invoke(Request request) throws InvocationTargetException, IllegalAccessException {
        Parameter[] parameters = method.getParameters();
        Object[] objects = new Object[parameters.length];
        for(int i = 0;i < parameters.length; i++){
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if(type == HttpServletRequest.class || type == Request.class){   // 拿到request对象
                objects[i] = request;
                continue;
            }

            Annotation annotations[] = parameter.getAnnotations();
            if (annotations.length==0){
                try {
                    objects[i] = processPojo(type, request);
                    continue;
                } catch (Exception e) {
                    throw new ServerInternalException("参数绑定java bean出错", e);
                }
            }

            Annotation annotation = annotations[0];
            Object o = null;
            Class<? extends Annotation> aType = annotation.annotationType();
            if (aType== MultiPart.class){
                o = processMultiPart(annotation,request,type);
            }else if (aType== QueryParam.class){
                o = processQueryParam(annotation, request, type);
            }else if (aType== FormParam.class){
                o = processFormParam(annotation, request, type);
            }else if (aType== RequestBody.class){
                o = JSON.parseObject(request.getResponseBody(), type);
                if (o==null&&((RequestBody)annotation).required()){
                    o = new Response(Status.BAD_REQUEST_400);
                }
            }else{
                try {
                    o = processPojo(type, request);
                } catch (Exception e) {
                    throw new ServerInternalException("参数绑定java bean出错", e);
                }
            }
            if (o != null && o instanceof HttpServletResponse) {
                return o;
            }
            objects[i] = o;
        }
        return method.invoke(controller,objects);
    }

    private Object processQueryParam(Annotation annotation, Request request, Class<?> type) {
        QueryParam queryParam = (QueryParam) annotation;
        String value = queryParam.value();
        Collection<String> strings = request.queryValue(value);
        boolean empty = CollectionUtils.isEmpty(strings);
        if (queryParam.require() && empty) {
            return new Response(Status.BAD_REQUEST_400);
        }
        return empty ? null : ReflectUtil.parseObj(strings.iterator().next(), type);
    }

    private Object processFormParam(Annotation annotation, Request request, Class<?> type) {
        FormParam formParam = (FormParam) annotation;
        String value = formParam.value();
        Collection<String> strings = request.formValue(value);
        boolean empty = CollectionUtils.isEmpty(strings);
        if (formParam.require() && empty) {
            return new Response(Status.BAD_REQUEST_400);
        }
        return empty ? null : ReflectUtil.parseObj(strings.iterator().next(), type);
    }

    private Object processMultiPart(Annotation annotation, Request request, Class<?> type) {
        MultiPart part = (MultiPart) annotation;      //仅支持文件上传
        String value = part.value();   // 大小写敏感
        MimeData mimeData = request.mimeValue(value);
        boolean empty = mimeData == null;
        if ((part.require() && empty) || (mimeData!=null&&mimeData.getContentType()==null)) {
            return new Response(Status.BAD_REQUEST_400);
        }
        return empty ? null : ReflectUtil.parseObj(mimeData, type);
    }

    private Object processPojo(Class<?> type, Request request) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = type.getMethods();
        Object o = type.newInstance();
        for (Method method : methods) {
            String methodName = method.getName();
            Parameter[] parameters = method.getParameters();
            if (!methodName.startsWith("set") || methodName.length() == 3 || parameters.length != 1) {
                continue;
            }
            String paraName = methodName.substring(3);
            //set方法一般是驼峰命名
            String val = request.getParameter(paraName.toLowerCase());
            if (val == null) {
                continue;
            }
            Parameter para = parameters[0];
            Class<?> parameterType  = para.getType();
            Object result = ReflectUtil.parseObj(val, parameterType); // 转换出错会抛出错误
            method.invoke(o, result);
        }
        return o;
    }

}
