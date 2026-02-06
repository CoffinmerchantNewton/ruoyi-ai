package org.ruoyi.dingtalk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.core.domain.R;
import org.ruoyi.dingtalk.service.DingTalkLoginService;
import org.ruoyi.system.domain.vo.LoginVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钉钉用户控制器
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/dingtalk")
@RequiredArgsConstructor
public class DingTalkUserController {

    private final DingTalkLoginService loginService;

    /**
     * 钉钉登录
     *
     * @param unionId  钉钉 unionId（可选）
     * @param authCode 钉钉扫码回调 code（可选）
     * @return 登录信息
     */
    @GetMapping("/login")
    public R<LoginVo> login(
        @RequestParam(required = false) String unionId,
        @RequestParam(required = false) String authCode
    ) {
        try {
            LoginVo loginVo;
            if (unionId != null && !unionId.isBlank()) {
                loginVo = loginService.dingTalkLogin(unionId);
            } else if (authCode != null && !authCode.isBlank()) {
                loginVo = loginService.dingTalkLoginByAuthCode(authCode);
            } else {
                return R.fail("参数缺失：unionId 或 authCode 至少传一个");
            }
            return R.ok(loginVo);
        } catch (Exception e) {
            log.error("钉钉登录失败", e);
            return R.fail("钉钉登录失败: " + e.getMessage());
        }
    }

    /**
     * 钉钉企业内部应用SSO登录（使用 dingtalk-jsapi）
     *
     * @param code   钉钉授权码（通过 dd.requestAuthCode 获取）
     * @param corpId 企业ID
     * @return 登录信息
     */
    @GetMapping("/sso/login")
    public R<LoginVo> ssoLogin(
        @RequestParam String code,
        @RequestParam String corpId
    ) {
        try {
            LoginVo loginVo = loginService.dingTalkLoginByCode(code, corpId);
            return R.ok(loginVo);
        } catch (Exception e) {
            log.error("钉钉企业内部应用SSO登录失败", e);
            return R.fail("钉钉企业内部应用SSO登录失败: " + e.getMessage());
        }
    }
}
