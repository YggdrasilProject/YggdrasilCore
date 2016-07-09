package ru.linachan.mmo.auth.user;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private Token token;
    private User user;
    private Map<String, Object> attributes = new HashMap<>();

    public Session(Token sessionToken, User sessionUser) {
        token = sessionToken;
        user = sessionUser;
    }

    public Token getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Object getAttribute(String attribute, Object defautlValue) {
        return attributes.getOrDefault(attribute, defautlValue);
    }

    public void setAttribute(String attribute, Object value) {
        attributes.put(attribute, value);
    }
}
