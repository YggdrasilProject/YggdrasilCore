package ru.linachan.yggdrasil.event;

import java.util.ArrayList;
import java.util.List;

public class YggdrasilEventSystem {

    private final List<YggdrasilEventListener> eventListeners;

    public YggdrasilEventSystem() {
        eventListeners = new ArrayList<>();
    }

    public void sendEvent(YggdrasilEvent event) {
        for (YggdrasilEventListener eventListener: eventListeners) {
            eventListener.onEvent(event);
        }
    }

    public void registerListener(YggdrasilEventListener listener) {
        eventListeners.add(listener);
    }

    public void unRegisterListener(YggdrasilEventListener listener) {
        if (eventListeners.contains(listener)) {
            eventListeners.remove(listener);
        }
    }
}
