package com.tskj.fileimport.process;

import java.util.Hashtable;

/**
 * 上传数据源管理器
 * @author LeonSu
 */
public class DataManager {
    private Hashtable<String, DataProcess> list;

    private DataManager() {
        list = new Hashtable<>(100);
    }

    private static DataManager dataManager = null;

    public static DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    public Hashtable<String, DataProcess> getList() {
        return list;
    }

    public void add(DataProcess value) {
        list.put(value.getUid(), value);
    }

    public void remove(DataProcess value) {
        for (String key : list.keySet()) {
            if (list.get(key).equals(value)) {
                list.remove(key);
                break;
            }
        }
    }

    public int size() {
        return list.size();
    }

    public DataProcess get(String uid) {
        return list.get(uid);
    }
}
