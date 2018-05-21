package com.cj.core.controller;

import com.cj.common.pojo.TaotaoResult;
import com.cj.common.utils.ExceptionUtil;
import com.cj.core.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cj
 * @description 登录的Controller
 * @date 2018/5/18
 */
@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 发布用户登录服务.
     *
     * @param username 用户名
     * @param password 密码
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return TaotaoResult
     * @author cj
     */
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    @ResponseBody
    public TaotaoResult login(String username, String password,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            //调用service,传递四个参数过去.返回TaotaoResult.
            TaotaoResult result = loginService.login(username, password, request, response);
            //用注解返回TaotaoResult的JSON数据.
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
        }
    }

    /**
     * 发布:根据"token"查询用户信息 服务.
     * @param token  保存在cookie中的session的token
     * @param callback jsonp
     * @return TaotaoResult
     * @author cj
     */
    @RequestMapping("/user/token/{token}")
    @ResponseBody
    public Object getUserByToken(@PathVariable String token, String callback) {
        try {
            TaotaoResult result = loginService.getUserByToken(token);
            //支持jsonp调用...
            if (StringUtils.isNotBlank(callback)) {
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
                mappingJacksonValue.setJsonpFunction(callback);
                return mappingJacksonValue;
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
        }
    }
}
