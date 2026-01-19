package org.ruoyi.dingtalk.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.ruoyi.dingtalk.api.response.Response;
import org.ruoyi.dingtalk.util.HttpUtil;
import org.ruoyi.dingtalk.util.JdtTypes;
import org.ruoyi.dingtalk.vo.PageResult;
import org.ruoyi.dingtalk.vo.GetUserListBody;
import org.ruoyi.dingtalk.vo.User;
import org.ruoyi.dingtalk.util.ApiUrls;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 钉钉用户接口
 *
 * @author sunjianlei
 */
@Component
@Slf4j
public class JdtUserAPI {

    @Autowired
    private ApiUrls apiUrls;

    /**
     * 创建用户
     * <br>
     * //developers.dingtalk.com/document/app/user-information-creation
     *
     * @param user        用户实例
     * @param accessToken 有效的access_token
     * @return Response&lt;String&gt 成功返回userid
     */
    public Response<String> create(User user, String accessToken) {
        String url = apiUrls.getUserCreateUrl(accessToken);
        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(user));
        Response<String> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            // 将常用的userId直接返回（实际上也就只有这一个返回参数）
            String userid = originResponse.getResult().getString("userid");
            response.setResult(userid);
        }
        return response;
    }

    /**
     * 更新用户信息
     * <br>
     * //developers.dingtalk.com/document/app/user-information-update
     *
     * @param user        用户实例
     * @param accessToken 有效的access_token
     * @return Response&lt;JSONObject&gt
     */
    public Response<JSONObject> update(User user, String accessToken) {
        String url = apiUrls.getUserUpdateUrl(accessToken);
        Response<JSONObject> response = HttpUtil.post(url, JSON.toJSONString(user));
        return response;
    }

    /**
     * 删除用户
     * <br>
     * //developers.dingtalk.com/document/app/delete-a-user
     *
     * @param userid      用户唯一标识userid
     * @param accessToken 有效的access_token
     * @return Response&lt;JSONObject&gt
     */
    public Response<JSONObject> delete(String userid, String accessToken) {
        String url = apiUrls.getUserDeleteUrl(accessToken);
        JSONObject body = new JSONObject();
        body.put("userid", userid);
        Response<JSONObject> response = HttpUtil.post(url, body.toJSONString());
        return response;
    }

    /**
     * 伪批量删除用户（for循环调接口）
     *
     * @param userIds     用户ID列表
     * @param accessToken 有效的access_token
     * @return List&lt;Response&lt;JSONObject&gt&gt
     */
    public List<Response<JSONObject>> batchDeletePseudo(Collection<String> userIds, String accessToken) {
        List<Response<JSONObject>> list = new ArrayList<>();
        int result = 0;
        for (String userId : userIds) {
            list.add(this.delete(userId, accessToken));
        }
        return list;
    }

    /**
     * 根据userid获取用户详情
     * <br>
     * developers.dingtalk.com/document/app/query-user-details
     *
     * @param userid      用户唯一标识userid
     * @param accessToken 有效的access_token
     * @return Response&lt;User&gt
     */
    public Response<User> getUserById(String userid, String accessToken) {
        String url = apiUrls.getUserGetUrl(accessToken);
        JSONObject body = new JSONObject();
        body.put("userid", userid);
        Response<User> response = HttpUtil.post(url, body.toJSONString(), User.class);
        return response;
    }

    /**
     * 获取部门用户详情。说明：只获取当前部门下的员工信息，不包含子部门内的员工。
     * <br>
     * //developers.dingtalk.com/document/app/queries-the-complete-information-of-a-department-user
     *
     * @param body        GetUserListBody实例
     * @param accessToken 有效的access_token
     * @return Response&lt;PageResult&lt;User&gt&gt; 分页对象
     */
    public Response<PageResult<User>> getUserListByDeptId(GetUserListBody body, String accessToken) {
        String url = apiUrls.getUserListUrl(accessToken);
        Response<PageResult<User>> response = HttpUtil.post(url, JSON.toJSONString(body), JdtTypes.PageResult_User);
        return response;
    }

    /**
     * 获取部门用户基础信息。<br>
     * 调用本接口获取指定部门的用户userid和name。<br>
     * 说明：只获取当前部门下的员工信息，不包含子部门内的员工。
     * <br>
     * developers.dingtalk.com/document/app/queries-the-complete-information-of-a-department-user
     *
     * @param body        GetUserListBody实例
     * @param accessToken 有效的access_token
     * @return Response&lt;PageResult&lt;User&gt&gt; 分页对象
     */
    public Response<PageResult<User>> getUserListSimpleByDeptId(GetUserListBody body, String accessToken) {
        String url = apiUrls.getUserListSimple(accessToken);
        Response<PageResult<User>> response = HttpUtil.post(url, JSON.toJSONString(body), JdtTypes.PageResult_User);
        return response;
    }

    /**
     * 获取部门用户userid列表。
     * <br>
     * 注意：目前暂不支持一次性获取企业下所有员工userid值，如果开发者希望获取企业下所有员工userid值，可以通过以下方法：<br>
     * 1. 调用“获取部门列表”接口，通过逐级遍历，获取该企业下所有部门ID。<br>
     * 2. 调用本接口，分别获取每个部门下的员工userid。<br>
     * <br>
     * developers.dingtalk.com/document/app/query-the-list-of-department-userids
     *
     * @param deptId      deptId
     * @param accessToken 有效的access_token
     * @return Response&lt;List&lt;String&gt;&gt;
     */
    public Response<List<String>> getUserListIdByDeptId(int deptId, String accessToken) {
        String url = apiUrls.getUserListId(accessToken);
        JSONObject body = new JSONObject();
        body.put("dept_id", deptId);
        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(body));
        Response<List<String>> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            List<String> userid_list = originResponse.getResult().getJSONArray("userid_list").toJavaList(String.class);
            response.setResult(userid_list);
        }
        return response;
    }

    /**
     * 获取员工人数
     * <br>
     * 调用本接口获取员工人数。
     * <br>
     * //developers.dingtalk.com/document/app/obtain-the-number-of-employees-v2
     *
     * @param onlyActive  是否包含未激活钉钉人数。
     *                    false：包含未激活钉钉的人员数量。
     *                    true：只包含激活钉钉的人员数量。
     * @param accessToken 有效的access_token
     * @return Response&lt;Integer&gt;
     */
    public Response<Integer> getUserCount(boolean onlyActive, String accessToken) {
        String url = apiUrls.getUserCount( accessToken);
        JSONObject body = new JSONObject();
        body.put("only_active", onlyActive);

        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(body));
        Response<Integer> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            response.setResult(originResponse.getResult().getIntValue("count"));
        }
        return response;
    }

    /**
     * 获取员工人数，包含未激活钉钉的人员数量
     *
     * @return Response&lt;Integer&gt;
     */
    public Response<Integer> getUserCount(String accessToken) {
        return this.getUserCount(false, accessToken);
    }

    /**
     * 根据手机号获取userid
     * <br>
     * //developers.dingtalk.com/document/app/query-users-by-phone-number
     *
     * @param mobile      用户的手机号。
     * @param accessToken 有效的access_token
     * @return Response&lt;String&gt;
     */
    public Response<String> getUseridByMobile(String mobile, String accessToken) {
        String url = apiUrls.getUserGetIdByMobile(accessToken);
        JSONObject body = new JSONObject();
        body.put("mobile", mobile);
        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(body));
        Response<String> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            // 将常用的userId直接返回（实际上也就只有这一个返回参数）
            String userid = originResponse.getResult().getString("userid");
            response.setResult(userid);
        }

        return response;
    }

    /**
     * 根据unionid获取用户userid
     * <br>
     * //developers.dingtalk.com/document/app/query-a-user-by-the-union-id
     *
     * @param unionid     员工在当前开发者企业账号范围内的唯一标识，系统生成，不会改变。
     * @param accessToken 有效的access_token
     * @return Response&lt;String&gt;
     */
    public Response<String> getUseridByUnionid(String unionid, String accessToken) {
        String url = apiUrls.getUserGetIdByUnionid(accessToken);
        JSONObject body = new JSONObject();
        body.put("unionid", unionid);
        Response<JSONObject> originResponse = HttpUtil.post(url, JSON.toJSONString(body));
        Response<String> response = new Response<>(originResponse);
        if (response.isSuccess()) {
            // 将常用的userId直接返回（实际上也就只有这一个返回参数）
            String userid = originResponse.getResult().getString("userid");
            response.setResult(userid);
        }
        return response;
    }

}
