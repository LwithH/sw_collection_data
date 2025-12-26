package com.util;

import java.util.UUID;

public class FileUtil {
    public static String generateUniqueFileName(String originalName) {
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + ext;
    }
}
