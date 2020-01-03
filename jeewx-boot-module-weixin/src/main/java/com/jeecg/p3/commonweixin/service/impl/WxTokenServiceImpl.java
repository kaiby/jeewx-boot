package com.jeecg.p3.commonweixin.service.impl;

import com.jeecg.p3.commonweixin.dao.MyJwWebJwidDao;
import com.jeecg.p3.commonweixin.entity.MyJwWebJwid;
import com.jeecg.p3.commonweixin.service.WxTokenService;
import com.jeecg.p3.commonweixin.util.AccessTokenUtil;
import com.jeecg.p3.redis.service.RedisService;
import com.jeecg.p3.weixin.dao.WeixinMenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service("wxTokenService")
public class WxTokenServiceImpl implements WxTokenService {

    @Resource
    private MyJwWebJwidDao myJwWebJwidDao;

    @Autowired
    private RedisService redisService;

    @Override
    public String getToken(String wxId){
        String key = wxId + "_token";
        String token = (String)redisService.get(key);
        return token;
    }

    @Override
    public String resetToken(String wxId) {
        String key = wxId + "_token";
        MyJwWebJwid wx = myJwWebJwidDao.queryByJwid(wxId);
        String token = AccessTokenUtil.getWxAccseeToken(wx.getWeixinAppId(),wx.getWeixinAppSecret());
        wx.setAccessToken(token);
        wx.setTokenGetTime(new Date());
        myJwWebJwidDao.update(wx);
        redisService.set(key,token,60*100L);
        return token;
    }

}
