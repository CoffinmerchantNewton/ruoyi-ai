package org.ruoyi.dingtalk.api;

import com.alibaba.fastjson2.JSONObject;
import org.ruoyi.common.core.exception.ServiceException;
import org.ruoyi.dingtalk.util.HttpUtil;
import org.ruoyi.dingtalk.vo.AccessToken;
import org.ruoyi.dingtalk.util.ApiUrls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 钉钉基础接口
 *
 * @author sunjianlei
 */
@Component
@Slf4j
public class JdtBaseAPI {

    @Autowired
    private ApiUrls apiUrls;


    /**
     * @param appKey    应用的唯一标识key
     * @param appSecret 应用的密钥
     * @return AccessToken
     */
    public AccessToken getAccessToken(String appKey, String appSecret) {
        AccessToken accessToken = null;
        String url = apiUrls.getAccessTokenUrl(appKey, appSecret);
        JSONObject response = HttpUtil.sendGet(url);

        // 如果请求成功
        if (response != null) {
            try {
                String access_token = response.getString("access_token");
                int expires_in = response.getIntValue("expires_in");
                accessToken = new AccessToken(access_token, expires_in);
            } catch (Exception e) {
                // 获取token失败
                Integer errcode = response.getInteger("errcode");
                String errmsg = response.getString("errmsg");
                throw new ServiceException("获取钉钉AccessToken失败: " + errmsg, errcode);
            }
        }
        return accessToken;
    }

}
