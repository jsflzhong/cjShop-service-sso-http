package com.cj.core.service;

import com.cj.common.pojo.TaotaoResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 登录Service
 * @date 2018/5/18
 * @author cj
 */
public interface LoginService {

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return TaotaoResult
     * @author cj
     */
    TaotaoResult login(String username, String password, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据token获取redis中的用户数据
     * @param token 保存在cookie中的session的token
     * @return TaotaoResult
     * @author cj
     */
    TaotaoResult getUserByToken(String token);
}
