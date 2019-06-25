package com.tskj.user.userRightDAO;

import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-22 16:38
 **/
public interface ContentPowerDao {
    //查看条目内容权限信息
    List<Map<String , Object>> ContentAuthority(String USERID, String MODULEID, String ROLEID);

}
