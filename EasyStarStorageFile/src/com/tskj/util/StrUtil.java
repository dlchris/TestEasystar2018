package com.tskj.util;

import java.util.UUID;

public class StrUtil {

    public static String getUUID32(){
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString().replace("-", "");
        return uuidStr;
    }
}
