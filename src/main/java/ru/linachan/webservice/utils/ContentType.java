package ru.linachan.webservice.utils;

import com.google.common.io.Files;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ContentType {

    private static Map<String, String> getContentTypes() {
        Map<String, String> contentTypes = new HashMap<>();

        contentTypes.put("ico", "image/x-icon");

        return contentTypes;
    }

    public static String guessContentType(String fileName) {
        String contentType = URLConnection.guessContentTypeFromName(fileName);

        if (contentType != null)
            return contentType;

        return getContentTypes().getOrDefault(Files.getFileExtension(fileName), "content/unknown");
    }
}
