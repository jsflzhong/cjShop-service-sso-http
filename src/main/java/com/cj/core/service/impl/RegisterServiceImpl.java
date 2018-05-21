package com.cj.core.service.impl;

import com.cj.common.pojo.TaotaoResult;
import com.cj.core.mapper.TbUserMapper;
import com.cj.core.pojo.TbUser;
import com.cj.core.pojo.TbUserExample;
import com.cj.core.service.RegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * @author cj
 * @author cj
 * @description sso服务service
 * @date 2018/5/18
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private TbUserMapper userMapper;

    /**
     * 注册用户的服务
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
    @Override
    public TaotaoResult checkData(String param, int type) {

        if (StringUtils.isEmpty(param)) return TaotaoResult.ok(false);

        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        //因为类型可能有3种,所以得判断. 1,2,3这三种类型代表的意义,一定要写在注释里.
        //1、2、3分别代表username、phone、email
        if (1 == type) {
            criteria.andUsernameEqualTo(param);
        } else if (2 == type) {
            criteria.andPhoneEqualTo(param);
        } else if (3 == type) {
            criteria.andEmailEqualTo(param);
        }
        //调用mapper,执行查询.
        List<TbUser> list = userMapper.selectByExample(example);
        //判断查询结果是否为空
        if (list == null || list.isEmpty()) {
            //如果没查到数据,说明该数据不存在,则可以用这个数据来注册.返回true.
            return TaotaoResult.ok(true);
        }
        return TaotaoResult.ok(false);
    }

    /**
     * 注册用户的服务
     *
     * @param user TbUser
     * @return register
     * {
     * status: 400
     * msg: "注册失败. 请校验数据后请再提交数据."
     * data: null
     * }
     * @author cj
     */
    @Override
    public TaotaoResult register(TbUser user) {
        //校验数据......
        //校验用户名、密码不能为空
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            //注意,如果为空的话,要调用TaotaoResult的build()方法了. 返回400和错误信息.
            return TaotaoResult.build(400, "用户名或密码不能为空");
        }
        //调用上面的校验方法,校验数据是否已经存在.
        //校验用户名
        TaotaoResult result = checkData(user.getUsername(), 1);
        //如果校验的结果为false
        if (!(boolean) result.getData()) {
            //返回TaotaoResult的错误信息.
            return TaotaoResult.build(400, "用户名重复");
        }
        //校验手机号是否为空.(手机号在表里允许为空)
        if (user.getPhone() != null) {
            //校验手机号是否已经存在.
            result = checkData(user.getPhone(), 2);
            //如果返回的结果为false,返回错误信息.
            if (!(boolean) result.getData()) {
                return TaotaoResult.build(400, "手机号重复");
            }
        }
        //校验邮箱.
        if (user.getEmail() != null) {
            result = checkData(user.getEmail(), 3);
            if (!(boolean) result.getData()) {
                return TaotaoResult.build(400, "邮箱重复");
            }
        }

        //补全user对象的字段.
        user.setCreated(new Date());
        user.setUpdated(new Date());

        /**
         对密码进行MD5加密.
         注意,在注册时,存入数据库中的密码,都必须用MD5加密的!!
         思路:从最内层看.从user DTO中把password取出来先,调用"spring的"DigestUtils对象的方法,选择返回十六进制字符串的那个,给取出的password加密.
         然后把这个加密后的password,再用setter设置回user对象.
         最后再插入这个对象!
         DigestUtils对象,是spring自己的对象!
         */
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));

        //调用mapper,插入数据.
        userMapper.insert(user);

        //返回TaotaoResult的成功信息.
        return TaotaoResult.ok();
    }

}
