package org.ruoyi.dingtalk.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ruoyi.dingtalk.api.JdtBaseAPI;
import org.ruoyi.dingtalk.api.JdtDepartmentAPI;
import org.ruoyi.dingtalk.api.JdtUserAPI;
import org.ruoyi.dingtalk.api.response.Response;
import org.ruoyi.dingtalk.config.DingTalkProperties;
import org.ruoyi.dingtalk.vo.AccessToken;
import org.ruoyi.dingtalk.vo.Department;
import org.ruoyi.dingtalk.vo.GetUserListBody;
import org.ruoyi.dingtalk.vo.PageResult;
import org.ruoyi.dingtalk.vo.User;
import org.ruoyi.system.domain.SysDept;
import org.ruoyi.system.domain.SysUser;
import org.ruoyi.system.domain.bo.SysUserBo;
import org.ruoyi.system.mapper.SysDeptMapper;
import org.ruoyi.system.mapper.SysUserMapper;
import org.ruoyi.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 钉钉组织架构同步服务（部门 + 用户部门归属）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkOrgSyncService {

    private static final int DD_ROOT_DEPT_ID = 1;

    private final DingTalkProperties dingTalkProperties;
    private final JdtBaseAPI jdtBaseAPI;
    private final JdtDepartmentAPI jdtDepartmentAPI;
    private final JdtUserAPI jdtUserAPI;

    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final ISysUserService sysUserService;

    public SyncResult syncOrg() {
        DingTalkProperties.Sync syncCfg = dingTalkProperties.getSync();
        SyncResult result = new SyncResult();

        AccessToken accessToken = jdtBaseAPI.getAccessToken(dingTalkProperties.getAppKey(), dingTalkProperties.getAppSecret());
        if (accessToken == null || StrUtil.isBlank(accessToken.getAccessToken())) {
            throw new IllegalStateException("获取钉钉 AccessToken 失败");
        }

        // 1) 拉取钉钉部门（平铺）
        List<Department> ddDepts = jdtDepartmentAPI.listAll(accessToken.getAccessToken());
        if (CollUtil.isEmpty(ddDepts)) {
            result.getWarnings().add("钉钉部门列表为空，跳过同步");
            return result;
        }

        Map<Integer, Department> ddDeptMap = new HashMap<>();
        Map<Integer, List<Integer>> childrenMap = new HashMap<>();
        Set<Integer> ddDeptIds = new HashSet<>();
        for (Department d : ddDepts) {
            if (d == null || d.getDept_id() == null) {
                continue;
            }
            ddDeptIds.add(d.getDept_id());
            ddDeptMap.put(d.getDept_id(), d);
            Integer parentId = d.getParent_id();
            if (parentId != null) {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(d.getDept_id());
            }
        }

        // 2) 预加载本地已存在的“钉钉映射部门”
        Map<Integer, SysDept> existingByDdId = new HashMap<>();
        List<SysDept> existing = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .in(SysDept::getDingtalkDeptId, ddDeptIds));
        for (SysDept dept : existing) {
            if (dept.getDingtalkDeptId() != null) {
                existingByDdId.put(dept.getDingtalkDeptId(), dept);
            }
        }

        // 3) 校验本地挂载根部门
        Long mountRootDeptId = syncCfg.getRootDeptId();
        SysDept mountRoot = sysDeptMapper.selectById(mountRootDeptId);
        if (mountRoot == null) {
            throw new IllegalStateException("本地挂载根部门不存在，rootDeptId=" + mountRootDeptId);
        }

        // 4) BFS 同步部门（父先于子）
        Map<Integer, Long> ddToLocalDeptId = new HashMap<>();
        Deque<Integer> queue = new ArrayDeque<>();

        if (!ddDeptMap.containsKey(DD_ROOT_DEPT_ID)) {
            result.getWarnings().add("钉钉根部门(1)不存在，无法构建树；将按 parent_id 关系尽力同步");
            // 尝试从所有顶层节点开始
            for (Integer id : ddDeptIds) {
                Department d = ddDeptMap.get(id);
                if (d != null && (d.getParent_id() == null || d.getParent_id() == 0)) {
                    queue.add(id);
                }
            }
        } else {
            queue.add(DD_ROOT_DEPT_ID);
        }

        while (!queue.isEmpty()) {
            Integer ddDeptId = queue.removeFirst();
            Department ddDept = ddDeptMap.get(ddDeptId);
            if (ddDept == null) {
                continue;
            }

            Long localParentId;
            if (Objects.equals(ddDeptId, DD_ROOT_DEPT_ID)) {
                localParentId = mountRootDeptId;
            } else {
                Integer ddParentId = ddDept.getParent_id();
                Long mappedParent = ddParentId == null ? null : ddToLocalDeptId.get(ddParentId);
                localParentId = mappedParent != null ? mappedParent : mountRootDeptId;
                if (mappedParent == null && ddParentId != null && !Objects.equals(ddParentId, DD_ROOT_DEPT_ID)) {
                    result.getWarnings().add("部门父节点未先同步，ddDeptId=" + ddDeptId + ", ddParentId=" + ddParentId + "，已挂载到 rootDeptId=" + mountRootDeptId);
                }
            }

            SysDept parentDept = sysDeptMapper.selectById(localParentId);
            String ancestors = parentDept == null
                    ? "0"
                    : (StrUtil.blankToDefault(parentDept.getAncestors(), "0") + "," + parentDept.getDeptId());

            SysDept existDept = existingByDdId.get(ddDeptId);
            if (existDept == null) {
                SysDept insert = new SysDept();
                insert.setTenantId("000000");
                insert.setParentId(localParentId);
                insert.setAncestors(ancestors);
                insert.setDeptName(StrUtil.blankToDefault(ddDept.getName(), "钉钉部门" + ddDeptId));
                insert.setOrderNum(ddDept.getOrder() == null ? 0 : ddDept.getOrder());
                insert.setStatus("0");
                insert.setDelFlag("0");
                insert.setDingtalkDeptId(ddDeptId);
                insert.setDingtalkParentDeptId(ddDept.getParent_id());
                sysDeptMapper.insert(insert);
                ddToLocalDeptId.put(ddDeptId, insert.getDeptId());
                existingByDdId.put(ddDeptId, insert);
                result.deptCreated++;
            } else {
                SysDept update = new SysDept();
                update.setDeptId(existDept.getDeptId());
                update.setParentId(localParentId);
                update.setAncestors(ancestors);
                update.setDeptName(StrUtil.blankToDefault(ddDept.getName(), existDept.getDeptName()));
                update.setOrderNum(ddDept.getOrder() == null ? 0 : ddDept.getOrder());
                update.setStatus("0");
                update.setDelFlag("0");
                update.setDingtalkDeptId(ddDeptId);
                update.setDingtalkParentDeptId(ddDept.getParent_id());
                sysDeptMapper.updateById(update);
                ddToLocalDeptId.put(ddDeptId, existDept.getDeptId());
                result.deptUpdated++;
            }

            List<Integer> children = childrenMap.get(ddDeptId);
            if (CollUtil.isNotEmpty(children)) {
                for (Integer child : children) {
                    queue.addLast(child);
                }
            }
        }

        // 5) 同步用户部门归属
        if (syncCfg.isSyncUserDept()) {
            Map<String, User> unionUserMap = collectAllUsersByDept(ddDeptIds, accessToken.getAccessToken(), result);
            for (User ddUser : unionUserMap.values()) {
                String unionId = ddUser.getUnionid();
                if (StrUtil.isBlank(unionId)) {
                    continue;
                }

                Long targetDeptId = choosePrimaryDept(ddUser, ddToLocalDeptId, mountRootDeptId);
                SysUser localUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUnionId, unionId)
                        .last("limit 1"));

                if (localUser == null && StrUtil.isNotBlank(ddUser.getMobile())) {
                    localUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhonenumber, ddUser.getMobile())
                            .last("limit 1"));
                }

                if (localUser == null) {
                    if (syncCfg.isCreateMissingUsers()) {
                        SysUserBo bo = new SysUserBo();
                        bo.setUserName(genUserName(unionId));
                        bo.setNickName(StrUtil.blankToDefault(ddUser.getName(), bo.getUserName()));
                        bo.setPassword(BCrypt.hashpw("123456"));
                        bo.setUnionId(unionId);
                        bo.setPhonenumber(StrUtil.blankToDefault(ddUser.getMobile(), ""));
                        bo.setEmail(StrUtil.blankToDefault(ddUser.getEmail(), ""));
                        bo.setAvatar(ddUser.getAvatar());
                        bo.setDeptId(targetDeptId);
                        // 与现有钉钉登录服务保持一致（当前项目 tenant.enable 默认 false）
                        SysUser created = sysUserService.registerUser(bo, "0");
                        result.userCreated++;
                        // 同步到内存继续后续更新
                        localUser = created;
                    } else {
                        result.userNotFound++;
                        continue;
                    }
                }

                boolean needUpdateDept = targetDeptId != null && !Objects.equals(localUser.getDeptId(), targetDeptId);
                boolean needUpdateProfile = syncCfg.isUpdateUserProfile();
                if (!needUpdateDept && !needUpdateProfile) {
                    continue;
                }

                SysUser update = new SysUser();
                update.setUserId(localUser.getUserId());
                if (needUpdateDept) {
                    update.setDeptId(targetDeptId);
                    result.userDeptUpdated++;
                }
                if (needUpdateProfile) {
                    update.setNickName(StrUtil.blankToDefault(ddUser.getName(), localUser.getNickName()));
                    if (StrUtil.isNotBlank(ddUser.getMobile())) {
                        update.setPhonenumber(ddUser.getMobile());
                    }
                    if (StrUtil.isNotBlank(ddUser.getEmail())) {
                        update.setEmail(ddUser.getEmail());
                    }
                    if (StrUtil.isNotBlank(ddUser.getAvatar())) {
                        update.setAvatar(ddUser.getAvatar());
                    }
                    result.userProfileUpdated++;
                }
                // 保底写入 unionId（手机号匹配场景）
                if (StrUtil.isBlank(localUser.getUnionId())) {
                    update.setUnionId(unionId);
                }
                sysUserMapper.updateById(update);
            }
        }

        return result;
    }

    private Map<String, User> collectAllUsersByDept(Set<Integer> ddDeptIds, String accessToken, SyncResult result) {
        Map<String, User> unionUserMap = new HashMap<>();
        for (Integer deptId : ddDeptIds) {
            // 根部门用户通常包含大量重复，仍然保留一次拉取即可（后续用 unionId 去重）
            getUserListByDeptIdRecursion(deptId, 0, accessToken, unionUserMap, result);
        }
        result.ddUserFetched = unionUserMap.size();
        return unionUserMap;
    }

    private void getUserListByDeptIdRecursion(int deptId, int cursor, String accessToken, Map<String, User> unionUserMap, SyncResult result) {
        GetUserListBody body = new GetUserListBody(deptId, cursor, 100);
        Response<PageResult<User>> response = jdtUserAPI.getUserListByDeptId(body, accessToken);
        if (!response.isSuccess() || response.getResult() == null) {
            result.getWarnings().add("获取部门用户失败 deptId=" + deptId + ", err=" + response.getErrmsg());
            return;
        }
        PageResult<User> page = response.getResult();
        if (CollUtil.isNotEmpty(page.getList())) {
            for (User u : page.getList()) {
                if (u != null && StrUtil.isNotBlank(u.getUnionid())) {
                    unionUserMap.putIfAbsent(u.getUnionid(), u);
                }
            }
        }
        if (Boolean.TRUE.equals(page.getHas_more())) {
            getUserListByDeptIdRecursion(deptId, page.getNext_cursor(), accessToken, unionUserMap, result);
        }
    }

    private Long choosePrimaryDept(User ddUser, Map<Integer, Long> ddToLocalDeptId, Long mountRootDeptId) {
        Integer[] deptIds = ddUser.getDept_id_listArray();
        if (deptIds != null) {
            for (Integer ddDeptId : deptIds) {
                if (ddDeptId == null || Objects.equals(ddDeptId, DD_ROOT_DEPT_ID)) {
                    continue;
                }
                Long mapped = ddToLocalDeptId.get(ddDeptId);
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        // fallback：钉钉根部门映射（若存在），否则挂载根部门
        Long rootMapped = ddToLocalDeptId.get(DD_ROOT_DEPT_ID);
        return rootMapped != null ? rootMapped : mountRootDeptId;
    }

    private String genUserName(String unionId) {
        String cleaned = unionId.replaceAll("[^a-zA-Z0-9]", "");
        if (cleaned.length() > 20) {
            cleaned = cleaned.substring(cleaned.length() - 20);
        }
        return "dd_" + cleaned;
    }

    @Data
    public static class SyncResult {
        private int deptCreated;
        private int deptUpdated;
        private int userDeptUpdated;
        private int userProfileUpdated;
        private int userCreated;
        private int userNotFound;
        private int ddUserFetched;
        private List<String> warnings = new ArrayList<>();
    }
}

