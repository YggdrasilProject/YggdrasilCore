package ru.linachan.yggdrasil.auth;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class YggdrasilAuthUser implements Serializable {

    private String userName;
    private Map<String, String> attributes;

    public YggdrasilAuthUser(String authUserName) {
        userName = authUserName;
        attributes = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getAttribute(String attribute) {
        return attributes.containsKey(attribute) ? attributes.get(attribute) : null;
    }

    public void setAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }
}
