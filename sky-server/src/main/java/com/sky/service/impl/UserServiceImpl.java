package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    UserMapper userMapper;
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getString(userLoginDTO);

        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user=userMapper.getByOpenid(openid);

        if(user==null){
            user=User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getString(UserLoginDTO userLoginDTO) {
        Map<String, String> mp=new TreeMap<>();
        mp.put("appid",weChatProperties.getAppid());
        mp.put("secret",weChatProperties.getSecret());
        mp.put("js_code", userLoginDTO.getCode());
        mp.put("grant_type","authorization_code");
        String json=HttpClientUtil.doGet(WX_LOGIN_URL,mp);
        System.out.println("json== "+json);
        JSONObject jsonObject= JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
