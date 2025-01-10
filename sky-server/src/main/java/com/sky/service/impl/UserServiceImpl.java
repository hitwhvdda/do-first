package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    private static final String LOGIN_URL= "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 登陆获得用户信息
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //根据用户的授权吗去找腾讯获取openid
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        //这一片传参都是固定的前两个是咱们自己的小程序方面的，然后是用户自己的授权吗，最后一个固定写法
        String s = HttpClientUtil.doGet(LOGIN_URL, map);
        //得到的是字符串的json，转换一下
        JSONObject jsonObject = JSON.parseObject(s);
        String openId = jsonObject.getString("openid");
        //判断openid是不是真拿到了，空说明有问题
        if(openId==null)
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        //非空要查询是不是已经注册
        User user = userMapper.selectByOpenId(openId);
        //查不到就给他注册一下
        if(user==null){
            user=User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }
}
