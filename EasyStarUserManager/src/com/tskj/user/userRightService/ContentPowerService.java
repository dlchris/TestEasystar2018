package com.tskj.user.userRightService;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 11:11
 **/
public interface ContentPowerService {

    /**
     * @Author JRX
     * @Description: 
     * @create 2019/4/19 9:51
 * @param USERID  用戶ID
 * @param MODULEID classId
 * @param ROLEID  角色ID
 * @param classType 判斷是盒级，还是 文件级案卷级
     * @return 
    **/
    //查看条目内容权限信息
    String ContentAuthority(String USERID, String MODULEID, String ROLEID,String classType);
    // 盒级条目 不带SECURITY权限
    String BoxContentAuthority(String USERID, String MODULEID, String ROLEID);

    //只显示权限内的字典项
    JSONObject FindPowerDict(String USERID, String MODULEID, String ROLEID, JSONObject jsonObject);
}
