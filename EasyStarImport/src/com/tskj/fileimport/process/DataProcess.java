package com.tskj.fileimport.process;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class DataProcess {

    public DataProcess(String uid, int count) {
        this.uid = uid;
        this.count = count;
        finished = false;
        failureList = JSONArray.parseArray("[]");
    }

    private int result = 0;
    private String errMsg;
    private long finishTime;

    public long getFinishTime() {
        return finishTime;
    }

    public String getUid() {
        return uid;
    }

    private String uid;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
        if (finished) {
            finishTime = System.currentTimeMillis();
        }
    }

    private int count;

    public int getCount() {
        return count;
    }

    private boolean finished;

    public int getSuccess() {
        return success;
    }

    public void addSuccess() {
        this.success++;
    }

    private int success = 0;

    public int getFailure() {
        return failure;
    }

    public void addFailure(@NotNull Object key, @Nullable Object value) {
        this.failure++;

        JSONObject tmpJson = JSONObject.parseObject("{}");
        tmpJson.put("keyField", key);//key第几条
        tmpJson.put("msg", value);//错误原因
        failureList.add(tmpJson);
    }

    private int failure = 0;

    private JSONArray failureList;

    public void addFailurelist(@NotNull Object key, @Nullable Object value) {
        this.success--;//失败的--
        this.failure++;//成功的++
        JSONObject tmpJson = JSONObject.parseObject("{}");
        tmpJson.put("keyField", key);//key第几条
        tmpJson.put("msg", value);//错误原因
        failureList.add(tmpJson);
    }

    public void errMsg(int result, String errMsg) {
        this.result = result;
        this.errMsg = errMsg;
    }

    @Override
    public String toString() {
        JSONObject ret = JSONObject.parseObject("{}");
        ret.put("finished", finished);//true,fasle判断是否上传完毕
        //ret.put("progress", String.valueOf(Math.round((success + failure) * 1.0 / count * 100)) + "%");
        ret.put("progress", String.valueOf(Math.round((success + failure) * 1.0 / count * 100)));
        ret.put("result", result);//是否有报错
        ret.put("errMsg", errMsg);//报错结果
        ret.put("success", success);//成功条数
        ret.put("failure", failure);//失败条数
        ret.put("values", failureList);//失败原因
        return ret.toString();
    }

}
