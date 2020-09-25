package com.baidu.controller;

import com.baidu.entity.Student;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/15
 * @Version V1.0
 **/
@Controller
public class TestController {

    @GetMapping("test")
    public String test(ModelMap map){
        map.put("name","tomcat");
        return "test";
    }

    @GetMapping("stu")
    public String student(ModelMap map){
        Student student = new Student();
        student.setCode("007");
        student.setPass("123");
        student.setAge(28);
        student.setLikeColor("<font color='red'>红色</fond>");
        map.put("stu",student);
        return "test";
    }
    @GetMapping("list")
    public String list(ModelMap map){
        Student s1=new Student("001","111",18,"red");
        Student s2=new Student("002","222",19,"red");
        Student s3=new Student("003","333",16,"blue");
        Student s4=new Student("004","444",28,"blue");
        Student s5=new Student("005","555",68,"blue");
        map.put("stuList", Arrays.asList(s1,s2,s3,s4,s5));
        return "list";
    }
}
