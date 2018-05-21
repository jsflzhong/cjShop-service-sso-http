package com.cj.core.controller;

import com.cj.common.pojo.TaotaoResult;
import com.cj.common.utils.ExceptionUtil;
import com.cj.core.pojo.TbUser;
import com.cj.core.service.RegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cj
 * @description sso服务Controller
 * @date 2018/5/18
 */
@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    /**
     * 检查注册数据的合法性.
     *
     * @param param    注册参数
     * @param type     可选参数1、2、3分别代表username、phone、email
     * @param callback 可选参数. jsonP.
     * @return TaotaoResult
     * {
     * status: 200 //200 成功
     * msg: "OK" // 返回信息消息
     * data: false // 返回数据，true：数据可用，false：数据不可用
     * }
     * @author cj
     */
    @RequestMapping("/check/{param}/{type}")
    @ResponseBody
    public Object checkData(@PathVariable String param, @PathVariable Integer type, String callback) {
        try {
            //调用service.
            TaotaoResult result = registerService.checkData(param, type);
            //2>.判断第三参数如果不为空.说明调用者希望支持JSONP.
            if (StringUtils.isNotBlank(callback)) {
                //3>.那么,就创建MappingJacksonValue对象,并用构函,包装上面返回的结果.
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
                //4>.设置参数.
                mappingJacksonValue.setJsonpFunction(callback);
                //5>.返回MappingJacksonValue.
                return mappingJacksonValue;
            }
            //如果第三参为空,说明调用者不想调用JSONP形式.
            return result;
        } catch (Exception e) {
            //发布服务时,都需要这两步.
            e.printStackTrace();
            //给调用者返回异常的堆栈信息.
            return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
        }
    }

    /**
     * 用户注册服务.
     *
     * @param user TbUser DTO
     * @return TaotaoResult
     * {
     * status: 400
     * msg: "注册失败. 请校验数据后请再提交数据."
     * data: null
     * }
     * @author cj
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public TaotaoResult register(TbUser user) {
        try {
            //调用service,返回结果.
            TaotaoResult result = registerService.register(user);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
        }
    }
}
