package org.ruoyi.dingtalk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 钉钉配置属性
 *
 * @author ruoyi
 */
@Data
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkProperties {

    /**
     * 钉钉应用的 AppKey
     */
    private String appKey;

    /**
     * 钉钉应用的 AppSecret
     */
    private String appSecret;

    /**
     * 钉钉 API 地址（OAPI）
     */
    private String oapiUrl = "https://oapi.dingtalk.com";

    /**
     * 钉钉 API 地址（API）
     */
    private String apiUrl = "https://oapi.dingtalk.com";

}
