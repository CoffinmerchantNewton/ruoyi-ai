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

    /**
     * 钉钉组织架构同步配置
     */
    private Sync sync = new Sync();

    /**
     * 钉钉登录相关配置
     */
    private Login login = new Login();

    @Data
    public static class Sync {
        /**
         * 是否启用组织同步（定时任务）
         */
        private boolean enabled = false;

        /**
         * 组织同步 cron 表达式
         */
        private String orgCron = "0 10 0 * * ?";

        /**
         * 钉钉根部门挂载到本地哪个部门ID下（默认 100：初始化数据的公司根部门）
         */
        private Long rootDeptId = 100L;

        /**
         * 是否同步更新本地用户的部门归属
         */
        private boolean syncUserDept = true;

        /**
         * 是否同步更新用户的基础信息（昵称/手机号/邮箱/头像），默认 false 避免覆盖本地修改
         */
        private boolean updateUserProfile = false;

        /**
         * 本地不存在用户时是否自动创建（默认 false）
         */
        private boolean createMissingUsers = false;
    }

    @Data
    public static class Login {
        /**
         * 系统中不存在绑定用户时，是否自动创建默认用户（默认 false：直接抛错）
         */
        private boolean createMissingUser = false;
    }

}
