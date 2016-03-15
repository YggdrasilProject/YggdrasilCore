package ru.linachan.yggdrasil.common.console;

public interface InterruptHandler {

    void onEOTEvent(); // Handles Control + D
    void onETXEvent(); // Handles Control + C
    void onSUBEvent(); // Handles Control + Z
}
