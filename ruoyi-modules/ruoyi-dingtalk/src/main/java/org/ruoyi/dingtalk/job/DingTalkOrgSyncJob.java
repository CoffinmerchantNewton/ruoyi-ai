package org.ruoyi.dingtalk.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.dingtalk.config.DingTalkProperties;
import org.ruoyi.dingtalk.service.DingTalkOrgSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 钉钉组织架构定时同步任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkOrgSyncJob {

    private final DingTalkProperties dingTalkProperties;
    private final DingTalkOrgSyncService orgSyncService;

    @Scheduled(cron = "${dingtalk.sync.org-cron:0 10 0 * * ?}")
    public void syncOrg() {
        if (!dingTalkProperties.getSync().isEnabled()) {
            return;
        }
        try {
            DingTalkOrgSyncService.SyncResult res = orgSyncService.syncOrg();
            log.info("钉钉组织同步完成：deptCreated={}, deptUpdated={}, userDeptUpdated={}, userProfileUpdated={}, userCreated={}, userNotFound={}, ddUserFetched={}, warnings={}",
                    res.getDeptCreated(), res.getDeptUpdated(), res.getUserDeptUpdated(), res.getUserProfileUpdated(),
                    res.getUserCreated(), res.getUserNotFound(), res.getDdUserFetched(), res.getWarnings().size());
            if (!res.getWarnings().isEmpty()) {
                log.warn("钉钉组织同步告警：{}", res.getWarnings());
            }
        } catch (Exception e) {
            log.error("钉钉组织同步失败", e);
        }
    }
}

