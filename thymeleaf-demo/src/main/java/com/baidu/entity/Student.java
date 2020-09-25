package com.baidu.entity;

/**
 * @ClassName Student
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/15
 * @Version V1.0
 **/
public class Student {

    private String code;
    private String pass;
    private int age;
    private String LikeColor;

    public Student() {
    }

    public Student(String code, String pass, int age, String likeColor) {
        this.code = code;
        this.pass = pass;
        this.age = age;
        LikeColor = likeColor;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLikeColor() {
        return LikeColor;
    }

    public void setLikeColor(String likeColor) {
        LikeColor = likeColor;
    }
}
