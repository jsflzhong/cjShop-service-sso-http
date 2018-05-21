package com.cj.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cj
 * @description 跳转到注册和登录的jsp页面的Controller
 * @date 2018/5/18
 */
@Controller
public class PageController {

    /**
     * 展示登录页面
     * 添加两个参数.一个是想要跳转到的页面的url. 一个是给前端jsp发送数据用的model.
     *
     */
    @RequestMapping("/page/login")
    public String showLogin(String redirectURL,Model model) {
        //需要把参数一,即url的值,通过域对象,传递给login.jsp中的JavaScript.
        //因为在这里无法判断登录是否成功了.具体判断跳转到哪里的工作,是在login.jsp中的JavaScript中做的.
        //jsp: var redirectUrl = "${redirect}"; //从model(request)中,取出key为"redirect"的数据. 所以controller那边,在放入参数时,key应该为"redirect".
        model.addAttribute("redirect", redirectURL);
        //返回逻辑视图. mvc配置那边有视图解析器给配前后缀.
        return "login";
    }

    /**
     * 展示注册页面
     */
    @RequestMapping("/page/register")
    public String showRegister() {
        return "register";
    }

}
