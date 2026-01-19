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
     * @param unionId 钉钉 unionId
     * @return 登录信息
     */
    @GetMapping("/login")
    public R<LoginVo> login(@RequestParam String unionId) {
        try {
            LoginVo loginVo = loginService.dingTalkLogin(unionId);
            return R.ok(loginVo);
        } catch (Exception e) {
            log.error("钉钉登录失败", e);
            return R.fail("钉钉登录失败: " + e.getMessage());
        }
    }
}
