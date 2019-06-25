package com.tskj.session.biz;

import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.user.dao.UserInfo;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 13:40
 **/
public interface SessionDataBiz {

    /* HttpSession session = request.getSession();
     ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");
     classId = classInfo.getRealClassId();
     tableName = classInfo.getBoxTable();
     perFixDes = classInfo.getPerFixDes();*/
    UserInfo getUserInfo();

    ClassTreeInfo getClassTreeInfo();

    String getUid();

    String getFileName();

    String getClassId();

    String getClassType();

    String getIp();

    String getMethod();

    String getRequestUri();


}
