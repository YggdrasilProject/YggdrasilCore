package ru.linachan.yggdrasil.common.nio.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface ServerHandler {

    ByteBuffer onMessageReceived(ByteBuffer input, SelectionKey key);

    void onConnect(SelectionKey key);

    void onDisconnect(SelectionKey key);

    void onStart(boolean isRestarting);

    void onStop();
}
