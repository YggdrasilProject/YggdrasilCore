package ru.linachan.yggdrasil.event;

import java.util.Map;

public class YggdrasilEvent {

    private String eventType;
    private Map<String, String> eventData;

    public YggdrasilEvent(String type, Map<String, String> data) {
        eventType = type;
        eventData = data;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }
}
