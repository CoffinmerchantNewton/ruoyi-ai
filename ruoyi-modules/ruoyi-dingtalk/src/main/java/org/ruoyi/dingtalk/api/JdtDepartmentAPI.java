package org.ruoyi.dingtalk.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.ruoyi.dingtalk.api.response.Response;
import org.ruoyi.dingtalk.util.HttpUtil;
import org.ruoyi.dingtalk.util.JdtTypes;
import org.ruoyi.dingtalk.vo.Department;
import org.ruoyi.dingtalk.vo.DeptParentResponse;
import org.ruoyi.dingtalk.util.ApiUrls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 钉钉部门接口
 *
 * @author sunjianlei
 */
@Component
@Slf4j
public class JdtDepartmentAPI {

    @Autowired
    private ApiUrls apiUrls;

    /**
     * 创建部门
     * <br>
     * developers.dingtalk.com/document/app/create-a-department-v2
     *
     * @param department  部门实例
     * @param accessToken 有效的access_token
     * @return Response&lt;String&gt 成功返回dept_id
     */
    public Response<Integer> create(Department department, String accessToken) {
        String url = apiUrls.getDepartCreateUrl(accessToken);
        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(department));
        Response<Integer> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            // 将常用的dept_id直接返回（实际上也就只有这一个返回参数）
            Integer dept_id = originResponse.getResult().getInteger("dept_id");
            response.setResult(dept_id);
        }
        return response;
    }

    /**
     * 更新部门信息
     * <br>
     * developers.dingtalk.com/document/app/update-a-department-v2
     *
     * @param department  部门实例
     * @param accessToken 有效的access_token
     * @return Response&lt;JSONObject&gt
     */
    public Response<JSONObject> update(Department department, String accessToken) {
        String url = apiUrls.getDepartUpdateUrl(accessToken);
        Response<JSONObject> response = HttpUtil.post(url, JSON.toJSONString(department));
        return response;
    }

    /**
     * 根据部门ID删除指定部门
     * <br>
     * developers.dingtalk.com/document/app/delete-a-department-v2
     *
     * @param dept_id     部门id
     * @param accessToken 有效的access_token
     * @return Response&lt;JSONObject&gt
     */
    public Response<JSONObject> delete(int dept_id, String accessToken) {
        String url = apiUrls.getDepartDeleteUrl(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", dept_id);
        Response<JSONObject> response = HttpUtil.post(url, body.toJSONString());
        return response;
    }

    /**
     * 伪批量删除部门（for循环调接口）
     *
     * @param deptIds     部门ID列表
     * @param accessToken 有效的access_token
     * @return List&lt;Response&lt;JSONObject&gt&gt
     */
    public List<Response<JSONObject>> batchDeletePseudo(Collection<Integer> deptIds, String accessToken) {
        List<Response<JSONObject>> list = new ArrayList<>();
        for (Integer deptId : deptIds) {
            list.add(this.delete(deptId, accessToken));
        }
        return list;
    }

    /**
     * 获取根部门下的子部门列表（不包含子部门的子部门）
     * <br>
     * developers.dingtalk.com/document/app/obtain-the-department-list-v2
     *
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;Department&gt;&gt;
     */
    public Response<List<Department>> listByRoot(String accessToken) {
        String url = apiUrls.getDepartListSubUrl(accessToken);
        Response<List<Department>> response = HttpUtil.post(url, "", JdtTypes.List_Department);
        return response;
    }

    /**
     * 根据父ID获取子部门列表（不包含子部门的子部门）
     * <br>
     * developers.dingtalk.com/document/app/obtain-the-department-list-v2
     *
     * @param dept_id     父部门ID
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;Department&gt;&gt;
     */
    public Response<List<Department>> listByParentId(int dept_id, String accessToken) {
        String url = apiUrls.getDepartListSubUrl(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", dept_id);
        Response<List<Department>> response = HttpUtil.post(url, body.toJSONString(), JdtTypes.List_Department);
        return response;
    }

    /**
     * 根据dept_id获取部门详情
     * <br>
     * developers.dingtalk.com/document/app/query-department-details0-v2
     *
     * @param dept_id     部门id
     * @param accessToken 有效的access_token
     * @return Response&lt;Department&gt;
     */
    public Response<Department> getDepartmentById(int dept_id, String accessToken) {
        String url = apiUrls.getDepartGetUrl(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", dept_id);
        Response<Department> response = HttpUtil.post(url, body.toJSONString(), Department.class);

        return response;
    }

    /**
     * 获取子部门ID列表
     * <br>
     * developers.dingtalk.com/document/app/obtain-a-sub-department-id-list-v2
     *
     * @param dept_id     部门id
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;Integer&gt;&gt; 成功返回子部门id列表
     */
    public Response<List<Integer>> getListSubId(int dept_id, String accessToken) {
        String url = apiUrls.getDepartListSubId(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", dept_id);
        Response<JSONObject> originResponse = HttpUtil.post(url, body.toJSONString());
        Response<List<Integer>> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            List<Integer> dept_id_list = originResponse.getResult().getJSONArray("dept_id_list").toJavaList(Integer.class);
            response.setResult(dept_id_list);
        }

        return response;
    }

    /**
     * 获取指定用户的所有父部门列表
     * <br>
     * developers.dingtalk.com/document/app/queries-the-list-of-all-parent-departments-of-a-user
     *
     * @param userid      用户id
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;DeptParentResponse&gt;&gt; 成功返回父部门id列表
     */
    public Response<List<DeptParentResponse>> getListParentByUser(String userid, String accessToken) {
        String url = apiUrls.getDepartGetListParentByUser(accessToken);
        JSONObject body = new JSONObject();
        body.put("userid", userid);
        Response<JSONObject> originResponse = HttpUtil.post(url, body.toJSONString());
        Response<List<DeptParentResponse>> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            List<DeptParentResponse> parent_list = originResponse.getResult().getJSONArray("parent_list").toJavaList(DeptParentResponse.class);
            response.setResult(parent_list);
        }

        return response;
    }

    /**
     * 获取指定部门的所有父部门列表
     * <br>
     * developers.dingtalk.com/document/app/query-the-list-of-all-parent-departments-of-a-department
     *
     * @param dept_id     部门id
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;Integer&gt;&gt; 成功返回父部门id列表
     */
    public Response<List<Integer>> getListParentByDept(String dept_id, String accessToken) {
        String url = apiUrls.getDepartListParentByDept(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", dept_id);
        Response<JSONObject> originResponse = HttpUtil.post(url, body.toJSONString());
        Response<List<Integer>> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            List<Integer> parent_id_list = originResponse.getResult().getJSONArray("parent_id_list").toJavaList(Integer.class);
            response.setResult(parent_id_list);
        }

        return response;
    }

    /**
     * 非官方API接口：获取钉钉的所有部门，平铺返回，不关心失败情况
     *
     * @param accessToken 有效的access_token
     * @return List&lt;Department&gt;
     */
    public List<Department> listAll(String accessToken) {
        List<Department> all = new ArrayList<>();
        int topDepId = 1;
        // 先查出来顶级的部门
        Response<Department> response = this.getDepartmentById(topDepId, accessToken);
        if (response.isSuccess()) {
            all.add(response.getResult());
            this.listAllGetChildren(topDepId, accessToken, all);
        }
        return all;
    }

    /**
     * 非官方API接口：获取钉钉的所有部门，平铺返回，返回失败结果
     *
     * @param accessToken 有效的access_token
     * @return List&lt;Response&lt;Department&gt;&gt;
     */
    public List<Response<Department>> listAllResponse(String accessToken) {
        List<Response<Department>> res = new ArrayList<>();
        List<Department> all = new ArrayList<>();
        int topDepId = 1;
        // 先查出来顶级的部门
        Response<Department> response = this.getDepartmentById(topDepId, accessToken);
        res.add(response);
        if (response.isSuccess()) {
            all.add(response.getResult());
            this.listAllGetChildren(topDepId, accessToken, all, res);
        }
        return res;
    }

    private void listAllGetChildren(int parentDepId, String accessToken, List<Department> all) {
        this.listAllGetChildren(parentDepId, accessToken, all, null);
    }

    private void listAllGetChildren(int parentDepId, String accessToken, List<Department> all, List<Response<Department>> res) {
        Response<List<Department>> response = this.listByParentId(parentDepId, accessToken);
        if (response.isSuccess()) {
            List<Department> departments = (List<Department>) response.getResult();
            if (!departments.isEmpty()) {
                all.addAll(departments);

                for (Department department : departments) {
                    if (res != null) {
                        res.add(new Response<>(response, department));
                    }
                    this.listAllGetChildren(department.getDept_id(), accessToken, all, res);
                }
            }
        } else if (res != null) {
            res.add(new Response<>(response));
        }
    }

}
