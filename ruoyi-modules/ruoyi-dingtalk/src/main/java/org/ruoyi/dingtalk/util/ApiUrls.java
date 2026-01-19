package org.ruoyi.dingtalk.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 钉钉Api接口地址
 *
 * @author sunjianlei
 */
@Component
public class ApiUrls {

    @Value("${dingtalk.oapi-url:https://oapi.dingtalk.com}")
    private String oapiUrl;

    @Value("${dingtalk.api-url:https://oapi.dingtalk.com}")
    private String apiUrl;



    public String getAccessTokenUrl(String appkey, String appsecret) {
        return String.format("%s/gettoken?appkey=%s&appsecret=%s", apiUrl, appkey, appsecret);
    }


    public String getUserCreateUrl(String accessToken) {
        return String.format("%s/topapi/v2/user/create?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserUpdateUrl(String accessToken) {
        return String.format("%s/topapi/v2/user/update?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserDeleteUrl(String accessToken) {
        return String.format("%s/topapi/v2/user/delete?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserGetUrl(String accessToken) {
        return String.format("%s/topapi/v2/user/get?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserListUrl(String accessToken) {
        return String.format("%s/topapi/v2/user/list?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserListSimple(String accessToken) {
        return String.format("%s/topapi/user/listsimple?access_token=%s", oapiUrl, accessToken);
    }

    public String getUserListId(String accessToken) {
        return String.format("%s/topapi/user/listid?access_token=%s", oapiUrl, accessToken);
    }

    public String getUserCount(String accessToken) {
        return String.format("%s/topapi/user/count?access_token=%s", oapiUrl, accessToken);
    }

    public String getUserGetIdByMobile(String accessToken) {
        return String.format("%s/topapi/v2/user/getbymobile?access_token=%s", oapiUrl, accessToken);
    }


    public String getUserGetIdByUnionid(String accessToken) {
        return String.format("%s/topapi/user/getbyunionid?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartCreateUrl(String accessToken) {
        return String.format("%s/topapi/v2/department/create?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartUpdateUrl(String accessToken) {
        return String.format("%s/topapi/v2/department/update?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartDeleteUrl(String accessToken) {
        return String.format("%s/topapi/v2/department/delete?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartListSubUrl(String accessToken) {
        return String.format("%s/topapi/v2/department/listsub?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartGetUrl(String accessToken) {
        return String.format("%s/topapi/v2/department/get?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartListSubId(String accessToken) {
        return String.format("%s/topapi/v2/department/listsubid?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartGetListParentByUser(String accessToken) {
        return String.format("%s/topapi/v2/department/listparentbyuser?access_token=%s", oapiUrl, accessToken);
    }

    public String getDepartListParentByDept(String accessToken) {
        return String.format("%s/topapi/v2/department/listparentbydept?access_token=%s", oapiUrl, accessToken);
    }


    public String getMsgAsyncSendUrl(String accessToken) {
        return String.format("%s/topapi/message/corpconversation/asyncsend_v2?access_token=%s", oapiUrl, accessToken);
    }


    public String getMsgRecallUrl(String accessToken) {
        return String.format("%s/topapi/message/corpconversation/recall?access_token=%s", oapiUrl, accessToken);
    }





}
