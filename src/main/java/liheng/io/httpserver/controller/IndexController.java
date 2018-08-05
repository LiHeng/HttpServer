package liheng.io.httpserver.controller;

import liheng.io.httpserver.http.HttpMethod;
import liheng.io.httpserver.http.request.Request;
import liheng.io.httpserver.http.response.FileResponse;
import liheng.io.httpserver.http.response.JsonResponse;
import liheng.io.httpserver.http.response.Response;
import liheng.io.httpserver.http.response.Status;
import liheng.io.httpserver.mvc.annotation.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Controller
public class IndexController {
    @WebPath("/")
    public Response index(Request request) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream("pages/index.html");
        File index = Files.createTempFile("index", ".html").toFile();
        FileOutputStream os = new FileOutputStream(index);
        IOUtils.copy(is, os);
        is.close();
        os.close();
        return new FileResponse(Status.OK_200, index);
    }

    @WebPath(value = "/user",method = {HttpMethod.POST,HttpMethod.GET})  //默认是GET请求
    public Response getUser(Request request) throws IOException {
        System.out.println(request.getContentLength());
        User user = new User();
        user.setName("lihang");
        user.setAge(20);
        return new JsonResponse(Status.OK_200,user);
    }


    @WebPath(value = "/query",method = {HttpMethod.POST})
    public Response query(Request request, @RequestBody User user) throws IOException {
        System.out.println(user.getName());
        return new JsonResponse(Status.OK_200,user);
    }

    @WebPath(value = "/pojo",method = {HttpMethod.POST,HttpMethod.GET})
    public Response pojo(Request request, User user) throws IOException {
        System.out.println(user.getName());
        return new JsonResponse(Status.OK_200,"Login success!");
    }

    @WebPath(value = "/param",method = {HttpMethod.POST,HttpMethod.GET})      //暂时不支持原生类型的注入
    public Response param(Request request, @QueryParam("name")String name, @QueryParam("age") Integer age) throws IOException {
        System.out.println(name);
        System.out.println(age);
        return new JsonResponse(Status.OK_200,"Login success!");
    }

    @WebPath(value = "/form",method = {HttpMethod.POST})
    public Response form(Request request, @FormParam("name") String name, @FormParam("age") Integer age, @MultiPart("file")byte[] data) throws IOException {
        System.out.println(name);
        System.out.println(age);
        if (data!=null){
            File index = new File("file");
            index.createNewFile();
            FileOutputStream os = new FileOutputStream(index);
            os.write(data);
            os.close();
        }
        return new JsonResponse(Status.OK_200,"Success!");
    }
}
