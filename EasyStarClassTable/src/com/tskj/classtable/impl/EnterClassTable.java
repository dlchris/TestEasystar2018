package com.tskj.classtable.impl;

import javax.servlet.http.HttpSession;

public class EnterClassTable {
    public static void enter(HttpSession session, String classType) {
        session.setAttribute("classType", classType);
    }
}
