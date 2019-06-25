package com.tskj.core.system.utility;

import com.tskj.core.config.ConfigUtility;

import javax.servlet.ServletContext;

public class Logs {
    public static ServletContext mContext = null;

    public static void log(String msg) {
        if (mContext != null) {
            mContext.log(msg);
        }
    }

}
