package com.tskj.fileimport.process;

import com.tskj.fileimport.system.Tools;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author LeonSu
 */
public class AsyncRequestProcessor implements Runnable {
    private AsyncContext asyncContext;
    private String uid;

    public AsyncRequestProcessor(AsyncContext asyncContext, String uid) {
        this.asyncContext = asyncContext;
        this.uid = uid;
    }

    @Override
    public void run() {
        DataProcess dataProcess = null;
        try {
            dataProcess = DataManager.getInstance().get(uid);
            String originHeader = ((HttpServletRequest) asyncContext.getRequest()).getHeader("Origin");
            originHeader = originHeader == null ? "" : originHeader;
            if (dataProcess != null) {
                Tools.sendResponseText(asyncContext.getResponse(), originHeader, dataProcess.toString());
            } else {
                //Tools.sendResponseText(asyncContext.getResponse(), originHeader, "{\"finished\":false,\"progress\":\"0\",\"success\":0,\"failure\":0,\"values\":[]}");
                Tools.sendResponseText(asyncContext.getResponse(), originHeader, "{\"result\":0,\"errMsg\":\"\",\"finished\":false,\"progress\":\"0\",\"success\":0,\"failure\":0,\"values\":[]}");


            }
        } finally {
            if (dataProcess != null && dataProcess.isFinished()) {
                DataManager.getInstance().remove(dataProcess);
            }
            asyncContext.complete();
        }

    }
}
