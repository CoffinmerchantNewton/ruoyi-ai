package org.ruoyi.dingtalk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.common.core.domain.R;
import org.ruoyi.dingtalk.service.DingTalkOrgSyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 钉钉同步控制器（手动触发）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dingtalk/sync")
public class DingTalkSyncController {

    private final DingTalkOrgSyncService orgSyncService;

    /**
     * 手动触发：同步钉钉组织架构（部门 + 用户部门归属）
     */
    @PostMapping("/org")
    public R<DingTalkOrgSyncService.SyncResult> syncOrg() {
        try {
            return R.ok(orgSyncService.syncOrg());
        } catch (Exception e) {
            log.error("手动触发钉钉组织同步失败", e);
            return R.fail("同步失败: " + e.getMessage());
        }
    }
}

