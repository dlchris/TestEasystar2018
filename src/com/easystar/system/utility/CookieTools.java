package com.easystar.system.utility;

import javax.servlet.http.Cookie;

public class CookieTools {
    public static Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(cookieName)) {
                return cookie;
            }
        }
        return null;
    }
}
