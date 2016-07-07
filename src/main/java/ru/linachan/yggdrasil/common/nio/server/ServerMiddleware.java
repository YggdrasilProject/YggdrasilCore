package ru.linachan.yggdrasil.common.nio.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface ServerMiddleware {

    ByteBuffer decode(ByteBuffer inputBuffer, SelectionKey key);
    ByteBuffer encode(ByteBuffer outputBuffer, SelectionKey key);

}
