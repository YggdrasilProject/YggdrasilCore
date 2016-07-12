package ru.linachan.yggdrasil.common;

import java.util.ArrayList;
import java.util.List;

public class Queue<T> {

    private List<T> objectQueue = new ArrayList<>();

    public synchronized T pop() {
        T object = null;

        if (!objectQueue.isEmpty()) {
            object = objectQueue.get(0);
            objectQueue.remove(0);
        }

        return object;
    }

    public List<T> list() {
        return objectQueue;
    }

    public synchronized void push(T object) {
        objectQueue.add(object);
    }
}
