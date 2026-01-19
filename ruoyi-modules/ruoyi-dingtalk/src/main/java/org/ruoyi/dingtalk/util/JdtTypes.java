package org.ruoyi.dingtalk.util;

import com.alibaba.fastjson2.TypeReference;
import org.ruoyi.dingtalk.vo.PageResult;
import org.ruoyi.dingtalk.vo.Department;
import org.ruoyi.dingtalk.vo.User;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 用于JSON泛型转换，定义各种实际类型
 *
 * @author sunjianlei
 */
public class JdtTypes {

    public final static Type PageResult_User = new TypeReference<PageResult<User>>() {
    }.getType();

    public final static Type PageResult_Department = new TypeReference<PageResult<Department>>() {
    }.getType();

    public final static Type List_String = new TypeReference<List<String>>() {
    }.getType();

    public final static Type List_Department = new TypeReference<List<Department>>() {
    }.getType();

}
