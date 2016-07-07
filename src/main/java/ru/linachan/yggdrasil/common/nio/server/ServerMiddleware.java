package ru.linachan.yggdrasil.common.nio.server;

import java.nio.ByteBuffer;

public interface ServerMiddleware {

    ByteBuffer decode(ByteBuffer inputBuffer);
    ByteBuffer encode(ByteBuffer outputBuffer);

}
