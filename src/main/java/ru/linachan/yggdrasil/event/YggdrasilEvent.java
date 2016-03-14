package ru.linachan.yggdrasil.event;

import java.util.Map;

public class YggdrasilEvent {

    private String eventType;
    private Map<String, Object> eventData;

    public YggdrasilEvent(String type, Map<String, Object> data) {
        eventType = type;
        eventData = data;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }
}
