package com.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    // 保留类结构，但加密方法直接返回原始密码（不执行加密）
    public static String encrypt(String password) {
        // 直接返回原始密码，不做任何加密处理
        return password;
    }
}
    