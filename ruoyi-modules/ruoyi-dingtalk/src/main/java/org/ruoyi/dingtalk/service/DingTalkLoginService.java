package org.ruoyi.dingtalk.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.core.constant.Constants;
import org.ruoyi.common.core.domain.model.VisitorLoginUser;
import org.ruoyi.common.core.enums.DeviceType;
import org.ruoyi.common.core.enums.UserType;
import org.ruoyi.common.core.utils.MessageUtils;
import org.ruoyi.common.core.utils.ServletUtils;
import org.ruoyi.common.core.utils.SpringUtils;
import org.ruoyi.common.log.event.LogininforEvent;
import org.ruoyi.common.satoken.utils.LoginHelper;
import org.ruoyi.dingtalk.api.JdtBaseAPI;
import org.ruoyi.dingtalk.api.JdtUserAPI;
import org.ruoyi.dingtalk.api.response.Response;
import org.ruoyi.dingtalk.config.DingTalkProperties;
import org.ruoyi.dingtalk.util.HttpUtil;
import org.ruoyi.dingtalk.vo.AccessToken;
import org.ruoyi.dingtalk.vo.User;
import org.ruoyi.system.domain.SysUser;
import org.ruoyi.system.domain.bo.SysUserBo;
import org.ruoyi.system.domain.vo.LoginVo;
import org.ruoyi.system.domain.vo.SysUserVo;
import org.ruoyi.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.taobao.api.ApiException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * 钉钉登录服务
 *
 * @author ruoyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkLoginService {

    private final ISysUserService userService;
    private final DingTalkProperties dingTalkProperties;
    private final JdtBaseAPI jdtBaseAPI;
    private final JdtUserAPI jdtUserAPI;

    /**
     * 钉钉登录
     *
     * @param unionId 钉钉 unionId
     * @return 登录信息
     */
    public LoginVo dingTalkLogin(String unionId) {
        // 获取 AccessToken
        AccessToken accessToken = jdtBaseAPI.getAccessToken(
            dingTalkProperties.getAppKey(),
            dingTalkProperties.getAppSecret()
        );

        if (accessToken == null) {
            throw new RuntimeException("获取钉钉 AccessToken 失败");
        }

        // 根据 unionId 获取用户 userid
        Response<String> useridResponse = jdtUserAPI.getUseridByUnionid(unionId, accessToken.getAccessToken());
        if (!useridResponse.isSuccess() || useridResponse.getResult() == null) {
            throw new RuntimeException("根据 unionId 获取用户失败: " + useridResponse.getErrmsg());
        }

        String userid = useridResponse.getResult();

        // 获取用户详情
        Response<User> userResponse = jdtUserAPI.getUserById(userid, accessToken.getAccessToken());
        if (!userResponse.isSuccess() || userResponse.getResult() == null) {
            throw new RuntimeException("获取用户详情失败: " + userResponse.getErrmsg());
        }

        User dingTalkUser = userResponse.getResult();

        // 使用 unionId 查询绑定用户，如未绑定用户则按配置决定：抛错 / 创建默认用户
        SysUserVo user = userService.selectUserByUnionId(unionId);
        VisitorLoginUser loginUser = new VisitorLoginUser();

        if (ObjectUtil.isNull(user)) {
            if (!dingTalkProperties.getLogin().isCreateMissingUser()) {
                throw new RuntimeException("钉钉用户未绑定系统用户，请联系管理员进行绑定");
            }
            SysUserBo sysUser = new SysUserBo();
            String nickName = dingTalkUser.getName() != null && !dingTalkUser.getName().isBlank()
                ? dingTalkUser.getName()
                : "用户" + UUID.randomUUID().toString().replace("-", "");
            String base = (unionId == null ? "" : unionId).replaceAll("[^a-zA-Z0-9]", "");
            if (base.isBlank()) {
                base = UUID.randomUUID().toString().replace("-", "");
            }
            // user_name 最大 30，这里做一个稳定且不易冲突的账号名
            String userName = "dd_" + (base.length() > 26 ? base.substring(base.length() - 26) : base);

            sysUser.setUserName(userName);
            sysUser.setNickName(nickName);
            // 由于需要兼顾到修改密码的情况，所以默认密码是12345678
            sysUser.setPassword(BCrypt.hashpw("12345678"));
            sysUser.setUserType(UserType.APP_USER.getUserType());
            sysUser.setUnionId(unionId);
            sysUser.setUserBalance(0.0);
            if (dingTalkUser.getMobile() != null && !dingTalkUser.getMobile().isBlank()) {
                sysUser.setPhonenumber(dingTalkUser.getMobile());
            }
            if (dingTalkUser.getEmail() != null && !dingTalkUser.getEmail().isBlank()) {
                sysUser.setEmail(dingTalkUser.getEmail());
            }
            if (dingTalkUser.getAvatar() != null && !dingTalkUser.getAvatar().isBlank()) {
                sysUser.setAvatar(dingTalkUser.getAvatar());
            }
            // 注册用户，设置默认租户为0
            SysUser registerUser = userService.registerUser(sysUser, "0");

            // 构建登录用户信息
            loginUser.setTenantId("0");
            loginUser.setUserId(registerUser.getUserId());
            loginUser.setUsername(registerUser.getUserName());
            loginUser.setUserType(UserType.APP_USER.getUserType());
            loginUser.setOpenid(unionId);
            loginUser.setNickName(registerUser.getNickName());
            loginUser.setAvatar(registerUser.getAvatar());
        } else {
            // 根据登录用户的数据不同自行创建 loginUser
            loginUser.setTenantId(user.getTenantId());
            loginUser.setUserId(user.getUserId());
            loginUser.setUsername(user.getUserName());
            loginUser.setUserType(user.getUserType());
            loginUser.setNickName(user.getNickName());
            loginUser.setAvatar(user.getAvatar());
            loginUser.setOpenid(unionId);
        }

        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.XCX);
        recordLogininfor(loginUser.getTenantId(), loginUser.getUsername(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        LoginVo loginVo = new LoginVo();
        // 生成令牌
        loginVo.setToken(StpUtil.getTokenValue());
        loginVo.setUserInfo(loginUser);
        return loginVo;
    }

    /**
     * 钉钉扫码登录（页面内二维码 -> 回调带 code）
     *
     * <p>前端通过 DDLogin 扫码后，回调地址会携带 {@code code} 参数（临时授权码）。</p>
     *
     * @param authCode 钉钉临时授权码（tmp_auth_code / code）
     * @return 登录信息
     */
    public LoginVo dingTalkLoginByAuthCode(String authCode) {
        String unionId = getUnionIdByAuthCode(authCode);
        return dingTalkLogin(unionId);
    }

    /**
     * 钉钉企业内部应用SSO登录（使用 dingtalk-jsapi）
     *
     * <p>前端通过 dingtalk-jsapi 的 dd.requestAuthCode 获取授权码后调用此接口。</p>
     * <p>注意：clientId 和 clientSecret 复用 appKey 和 appSecret。</p>
     *
     * @param code   钉钉授权码
     * @param corpId 企业ID
     * @return 登录信息
     */
    public LoginVo dingTalkLoginByCode(String code, String corpId) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("code 不能为空");
        }
        if (corpId == null || corpId.trim().isEmpty()) {
            throw new RuntimeException("corpId 不能为空");
        }

        // 复用 appKey 和 appSecret 作为 clientId 和 clientSecret
        String clientId = dingTalkProperties.getAppKey();
        String clientSecret = dingTalkProperties.getAppSecret();
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new RuntimeException("钉钉应用配置缺失：appKey 或 appSecret 未配置");
        }

        try {
            // 获取 AccessToken
            String accessToken = getAccessToken(clientId, clientSecret);
            if (accessToken == null) {
                throw new RuntimeException("获取钉钉 AccessToken 失败");
            }

            // 获取用户信息
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getuserinfo");
            OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
            req.setCode(code);

            OapiV2UserGetuserinfoResponse rsp = client.execute(req, accessToken);
            if (!rsp.isSuccess()) {
                log.error("获取用户信息失败: {}", rsp.getErrmsg());
                throw new RuntimeException("获取用户信息失败: " + rsp.getErrmsg());
            }

            // 从响应中提取 unionId
            com.dingtalk.api.response.OapiV2UserGetuserinfoResponse.UserGetByCodeResponse userInfo = rsp.getResult();
            if (userInfo == null) {
                throw new RuntimeException("钉钉返回用户信息为空");
            }

            String unionId = userInfo.getUnionid();
            if (unionId == null || unionId.isBlank()) {
                throw new RuntimeException("钉钉返回缺少 unionid");
            }

            log.info("成功获取用户信息，unionId: {}", unionId);
            // 使用 unionId 完成登录
            return dingTalkLogin(unionId);
        } catch (ApiException e) {
            log.error("钉钉企业内部应用SSO登录失败: {}", e.getMessage(), e);
            throw new RuntimeException("钉钉企业内部应用SSO登录失败: " + e.getMessage());
        }
    }

    /**
     * 使用 clientId 和 clientSecret 获取 AccessToken（企业内部应用）
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @return AccessToken
     */
    private String getAccessToken(String clientId, String clientSecret) {
        try {
            String url = "https://api.dingtalk.com/v1.0/oauth2/accessToken";
            JSONObject body = new JSONObject();
            body.put("appKey", clientId);
            body.put("appSecret", clientSecret);

            JSONObject response = HttpUtil.httpRequest(url, "POST", body.toJSONString());
            if (response == null) {
                log.error("获取 AccessToken 失败：响应为空");
                return null;
            }

            // 检查是否有错误
            Object code = response.get("code");
            if (code != null && !"0".equals(code.toString())) {
                String message = response.getString("message");
                log.error("获取 AccessToken 失败: code={}, message={}", code, message);
                return null;
            }

            // 获取 accessToken
            String accessToken = response.getString("accessToken");
            if (accessToken == null || accessToken.trim().isEmpty()) {
                log.error("AccessToken 为空");
                return null;
            }
            return accessToken;
        } catch (Exception e) {
            log.error("获取 AccessToken 异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过钉钉扫码回调 code 换取 unionId
     *
     * @param authCode 临时授权码（tmp_auth_code / code）
     * @return unionId
     */
    public String getUnionIdByAuthCode(String authCode) {
        if (authCode == null || authCode.isBlank()) {
            throw new RuntimeException("authCode 不能为空");
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = sign(timestamp, dingTalkProperties.getAppSecret());

        // 钉钉：sns/getuserinfo_bycode（扫码登录）
        // https://oapi.dingtalk.com/sns/getuserinfo_bycode?signature=...&timestamp=...&accessKey=...
        String url = String.format(
            "%s/sns/getuserinfo_bycode?signature=%s&timestamp=%s&accessKey=%s",
            dingTalkProperties.getOapiUrl(),
            signature,
            timestamp,
            dingTalkProperties.getAppKey()
        );

        JSONObject body = new JSONObject();
        body.put("tmp_auth_code", authCode);
        JSONObject resp = HttpUtil.httpRequest(url, "POST", body.toJSONString());
        if (resp == null) {
            throw new RuntimeException("钉钉返回为空");
        }

        Integer errCode = resp.getInteger("errcode");
        String errMsg = resp.getString("errmsg");
        if (errCode == null || errCode != 0) {
            throw new RuntimeException("钉钉获取用户信息失败: " + (errMsg == null ? errCode : errMsg));
        }

        JSONObject userInfo = resp.getJSONObject("user_info");
        if (userInfo == null) {
            throw new RuntimeException("钉钉返回缺少 user_info");
        }
        String unionId = userInfo.getString("unionid");
        if (unionId == null || unionId.isBlank()) {
            throw new RuntimeException("钉钉返回缺少 unionid");
        }
        return unionId;
    }

    private static String sign(String timestamp, String appSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                appSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(timestamp.getBytes(StandardCharsets.UTF_8));
            String base64 = Base64.getEncoder().encodeToString(rawHmac);
            return URLEncoder.encode(base64, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("生成钉钉签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录登录信息
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    private void recordLogininfor(String tenantId, String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setTenantId(tenantId);
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }
}
