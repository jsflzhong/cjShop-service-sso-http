package com.cj.core.service.impl;

import com.cj.common.pojo.TaotaoResult;
import com.cj.common.utils.JsonUtils;
import com.cj.core.mapper.TbUserMapper;
import com.cj.core.pojo.TbUser;
import com.cj.core.pojo.TbUserExample;
import com.cj.core.service.LoginService;
import com.cj.core.utils.CookieUtils;
import com.cj.core.utils.JedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

/**
 * @author cj
 * @description 登录Service
 * @date 2018/5/18
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private TbUserMapper userMapper;
    //操作Redis缓存,需要从IOC中注入这个对象.
    @Autowired
    private JedisClient jedisClient;

    //注入#Redis中,session的key.
    @Value("${REDIS_SESSION_KEY}")
    private String REDIS_SESSION_KEY;
    //注入#session的过期时间30分钟.单位为秒.(实际上是Redis缓存中的过期时间)
    @Value("${SESSION_EXPIRE}")
    private Integer SESSION_EXPIRE;

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return TaotaoResult
     * @author cj
     */
    @Override
    public TaotaoResult login(String username, String password, HttpServletRequest request,
                              HttpServletResponse response) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return TaotaoResult.build(400, "用户名或密码不能为空");
        }
        //创建查询条件.
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        //根据username来查数据库.因为这是在写登录功能.
        criteria.andUsernameEqualTo(username);
        //调用mapper,根据username查询.
        List<TbUser> list = userMapper.selectByExample(example);
        //判断返回的List是否为空.
        if (list == null || list.isEmpty()) {
            //如果返回的List为空,说明表中没有:输入的username对应的用户信息.
            return TaotaoResult.build(400, "用户名或密码错误");
        }
        //如果返回的List不为空.则取出用户对象.
        TbUser user = list.get(0);

        //校验密码.
        /**
         注意,涉及到MD5加密问题.
         以后的密码判断,要先调用MD5的方法.
         */
        //逻辑:判断上面查到的表中的user对象,它的password(加密状态),是不是equals:"被同样用MD5加密后的,传入的password参数".
        //DigestUtils是spring自己的对象!!
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
            //如果两个密码不相同,则返回错误信息.
            return TaotaoResult.build(400, "用户名或密码错误");
        }

        //如果两个密码相同,则说明登录成功了.
        //接下来应该:生成token (使用uuid)
        String token = UUID.randomUUID().toString();

        /**
         把用户信息写入redis.
         要操作Redis,肯定需要jedisClient对象.上面注入.

         参数分别为Redis中的key和value:
         key: 自己命名, 命名规则以前说过,要用冒号.便于在Redis中分层.
         (注意不要直接用token作为key!要有key在Redis中的分层思想.所谓分层,就是放入不同的目录中.)
         而且key的命名,要写入配置文件.再在上面注入.
         value: 就是用户对象.但是对象不能直接存入Redis.可以把它序列化成字符串.
         (对象的序列化的用途之一:便于网络传输和读写操作.比如在单点登录这里,是在把用户对象写入Redis缓存时,需要把它序列化)

         key:REDIS_SESSION:{TOKEN}
         value:user转json

         注意:把对象序列化之前,为了安全,先清空对象中的密码.
         不要把密码一起序列化后存入redis缓存.
         */
        user.setPassword(null);
        jedisClient.set(REDIS_SESSION_KEY + ":" + token, JsonUtils.objectToJson(user));

        /**
         调用jedisClient自己的方法,设置session的过期时间
         参数一是这个user对象在Redis的key.就是上行设置的key.
         参数二就是写进配置文件中的过期时间.在上面注入的.
         实际上并不是"session"的过期时间吧..应该是Redis缓存中,这个对象的过期时间.
         因为单点登录系统,我们并没有用session. 而是用Redis模拟session. 所以,应该只有Redis,没session啥事.
         */
        jedisClient.expire(REDIS_SESSION_KEY + ":" + token, SESSION_EXPIRE);

        /**
         写cookie
         取cookie用request,写cookie用response.
         但是我有十分方便的工具类.直接调用即可.已归纳到工具类.
         参数:request,response,cookie的名字自己起,cookie的值就是上面的token.
         注意:这里不要调用有第5参:即cookie的过期时间的那个方法.因为,我们是在做登录功能,要设置的是session的过期时间,而不是cookie的.
         由于单点登录,要把token保存进cookie,但是过期时间还要针对session的.(其实是Redis模拟的session.实际上是Redis中该对象的过期时间,而不是session的过期时间.)
         cookie的过期时间,不设置也可以.是有默认的.即:关闭浏览器时,cookie自动失效.
         */
        CookieUtils.setCookie(request, response, "TT_TOKEN", token);
        //CookieUtils.setCookie(request, response, "TT_TOKEN", token,SESSION_EXPIRE);

        return TaotaoResult.ok(token);
    }

    /**
     * 根据token获取redis中的用户数据
     *
     * @param token 保存在cookie中的session的token
     * @return TaotaoResult
     * @author cj
     */
    @Override
    public TaotaoResult getUserByToken(String token) {

        if(StringUtils.isEmpty(token)) {
            return TaotaoResult.build(400, "token不能为空");
        }

        //使用jedis,"根据token取用户信息".
        //这个key,就是上面用jedis往Redis中存入数据时,使用的key.
        //jedisClient.set(REDIS_SESSION_KEY + ":" + token, JsonUtils.objectToJson(user));
        String json = jedisClient.get(REDIS_SESSION_KEY + ":" + token);
        //判断是否查询到结果
        if (StringUtils.isEmpty(json)) {
            //如果没查到传入的token在Redis中对应的对象. 返回错误信息.
            return TaotaoResult.build(400, "用户session已经过期");
        }
        //如果在Redis中查到了对应token的对象...
        /**
         注意:需要先把json转换成java对象(TbUser),再用TaotaoResult包装!看需求文档中的返回值里的"data"部分.
         千万不要把上面查到的JSON数据,直接用TaotaoResult包装.
         那样的话,到客户端,它取出来的还是个JSON字符串.
         */
        TbUser user = JsonUtils.jsonToPojo(json, TbUser.class);

        //更新Redis中,用户session的过期时间
        //key还是上面的key.
        //时间这种东西,还是上面从IOC注入的,在配置文件中配置的时间.
        jedisClient.expire(REDIS_SESSION_KEY + ":" + token, SESSION_EXPIRE);

        //返回 包装了user对象的TaotaoResult对象.
        return TaotaoResult.ok(user);
    }

}
