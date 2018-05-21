package com.cj.core.service;

import com.cj.common.pojo.TaotaoResult;
import com.cj.core.pojo.TbUser;

/**
 * @author cj
 * @author cj
 * @description sso服务service
 * @date 2018/5/18
 */
public interface RegisterService {

    /**
     * 检查注册数据的合法性.
     *
     * @param param 注册参数
     * @param type  可选参数1、2、3分别代表username、phone、email
     * @return TaotaoResult
     * {
     * status: 200 //200 成功
     * msg: "OK" // 返回信息消息
     * data: false // 返回数据，true：数据可用，false：数据不可用
     * }
     * @author cj
     */
    TaotaoResult checkData(String param, int type);

    /**
     * 注册用户的服务
     *
     * @param user TbUser
     * @return register
     *  {
     *      status: 400
     *   msg: "注册失败. 请校验数据后请再提交数据."
     *      data: null
     *  }
     * @author cj
     */
    TaotaoResult register(TbUser user);
}
