package org.ruoyi.dingtalk.vo;

import lombok.Getter;

/**
 * 钉钉部门对象
 *
 * @author sunjianlei
 */

@Getter
public class Department {
    /**
     * 部门ID。
     */
    private Integer dept_id;
    /**
     * 部门名称。
     */
    private String name;
    /**
     * 父部门ID。
     */
    private Integer parent_id;
    /**
     * 部门标识字段。
     */
    private String source_identifier;
    /**
     * 是否同步创建一个关联此部门的企业群：
     */
    private Boolean create_dept_group;
    /**
     * 当部门群已经创建后，是否有新人加入部门会自动加入该群：
     */
    private Boolean auto_add_user;
    /**
     * 部门是否来自关联组织：
     */
    private Boolean from_union_org;
    /**
     * 教育部门标签：
     */
    private String tags;
    /**
     * 在父部门中的次序值。
     */
    private Integer order;
    /**
     * 部门群ID。
     */
    private String dept_group_chat_id;
    /**
     * 部门群是否包含子部门：
     */
    private Boolean group_contain_sub_dept;
    /**
     * 企业群群主ID。
     */
    private String org_dept_owner;
    /**
     * 部门的主管列表。
     */
    private String[] dept_manager_userid_list;
    /**
     * 是否限制本部门成员查看通讯录：
     */
    private Boolean outer_dept;
    /**
     * 当限制部门成员的通讯录查看范围时（即outer_dept为true时），配置的部门员工可见部门列表。
     */
    private Integer[] outer_permit_depts;
    /**
     * 本部门成员是否只能看到所在部门及下级部门通讯录：
     * <p>
     * true：只能看到所在部门及下级部门通讯录
     * <p>
     * false：不能查看所有通讯录，在通讯录中仅能看到自己
     * <p>
     * 当outer_dept为true时，此参数生效。
     */
    private Boolean outer_dept_only_self;
    /**
     * 当限制部门成员的通讯录查看范围时（即outer_dept为true时），配置的部门员工可见员工列表。
     */
    private String[] outer_permit_users;
    /**
     * 是否隐藏本部门：
     */
    private Boolean hide_dept;
    /**
     * 当隐藏本部门时（即hide_dept为true时），配置的允许在通讯录中查看本部门的员工列表。
     */
    private String[] user_permits;
    /**
     * 隐藏本部门时（即hide_dept为true时），配置的允许在通讯录中查看本部门的部门列表。
     */
    private Integer[] dept_permits;

    public Department setDept_id(Integer dept_id) {
        this.dept_id = dept_id;
        return this;
    }

    public Department setName(String name) {
        this.name = name;
        return this;
    }

    public Department setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
        return this;
    }

    public Department setSource_identifier(String source_identifier) {
        this.source_identifier = source_identifier;
        return this;
    }

    public Department setCreate_dept_group(Boolean create_dept_group) {
        this.create_dept_group = create_dept_group;
        return this;
    }

    public Department setAuto_add_user(Boolean auto_add_user) {
        this.auto_add_user = auto_add_user;
        return this;
    }

    public Department setFrom_union_org(Boolean from_union_org) {
        this.from_union_org = from_union_org;
        return this;
    }

    public Department setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public Department setOrder(Integer order) {
        this.order = order;
        return this;
    }

    public Department setDept_group_chat_id(String dept_group_chat_id) {
        this.dept_group_chat_id = dept_group_chat_id;
        return this;
    }

    public Department setGroup_contain_sub_dept(Boolean group_contain_sub_dept) {
        this.group_contain_sub_dept = group_contain_sub_dept;
        return this;
    }

    public Department setOrg_dept_owner(String org_dept_owner) {
        this.org_dept_owner = org_dept_owner;
        return this;
    }

    public Department setDept_manager_userid_list(String[] dept_manager_userid_list) {
        this.dept_manager_userid_list = dept_manager_userid_list;
        return this;
    }

    public Department setOuter_dept(Boolean outer_dept) {
        this.outer_dept = outer_dept;
        return this;
    }

    public Department setOuter_permit_depts(Integer[] outer_permit_depts) {
        this.outer_permit_depts = outer_permit_depts;
        return this;
    }

    public Department setOuter_permit_users(String[] outer_permit_users) {
        this.outer_permit_users = outer_permit_users;
        return this;
    }

    public Department setHide_dept(Boolean hide_dept) {
        this.hide_dept = hide_dept;
        return this;
    }

    public Department setUser_permits(String[] user_permits) {
        this.user_permits = user_permits;
        return this;
    }

    public Department setDept_permits(Integer[] dept_permits) {
        this.dept_permits = dept_permits;
        return this;
    }

    public Department setOuter_dept_only_self(Boolean outer_dept_only_self) {
        this.outer_dept_only_self = outer_dept_only_self;
        return this;
    }

}
