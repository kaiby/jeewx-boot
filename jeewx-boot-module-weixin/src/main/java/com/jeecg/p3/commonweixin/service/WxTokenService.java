package com.jeecg.p3.commonweixin.service;

public interface WxTokenService {

    /**
     * 获取微信TOKEN
     * @param wxId 微信ID
     * @return
     */
    public String getToken(String wxId);

    /**
     * 重置TOKEN
     * @param wxId 微信ID
     * @return
     */
    public String resetToken(String wxId);
}
