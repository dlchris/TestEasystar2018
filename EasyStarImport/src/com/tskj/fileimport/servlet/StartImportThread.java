package com.tskj.fileimport.servlet;

import com.alibaba.fastjson.JSONObject;
import com.tskj.fileimport.process.DataManager;
import com.tskj.fileimport.process.DataProcess;
import com.tskj.fileimport.system.CFilesImport;
import com.tskj.fileimport.system.Tools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-19 11:05
 **/
public class StartImportThread extends Thread {
    private CFilesImport CFilesImport;
    private Object uid;
    private JSONObject keyFields;
    private int mode;

    public StartImportThread(int mode, CFilesImport CFilesImport, Object uid, JSONObject keyFields) {
        this.mode = mode;
        this.CFilesImport = CFilesImport;
        this.uid = uid;
        this.keyFields = keyFields;
    }

    @Override
    public void run() {

        long startTime = System.currentTimeMillis();
        System.err.println("开始线程");

        //excal中空行之前的数据条数
        int xlsCount = CFilesImport.getNotEmptyRowCount();
        DataProcess dataProcess = new DataProcess(uid.toString(), xlsCount);
        DataManager.getInstance().add(dataProcess);
        //初始化就判断档号生成项是否完整,不完整直接会抛异常出来
        try {
            CFilesImport.startTrans();
            //临时中间表创建
            CFilesImport.cTmpTable();
            //临时条目表创建
            CFilesImport.cTmpDataTable(null);
            //临时条目表sql初始化(//写临时表拼接语句初始化)
            CFilesImport.tempTableSqlInIt();
        } catch (Exception e) {
            e.printStackTrace();
            dataProcess.errMsg(1, "导入失败临时表创建失败！！！");
            //删除临时表
            CFilesImport.dropTempTable();
            return;
        }


        boolean canCommit = true;
        String ret = "{\"result\":0}";
        int i;
        System.err.println("多少条数据：" + xlsCount);

        try {
            for (i = 1; i <= xlsCount; i++) {
                JSONObject ret1 = CFilesImport.save(keyFields, null, i, xlsCount);
                if (ret1.getInteger("result") != 0) {
                    //canCommit = false;// 回滚如果加了一个保存不上都保存不上 真需要这样吗
                    dataProcess.addFailure(i, ret1.getString("errMsg"));

                } else {
                    dataProcess.addSuccess();
                }
            }
            //System.err.println(CFilesImport.getDocNoList());
            System.err.println("导入临时表失败:" + dataProcess.getFailure() + "条~~~~~~成功导入临时表:" + dataProcess.getSuccess() + "条");
            if (dataProcess.getSuccess() <= 0) {
                dataProcess.errMsg(1, "导入条目失败,没有可导入的条目");
                return;
            }
            //查对应临时表存的数据为
            //CFilesImport.findTempList();
            //保存成功后进行数据清洗查重后转存入目的表
            List<Map<String, Object>> noOperation = new ArrayList<>();
            String str = "";
            if (mode == 0) {//0.跳过
                str = "重复的条目已跳过未予导入";
                noOperation = CFilesImport.repeatSkipFields();
            } else if (mode == 1) {
                str = "EXCAL中存在多条重复该条未予导入";
                noOperation = CFilesImport.repeatCoverFields();
            }
            System.err.println("没有录入的条目(无操作):" + noOperation.size());

            JSONObject jsonObject = CFilesImport.saveToDB(mode);
            System.err.println(jsonObject);
            if (jsonObject.getInteger("result") != 0) {
                canCommit = false;
                dataProcess.errMsg(1, "清洗查重数据失败条目导入失败");
                return;
            }
            for (Map<String, Object> map : noOperation) {
                dataProcess.addFailurelist(map.get("ROWINDEX"), str);
            }
            System.err.println("最终未导入:" + dataProcess.getFailure() + "条~~~~~~成功导入:" + dataProcess.getSuccess() + "条");
            /*//删除临时表
            CFilesImport.dropTempTable();*/
            dataProcess.setFinished(true);
            System.err.println("导入成功!!!结束。。。");
            //jsonReturn = JSONObject.parseObject(ret);
        } catch (Exception e) {
            e.printStackTrace();
            /*//删除临时表
            CFilesImport.dropTempTable();*/
            dataProcess.errMsg(1, "导入失败");
            return;
        } finally {
            //删除临时表
            CFilesImport.dropTempTable();
            if (CFilesImport != null) {
                CFilesImport.endTrans(canCommit);
                CFilesImport.delFile();
            }
            long endTime = System.currentTimeMillis();
            endTime = endTime;
            System.err.println("上传条目耗时测试:" + (endTime - startTime) + "ms");
            System.err.println("上传条目耗时测试:" + (endTime - startTime) / 1000 + "s");
        }
    }
}
