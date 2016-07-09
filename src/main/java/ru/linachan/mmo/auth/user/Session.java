package ru.linachan.mmo.auth.user;

import ru.linachan.mmo.utils.DataWriter;
import ru.linachan.mmo.world.Player;

import javax.xml.crypto.Data;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class Session {

    private Token token;
    private User user;
    private Player player = null;

    private SelectionKey key;

    private Map<String, Object> attributes = new HashMap<>();
    private DataWriter writer = null;

    private Long startTime;
    private Long updateTime;

    public Session(Token sessionToken, User sessionUser) {
        token = sessionToken;
        user = sessionUser;

        startTime = updateTime = System.currentTimeMillis();
    }

    public void update() {
        updateTime = System.currentTimeMillis();
    }

    public Token getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player sessionPlayer) {
        player = sessionPlayer;
    }

    public Object getAttribute(String attribute, Object defautlValue) {
        return attributes.getOrDefault(attribute, defautlValue);
    }

    public void setAttribute(String attribute, Object value) {
        attributes.put(attribute, value);
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public Long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public void setKey(SelectionKey sessionKey) {
        if (!sessionKey.equals(key)) {
            key = sessionKey;

            if (writer != null)
                writer.stop();

            writer = new DataWriter(key);
            writer.start();
        }
    }

    public SelectionKey getKey() {
        return key;
    }

    public DataWriter getWriter() {
        return writer;
    }

    public void onClose() {
        if (writer != null) {
            writer.stop();
        }
    }
}
