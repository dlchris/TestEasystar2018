package com.tskj.user.userRightDAOImpl;

import com.tskj.core.db.DbUtility;
import com.tskj.user.userRightDAO.ContentPowerDao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 11:06
 **/
public class ContentPowerDaoImpl implements ContentPowerDao {
    @Override
    public List<Map<String, Object>> ContentAuthority(String USERID, String MODULEID, String ROLEID) {

        String sql = "SELECT * FROM USERMODULE  WHERE USERID = '" + USERID + "' AND MODULEID = '" + MODULEID + "' UNION ALL\n" +
                "SELECT * FROM ROLEMODULE  WHERE ROLEID = '" + ROLEID + "' AND MODULEID = '" + MODULEID + "'";

        //System.err.println("权限sql"+sql);

        return DbUtility.execSQL(sql);
    }

    public static void main(String[] args) {
        String a = "9+2+6+秘密+公开+绝密+国内+内部+机密+测试+";
        String b = "2+5+办公室+AAA+绝密+BBB+测试+";
        //String b = "";
        String[] array1 = a.split("\\+");
        String[] array2 = b.split("\\+");
        System.err.println(array1.toString());
        System.err.println(array2.toString());
        TreeSet<String> set = new TreeSet<>();
        //HashSet<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(array1));
        set.addAll(Arrays.asList(array2));
        String[] arr = set.toArray(new String[set.size()]);
        System.err.println(arr.toString());
    }
}
